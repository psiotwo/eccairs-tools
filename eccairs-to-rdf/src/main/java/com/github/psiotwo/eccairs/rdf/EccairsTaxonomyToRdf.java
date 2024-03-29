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
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDFS;

/**
 * Transforms ECCAIRS taxonomy into an RDF dataset. It generates:
 * - main model with the RDF version of the ECCAIRS XML file
 * - mapping to the version agnostic IRIs. The mapping is currently only generated for entities, attributes and value lists,
 * BUT NOT VALUES
 */
@Slf4j
public class EccairsTaxonomyToRdf {

    /**
     * Ab RDF dataset with ECCAIRS taxonomy.
     */
    private Dataset dataset;

    /**
     * An Eccairs dictionary.
     */
    private final EccairsDictionary dictionary;

    /**
     * Base Ontology IRI.
     */
    private String base;

    /**
     * Taxonomy language.
     */
    private String lang;

    private final TaxonomyService taxonomyService;

    private OntModel model;
    private Model mapping;

    public String mainGraphIri;
    public String mappingGraphIri;

    /**
     * Creates a new serializer of ECCAIRS to RDF.
     *
     * @param base       base IRI of the ontology
     * @param dictionary RDF dictionary to transform
     */
    public EccairsTaxonomyToRdf(final String base, final EccairsDictionary dictionary, TaxonomyService taxonomyService) {
        this.dictionary = dictionary;
        this.base = base;
        this.taxonomyService = taxonomyService;
    }

    private Map<String, String> setupNamespaces() {
        final Map<String, String> namespaces = new HashMap<>();
        namespaces.put("e-m", Vocabulary.ONTOLOGY_IRI_model + "/");
        namespaces.put("e-a", getOntologyIriBase());
        namespaces.put(EccairsUtils.generateOntologyPrefix(dictionary),
            getVersionedOntologyIriBase());
        return namespaces;
    }

    /**
     * Transforms an ECCAIRS taxonomy into RDF.
     */
    public Dataset transform() {
        dataset = DatasetFactory.createGeneral();
        final Map<String, String> namespaces = setupNamespaces();

        mainGraphIri = EccairsUtils.getVersionedOntologyUrl(base, dictionary.getTaxonomy(),
            dictionary.getVersion());
        mappingGraphIri = mainGraphIri + "/mapping";

        model = ModelFactory.createOntologyModel();
        dataset.addNamedModel(mainGraphIri, model);
        namespaces.forEach((k, v) -> model.setNsPrefix(k, v));

        mapping = ModelFactory.createDefaultModel();
        namespaces.forEach((k, v) -> mapping.setNsPrefix(k, v));
        dataset.addNamedModel(mappingGraphIri, mapping);

        transformDictionary();

        dictionary.getEntities().forEach(this::transformEntity);

        return dataset;
    }

    private void transformDictionary() {
        this.lang = getLang(dictionary.getLanguage());
        final Ontology o = model.createOntology(getVersionedOntologyIri());
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
            getVersionedOntologyIriBase() + "r-" +
                parent.getURI().substring(parent.getURI().lastIndexOf("-") - 1)
                + "-" + child.getURI()
                .substring(child.getURI().lastIndexOf("-") - 1));
        rs.addProperty(ap(Vocabulary.s_p_has_min_instance), minInstance);
        rs.addProperty(ap(Vocabulary.s_p_has_max_instance), maxInstance);
        if (isLink != null) {
            rs.addProperty(ap(Vocabulary.s_p_is_link), isLink);
        }
        model.add(rs.getModel());
    }

    private void createSubValueLink(final Individual parent, final Individual child) {
        model.add(ResourceFactory.createStatement(parent, op(Vocabulary.s_p_has_child), child));
    }

    private Individual transformEntity(final EccairsEntity entity) {
        final OntClass cEntity = model.createClass(Vocabulary.s_c_entity);
        final String localName = "e-" + entity.getId();
        final Individual rEntity =
            model.createIndividual(getVersionedOntologyIriBase() + localName, cEntity);
        addProperties(rEntity, entity);
        addTermMapping(localName);
        Optional.ofNullable(entity.getEntities()).orElse(Collections.emptySet())
            .forEach(subEntity -> {
                createEntityChildLink(rEntity, transformEntity(subEntity),
                    subEntity.getMinInstance(),
                    subEntity.getMaxInstance(), subEntity.getIsLink());
            });
        Optional.ofNullable(entity.getAttributes()).orElse(Collections.emptySet())
            .forEach(attribute -> {
                createEntityChildLink(rEntity, transformAttribute(attribute),
                    attribute.getMinInstance(),
                    attribute.getMaxInstance(), null);
            });
        return rEntity;
    }

    private Individual transformAttribute(final EccairsAttribute attribute) {
        final OntClass cAttribute = model.createClass(Vocabulary.s_c_attribute);
        final String localName = "a-" + attribute.getId();
        final Individual rAttribute =
            model.createIndividual(getVersionedOntologyIriBase() + localName, cAttribute);
        final String valueListId = "vl-a-" + attribute.getId();
        final String valueListUri = getVersionedOntologyIriBase() + valueListId;
        if (model.contains(rAttribute, op(Vocabulary.s_p_has_child), ResourceFactory.createResource(valueListUri))) {
            log.debug("Model already contains attribute {} with value list. Skipping its repeated processing.", rAttribute);
            return rAttribute;
        }
        addProperties(rAttribute, attribute);
        addTermMapping(localName);
        if (!Optional.ofNullable(attribute.getValues()).orElse(Collections.emptyList()).isEmpty()) {
            final OntClass cValueList = model.createClass(Vocabulary.s_c_value_list);
            addTermMapping(valueListId);
            final Individual rValueList = model.createIndividual(valueListUri, cValueList);
            createSubValueLink(rAttribute, rValueList);
            model.setNsPrefix("e-" + valueListId, valueListUri + "/");
            if (taxonomyService != null && taxonomyService.hasHierarchicalValueList(attribute.getId())) {
                log.debug("Attribute {} has hierarchical value list. Fetching the value list from taxonomy service to preserve hierarchy.", attribute.getId());
                final List<EccairsValue> valueList = taxonomyService.getValueList(attribute.getId());
                valueList.forEach(value -> {
                    final Individual rValue = transformValue(value, valueListId);
                    createSubValueLink(rValueList, rValue);
                });
            } else {
                attribute.getValues().forEach(value -> {
                    final Individual rValue = transformValue(value, valueListId);
                    createSubValueLink(rValueList, rValue);
                });
            }
        }
        return rAttribute;
    }

    private Individual transformValue(final EccairsValue value, final String valueListId) {
        final OntClass cValue = model.createClass(Vocabulary.s_c_value);
        final String valueId = valueListId + "/v-" + value.getId();
        final Individual rValue =
            model.createIndividual(getVersionedOntologyIriBase() + valueId, cValue);
        addProperties(rValue, value);
        addMapping(rValue.getURI(), getOntologyIriBase() + valueId);
        Optional.ofNullable(value.getValues()).orElse(Collections.emptyList()).forEach(child -> {
            final Individual rChild = transformValue(child, valueListId);
            createSubValueLink(rValue, rChild);
        });
        return rValue;
    }

    private void addTermMapping(final String termLocalName) {
        addMapping(getVersionedOntologyIriBase() + termLocalName,
            getOntologyIriBase() + termLocalName);
    }

    private void addMapping(final String from, final String to) {
        final Resource versionedResource = mapping.getResource(from);
        final Resource unversionedResource = mapping.getResource(to);
        mapping.add(versionedResource, mapping.getProperty(Vocabulary.s_p_is_version_of),
            unversionedResource);
    }

    private static String getLang(String language) {
        final String languageTag = EccairsUtils.getLangCodeForEccairsLanguage(language);
        if (languageTag == null) {
            throw new IllegalArgumentException("Unsupported language '" + language + "' ");
        }
        return languageTag;
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
        resource.addProperty(dp(Vocabulary.s_p_has_level), value.getLevel());
        addTermProperties(resource, value);
    }

    private void addTermProperties(final Resource resource, final EccairsTerm term) {
        resource.addProperty(RDFS.label, ResourceFactory
            .createLangLiteral(term.getId() + " - " + term.getDescription(), lang));
        resource.addProperty(dp(Vocabulary.s_p_has_id),
            ResourceFactory.createTypedLiteral(term.getId()));
        resource.addProperty(dp(Vocabulary.s_p_has_description),
            ResourceFactory.createLangLiteral(term.getDescription(), lang));
        resource.addProperty(dp(Vocabulary.s_p_has_detailed_description),
            ResourceFactory.createLangLiteral(term.getDetailedDescription(), lang));
        if ( term.getExplanation() != null ) {
            resource.addProperty(dp(Vocabulary.s_p_has_explanation),
                    ResourceFactory.createLangLiteral(term.getExplanation(), lang));
        }
    }

    private String getVersionedOntologyIriBase() {
        return getVersionedOntologyIri() + "/";
    }

    private String getOntologyIriBase() {
        return getOntologyIri() + "/";
    }

    private String getVersionedOntologyIri() {
        return EccairsUtils.getVersionedOntologyUrl(this.base, dictionary.getTaxonomy(),
            dictionary.getVersion());
    }

    private String getOntologyIri() {
        return EccairsUtils.getOntologyUrl(this.base, dictionary.getTaxonomy());
    }

    public static void main(final String[] args) throws IOException {
        final EccairsTaxonomyParser p = new EccairsTaxonomyParser();
        final EccairsDictionary d = p.parse(new File(args[0]));
        final EccairsTaxonomyToRdf r = new EccairsTaxonomyToRdf("http://test.org/", d, null);
        final Dataset dataset = r.transform();
        RDFDataMgr.write(new FileOutputStream(args[0] + ".trig"), dataset, RDFFormat.TRIG_PRETTY);
    }
}
