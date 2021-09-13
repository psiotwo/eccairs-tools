package com.github.psiotwo.eccairs.rdf;

import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import java.util.Collections;
import java.util.List;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EccairsTaxonomyToRdfTest {

    private EccairsDictionary dictionary = new EccairsDictionary()
        .setTaxonomy("ECCAIRS Aviation")
        .setVersion("3.4.0.2")
        .setLanguage("English")
        .setKey("00d4fe6f-f522-42a8-801f-65fd2a58af31")
        .setEntities(Collections.emptyList());

    private EccairsEntity entity = new EccairsEntity()
        .setId(1)
        .setDescription("Description of 1")
        .setExplanation("Explanation of 1")
        .setDetailedDescription("Detailed Description of 1");

    @Test
    public void exporterWritesTaxonomyNameCorrectly() {
        final EccairsDictionary dictionary = this.dictionary;
        final EccairsTaxonomyToRdf r = new EccairsTaxonomyToRdf("http://test.org/", dictionary);
        final OntModel model = r.transform();
        Assertions.assertEquals(1, model.listOntologies().toList().size());
        Assertions.assertEquals("http://test.org/eccairs/aviation-3.4.0.2",
            model.listOntologies().next().getURI());
    }

    @Test
    public void exporterWritesEntityAttributes() {
        final EccairsDictionary dictionary = this.dictionary;
        dictionary.setEntities(Collections.singletonList(entity));
        final EccairsTaxonomyToRdf r = new EccairsTaxonomyToRdf("http://test.org/", dictionary);
        final OntModel model = r.transform();
        final List<Resource> entities =
            model.listSubjectsWithProperty(RDF.type, model.getOntClass(Vocabulary.s_c_entity))
                .toList();
        Assertions.assertEquals(1, entities.size());
        final Resource rEntity = entities.iterator().next();
        Assertions.assertEquals("Description of 1",
            rEntity.getProperty(model.getProperty(Vocabulary.s_p_has_description)).getString(), entity.getDescription());
        Assertions.assertEquals("Explanation of 1",
            rEntity.getProperty(model.getProperty(Vocabulary.s_p_has_explanation)).getString(), entity.getExplanation());
        Assertions.assertEquals("Detailed Description of 1",
            rEntity.getProperty(model.getProperty(Vocabulary.s_p_has_detailed_description)).getString(), entity.getDetailedDescription());
    }
}
