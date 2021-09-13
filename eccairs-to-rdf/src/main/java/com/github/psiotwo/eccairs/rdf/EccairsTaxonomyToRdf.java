package com.github.psiotwo.eccairs.rdf;

import com.github.psiotwo.eccairs.core.EccairsTaxonomyParser;
import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import com.github.psiotwo.eccairs.core.model.EccairsTerm;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDFS;

@Slf4j
public class EccairsTaxonomyToRdf {

    private OntModel model;
    private final EccairsDictionary dictionary;
    private String baseIri;

    private String ontologyBaseIri;
    private String lang;

    /**
     * Creates a new serializer of ECCAIRS to RDF.
     *
     * @param baseIri    root RDF namespace
     * @param dictionary RDF dictionary to transform
     */
    public EccairsTaxonomyToRdf(final String baseIri, final EccairsDictionary dictionary) {
        this.baseIri = baseIri;
        this.dictionary = dictionary;
    }

    private String generateOntologyIri(final EccairsDictionary dictionary) {
        return this.baseIri
            + dictionary.getTaxonomy().toLowerCase(Locale.ROOT).replace(" ", "/") + "-" +
            dictionary.getVersion();
    }

    private String generateOntologyPrefix(final EccairsDictionary dictionary) {
        return Arrays.stream(dictionary.getTaxonomy().split(" "))
            .map(c -> c.toLowerCase(Locale.ROOT).charAt(0) + "")
            .collect(Collectors.joining("-"))
            + "-" + dictionary.getVersion().replace(".", "_");
    }

    /**
     * Transforms an ECCAIRS taxonomy into RDF.
     */
    public OntModel transform() {
        model = ModelFactory.createOntologyModel();
        model.setNsPrefix("e-m", Vocabulary.ONTOLOGY_IRI_model + "/");

        transformDictionary();

        dictionary.getEntities().forEach(this::transformEntity);

        return model;
    }

    private void transformDictionary() {
        final String ontologyIRI = generateOntologyIri(dictionary);
        this.ontologyBaseIri = ontologyIRI + "/";

        this.lang = getLang(dictionary.getLanguage());

        final String prefix = generateOntologyPrefix(dictionary);
        model.setNsPrefix(prefix, ontologyBaseIri);
        final Ontology o = model.createOntology(ontologyIRI);
        o.addProperty(model.createAnnotationProperty(Vocabulary.s_p_has_key), dictionary.getKey());
    }

    private AnnotationProperty ap(final String uri) {
        return model.createAnnotationProperty(uri);
    }

    private ObjectProperty op(final String uri) {
        return model.createObjectProperty(uri);
    }

    private DatatypeProperty dp(final String uri) {
        return model.createDatatypeProperty(uri);
    }


    private void createEntityChildLink(final Individual parent,
                                       final Individual child,
                                       final String minInstance,
                                       final String maxInstance,
                                       final String isLink) {
        final Statement s = ResourceFactory
            .createStatement(parent, op(Vocabulary.s_p_has_child),
                child);
        model.add(s);
        final ReifiedStatement rs = s.createReifiedStatement(
            ontologyBaseIri + "r-" +
                parent.getURI().substring(parent.getURI().lastIndexOf("-") - 1)
                + "-" + child.getURI()
                .substring(child.getURI().lastIndexOf("-") - 1));
        rs.addProperty(ap(Vocabulary.s_p_has_min_instance),
            minInstance);
        rs.addProperty(ap(Vocabulary.s_p_has_max_instance),
            maxInstance);
        if (isLink != null) {
            rs.addProperty(ap(Vocabulary.s_p_is_link), isLink);
        }
        model.add(rs.getModel());
    }

    private Statement createSubValueLink(final Individual parent,
                                         final Individual child) {
        final Statement s = ResourceFactory
            .createStatement(parent, op(Vocabulary.s_p_has_child),
                child);
        model.add(s);
        return s;
    }

    private Individual transformEntity(final EccairsEntity entity) {
        final OntClass cEntity = model.createClass(Vocabulary.s_c_entity);
        final Individual rEntity =
            model.createIndividual(ontologyBaseIri + "e-" + entity.getId(), cEntity);
        addProperties(rEntity, entity);
        Optional.ofNullable(entity.getEntities()).orElse(Collections.emptySet())
            .forEach(subEntity -> {
                final Individual rChild = transformEntity(subEntity);
                createEntityChildLink(rEntity, rChild, subEntity.getMinInstance(),
                    subEntity.getMaxInstance(), subEntity.getIsLink());
            });
        Optional.ofNullable(entity.getAttributes()).orElse(Collections.emptySet())
            .forEach(attribute -> {
                final Individual rChild = transformAttribute(attribute);
                createEntityChildLink(rEntity, rChild, attribute.getMinInstance(),
                    attribute.getMaxInstance(), null);
            });
        return rEntity;
    }

    private Individual transformAttribute(final EccairsAttribute attribute) {
        final OntClass cAttribute = model.createClass(Vocabulary.s_c_attribute);
        final Individual rAttribute =
            model.createIndividual(ontologyBaseIri + "a-" + attribute.getId(), cAttribute);
        addProperties(rAttribute, attribute);
        Optional.ofNullable(attribute.getValues()).orElse(Collections.emptyList())
            .forEach(value -> {
                final Individual rValue = transformValue(rAttribute, value);
                createSubValueLink(rAttribute, rValue);
            });
        return rAttribute;
    }

    private Individual transformValue(final Individual rAttribute, final EccairsValue value) {
        final OntClass cValue = model.createClass(Vocabulary.s_c_value);
        final String valueListIdx = getValueListId(rAttribute);
        final Individual rValue =
            model.createIndividual(ontologyBaseIri + valueListIdx + "-v-" + value.getId(),
                cValue);
        addProperties(rValue, value);
        Optional.ofNullable(value.getValues()).orElse(Collections.emptyList()).forEach(child -> {
            final Individual rChild = transformValue(rAttribute, child);
            createSubValueLink(rValue, rChild);
        });
        return rValue;
    }

    private String getValueListId(Resource rAttribute) {
        return "vl-a-" + rAttribute.getProperty(dp(Vocabulary.s_p_has_id)).getString();
    }

    private static String getLang(String language) {
        switch (language) {
            case "English":
                return "en";
            case "Czech":
                return "cs";
            default:
                throw new IllegalArgumentException("Unsupported language '" + language + "' ");
        }
    }

    private void addProperties(final Resource resource, final EccairsEntity entity) {
        addTermProperties(resource, entity);
    }

    private void addProperties(final Resource resource, final EccairsAttribute attribute) {
        resource.addProperty(dp(Vocabulary.s_p_has_value_type), attribute.getValueType());
        resource.addProperty(dp(Vocabulary.s_p_has_data_type), attribute.getDataType());
        if (attribute.getDefaultUnit() != null) {
            resource.addProperty(dp(Vocabulary.s_p_has_default_unit), attribute.getDefaultUnit());
        }
        addTermProperties(resource, attribute);
    }

    private void addProperties(final Resource resource, final EccairsValue value) {
        resource.addProperty(model.createDatatypeProperty(Vocabulary.s_p_has_level),
            value.getLevel());
        addTermProperties(resource, value);
    }

    private void addTermProperties(final Resource resource, final EccairsTerm term) {
        resource.addProperty(RDFS.label, ResourceFactory
            .createLangLiteral(term.getId() + " - " + term.getDescription(), lang));
        resource.addProperty(model.createDatatypeProperty(Vocabulary.s_p_has_id),
            ResourceFactory.createTypedLiteral(term.getId()));
        resource.addProperty(model.createDatatypeProperty(Vocabulary.s_p_has_description),
            ResourceFactory.createLangLiteral(term.getDescription(), lang));
        resource.addProperty(model.createDatatypeProperty(Vocabulary.s_p_has_detailed_description),
            ResourceFactory.createLangLiteral(term.getDetailedDescription(), lang));
        resource.addProperty(model.createDatatypeProperty(Vocabulary.s_p_has_explanation),
            ResourceFactory.createLangLiteral(term.getExplanation(), lang));
    }

    public static void main(String[] args) throws IOException {
        final EccairsTaxonomyParser p = new EccairsTaxonomyParser();
        final EccairsDictionary d = p.parse(new File(args[0]));
        final EccairsTaxonomyToRdf r = new EccairsTaxonomyToRdf("http://test.org/", d);
        final OntModel model = r.transform();
        RDFDataMgr.write(new FileOutputStream(args[0] + ".trig"), model, RDFFormat.TRIG_PRETTY);
    }
}