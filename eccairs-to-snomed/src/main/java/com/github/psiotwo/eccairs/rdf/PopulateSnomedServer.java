package com.github.psiotwo.eccairs.rdf;

import com.github.psiotwo.eccairs.core.EccairsTaxonomyParser;
import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import com.github.psiotwo.eccairs.core.model.EccairsTerm;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;

/**
 * entity -> SNOMED concept (subClass of ECCAIRS concept)
 *    => min-instance, max-instance [0..1,0..unlimited] => hasReportPart someValuesFrom
 *       attribute -> SNOMED concept attribute (subClassOf ECCAIRS concept attribute, SNOMED
 *       concept attribute)
 *    => min-instance, max-instance [0..1,0..unlimited] => <attribute> someValuesFrom
 *    => valueType TODO
 *    => dataType TODO
 *    => defaultUnit TODO
 *    => xsd-tag TODO
 *    => domains TODO
 *    => size TODO
 *    => specialAttribute TODO
 * value -> SNOMED concept
 *    => level TODO
 *    => domains TODO
 *
 * ***
 *    detailed-description -> synonym, preferred in US
 *    description -> synonym
 *    explanation -> definition
 */
@Slf4j
public class PopulateSnomedServer {

    // ECCAIRS module
    private static final long moduleId = 21000250107L;

    private final SnomedCtStoreApi api;

    private final long entityConceptId = 121000250102L;
    private final long objectAttributeConceptId = 131000250100L;
    private final long dataAttributeConceptId = 151000250107L;
    private final long valueConceptId = 141000250109L;
    private final long hasSubEntity = 1121000250103L;
    private final long hasId = 1931000250104L;

    private long entityCount = 0;
    private long attributeCount = 0;
    private long valueCount = 0;

    private final List<Integer> filteredValueLists = Arrays.asList(5, 16, 21, 167, 215, 228);

    private final String branch;

    public PopulateSnomedServer(final SnomedCtStoreApi api,
                                final String newBranch,
                                final String taxonomyFile
                           )
        throws JsonProcessingException, UnirestException {
        this.branch = "MAIN/" + newBranch;
        this.api = api;
        final EccairsDictionary d = new EccairsTaxonomyParser().parse(new File(taxonomyFile));
        api.start();

        initModel();

        for (final EccairsEntity e : d.getEntities()) {
            storeEntity(e);
        }
        api.finish();
    }

    private void initModel() throws UnirestException {
        try {
            api.createConcept("ECCAIRS entity", SnomedConstants.CONCEPT, branch, moduleId, "entity",
                entityConceptId);
            api.createConcept("ECCAIRS PredefinedValueList Attribute",
                SnomedConstants.CONCEPT_MODEL_OBJECT_ATTRIBUTE,
                branch, moduleId, "attribute", objectAttributeConceptId);
            api.createConcept("ECCAIRS ManualEntry Attribute",
                SnomedConstants.CONCEPT_MODEL_DATA_ATTRIBUTE, branch,
                moduleId, "attribute", dataAttributeConceptId);
            api.createConcept("ECCAIRS value", SnomedConstants.CONCEPT, branch, moduleId, "value",
                valueConceptId);
            api.createConcept("has report part", SnomedConstants.CONCEPT_MODEL_OBJECT_ATTRIBUTE,
                branch,
                moduleId,
                "concept attribute",
                hasSubEntity);
            api.createConcept("has eccairs id", SnomedConstants.CONCEPT_MODEL_DATA_ATTRIBUTE,
                branch,
                moduleId,
                "concept attribute", hasId);
//        this.valueListConceptId = api.createConcept("ECCAIRS valuelist", "138875005", branch,
//        moduleId, "eccairs valuelist");
//        this.hasId = api.createConcept("has ECCAIRS id", CONCEPT_ATTRIBUTE, branch, moduleId,
//        "attribute");
        } catch(Exception e) {
            log.error("Skipping errors during ", e);
        }
    }

    private long addConcept(EccairsTerm e, long parentId, String semanticTag)
        throws JsonProcessingException, UnirestException {
        long conceptId =
            api.createConcept(e.getDetailedDescription(), parentId, branch, moduleId, semanticTag);
        if (!e.getDescription().trim().isEmpty()) {
            api.addDescription(conceptId, SnomedConstants.SYNONYM, e.getDescription(), branch,
                moduleId);
        }
        if (!e.getExplanation().trim().isEmpty()) {
            api.addDescription(conceptId, SnomedConstants.DEFINITION, e.getExplanation(), branch,
                moduleId);
        }
        if (e.getId() != 0) {
            api.addDescription(conceptId, SnomedConstants.SYNONYM, e.getId() + "", branch,
                moduleId); // TODO
        }
        return conceptId;
    }

    private long storeEntity(final EccairsEntity e)
        throws JsonProcessingException, UnirestException {
        long conceptId = addConcept(e, entityConceptId, "Entity");
        log.info("Počet entit: {}", ++entityCount);

        if (e.getEntities() != null) {
            for (final EccairsEntity ee : e.getEntities()) {
                long childConceptId = storeEntity(ee);
                api.addRelationship(conceptId, childConceptId, hasSubEntity, branch, moduleId);
            }
        }
        if (e.getAttributes() != null) {
            for (final EccairsAttribute a : e.getAttributes()) {
                storeAttribute(conceptId, a);
            }
        }
        log.info("Počet prvků modelu: {} ", (entityCount + attributeCount + valueCount));

        return conceptId;
    }

    private void storeAttribute(final long eccairsEntitySctid, final EccairsAttribute a)
        throws JsonProcessingException, UnirestException {

        System.out.println("Počet atributů: " + ++attributeCount);

        // TODO also data types
        if (a.getValues() != null && !a.getValues().isEmpty()) {
            long attributeSctid = addConcept(a, objectAttributeConceptId, "attribute");
            // TODO creating custom valuelist every time
            final long valueListSctid = api.createConcept(
                "Value for " + a.getDetailedDescription(),
                valueConceptId,
                branch,
                moduleId,
                "value");
            api.addRelationship(
                eccairsEntitySctid,
                valueListSctid,
                attributeSctid,
                branch,
                moduleId
            );
            if (!filteredValueLists.contains(a.getId())) {
                for (final EccairsValue eccairsValue : a.getValues()) {
                    storeValue(valueListSctid, eccairsValue);
                }
            }
        } else {
//            long attributeSctid =
            addConcept(a, dataAttributeConceptId, "attribute");
            // TODO data attributes
        }
    }

    private void storeValue(final long parentSctId, final EccairsValue v)
        throws JsonProcessingException, UnirestException {
        final long valueSctid = api.createConcept(
            v.getDescription(),
            parentSctId,
            branch,
            moduleId,
            "value");
        log.info("Počet hodnot: {} ", ++valueCount);

        if (v.getValues() != null) {
            for (final EccairsValue subV : v.getValues()) {
                storeValue(valueSctid, subV);
            }
        }
    }
}
