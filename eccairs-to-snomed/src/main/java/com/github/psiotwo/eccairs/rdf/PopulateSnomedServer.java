package com.github.psiotwo.eccairs.rdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.psiotwo.eccairs.core.EccairsTaxonomyParser;
import com.github.psiotwo.eccairs.core.EccairsTaxonomyUtils;
import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import com.github.psiotwo.eccairs.core.model.EccairsTerm;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;

/**
 * entity -> SNOMED concept (subClass of ECCAIRS concept)
 * => min-instance, max-instance [0..1,0..unlimited] => hasReportPart someValuesFrom
 * attribute -> SNOMED concept attribute (subClassOf ECCAIRS concept attribute, SNOMED
 * concept attribute)
 * => min-instance, max-instance [0..1,0..unlimited] => <attribute> someValuesFrom
 * => valueType TODO
 * => dataType TODO
 * => defaultUnit TODO
 * => xsd-tag TODO
 * => domains TODO
 * => size TODO
 * => specialAttribute TODO
 * value -> SNOMED concept
 * => level TODO
 * => domains TODO
 * <p>
 * ***
 * detailed-description -> synonym, preferred in US
 * description -> synonym
 * explanation -> definition
 */
@Slf4j
public class PopulateSnomedServer {

    // ECCAIRS module
    private static final long moduleId = 21000250107L;

    private final SnomedCtStoreApi api;

    /**
     * White list takes precedence.
     */
    private final List<Integer> whiteListEntities =
        Arrays.asList(1, 3, 4, 12, 14, 15, 24);
    private final List<Integer> whiteListAttribute =
        Arrays.asList(430, 390, 391, 385, 386, 392, 393, 394);
    private final List<Integer> blackListAttributes = Arrays.asList(5, 16, 21, 167, 215, 228);

    /**
     * Only generate first level of values - a sample.
     */
    private boolean sample = false;

    private final String branch;
    private long entityCount = 0;
    private long attributeCount = 0;
    private long valueCount = 0;

    /**
     * ECCAIRS id to SCTID
     */
    private final Map<Integer, Long> eIdMap = new HashMap<>();
    private final Map<Integer, Long> aIdMap = new HashMap<>();

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
            if (!whiteListEntities.contains(e.getId())) {
                continue;
            }
            storeEntity(e);
        }
        api.finish();
    }

    private void initModel() throws UnirestException {
        try {
            api.createConcept("ECCAIRS entity", SnomedConstants.CONCEPT, branch, moduleId, "entity",
                SnomedEccairsConstants.ENTITY);
            api.createConcept("ECCAIRS value", SnomedConstants.CONCEPT, branch, moduleId, "value",
                SnomedEccairsConstants.VALUE);
            api.createConcept("has report part", SnomedConstants.CONCEPT_MODEL_OBJECT_ATTRIBUTE,
                branch,
                moduleId,
                "attribute",
                SnomedEccairsConstants.HAS_SUB_ENTITY);
            api.createConcept("has eccairs id", SnomedConstants.CONCEPT_MODEL_DATA_ATTRIBUTE,
                branch,
                moduleId,
                "attribute", SnomedEccairsConstants.HAS_ID);
            api.createConcept("ECCAIRS module", SnomedConstants.MODULE,
                branch,
                moduleId,
                "core metadata concept", moduleId);
//        this.valueListConceptId = api.createConcept("ECCAIRS valuelist", "138875005", branch,
//        moduleId, "eccairs valuelist");
//        this.hasId = api.createConcept("has ECCAIRS id", CONCEPT_ATTRIBUTE, branch, moduleId,
//        "attribute");
        } catch (Exception e) {
            log.error("Skipping errors during ", e);
        }
    }

    private Map<Long, Set<String>> createDescriptions(final EccairsTerm e, final String semanticTag)
        throws UnirestException {
        final Map<Long, Set<String>> descriptions = new HashMap<>();
        descriptions.put(SnomedConstants.FSN, Collections.singleton(e.getDetailedDescription() + " (" + semanticTag + ")"));
        final Set<String> synonyms = new HashSet<>();
        if (!e.getExplanation().trim().isEmpty()) {
            descriptions.put(SnomedConstants.DEFINITION, Collections.singleton(e.getExplanation()));
        }
        if (e.getId() != 0) {
            synonyms.add(e.getId() + "");
        }

        descriptions.put(SnomedConstants.SYNONYM, synonyms);

        return descriptions;
    }

    private Map<Long, Set<Long>> createRelationships(final Long parent)
        throws UnirestException {
        final Map<Long, Set<Long>> relationships = new HashMap<>();
        final Set<Long> superClasses = new HashSet<>();
        superClasses.add(parent);
        relationships.put(SnomedConstants.IS_A, superClasses);
        return relationships;
    }

    private long storeEntity(final EccairsEntity e)
        throws JsonProcessingException, UnirestException {

        log.info("[" + ++entityCount + "] Entity (" + e.getId() + ")" + e.getDescription());
        final Map<Long, Set<String>> descriptions = createDescriptions(e, "Entity");
        final Map<Long, Set<Long>> relationships = createRelationships(SnomedEccairsConstants.ENTITY);
        relationships.put(SnomedEccairsConstants.HAS_SUB_ENTITY, new HashSet<>());

        if (e.getEntities() != null) {
            for (final EccairsEntity ee : e.getEntities()) {
                if (!whiteListEntities.contains(ee.getId())) {
                    continue;
                }
                long childConceptId = storeEntity(ee);
                relationships.get(SnomedEccairsConstants.HAS_SUB_ENTITY).add(childConceptId);
            }
        }
        if (e.getAttributes() != null) {
            for (final EccairsAttribute a : e.getAttributes()) {

                if (whiteListAttribute != null && !whiteListAttribute.contains(a.getId())) {
                    log.warn("Skipping not whitelisted attribute (" + a.getId() + ")");
                    continue;
                } else if (whiteListAttribute == null && blackListAttributes.contains(a.getId())) {
                    log.warn("Skipping blacklisted attribute (" + a.getId() + ")");
                    continue;
                }
                log.info("[" + ++attributeCount + "] Attribute (" + a.getId() + ")" +
                    a.getDescription());
                final boolean valueListAttribute =
                    a.getValues() != null && !a.getValues().isEmpty();

                final long attributeSctId;
                if (!aIdMap.containsKey(a.getId())) {
                    final Map<Long, Set<String>> aDescriptions = createDescriptions(a, "Attribute");

                    attributeSctId = api.createConcept(
                        aDescriptions,
                        createRelationships(valueListAttribute ? SnomedConstants.CONCEPT_MODEL_OBJECT_ATTRIBUTE :
                                    SnomedConstants.CONCEPT_MODEL_DATA_ATTRIBUTE),
                        a.getDescription(),
                        branch,
                        moduleId,
                        "attribute");
                    aIdMap.put(a.getId(), attributeSctId);
                    if (valueListAttribute) {
                        final Long id = storeValueList(a);
                        relationships.putIfAbsent(attributeSctId, new HashSet<>());
                        relationships.get(attributeSctId).add(id);
                    }
                } else {
                    log.info(" - skipping " + a.getId() + " : " + a.getDescription() +
                        ", already created. ");
                }
            }
        }

        Long eId = null;
        if (eIdMap.containsKey(e.getId())) {
            log.info(" - skipping " + e.getId() + " : " + e.getDescription() + ", already created. ");
            eId = eIdMap.get(e.getId());
        }

        if ( eId == null ) {
            log.info(" - creating " + e.getId() + " : " + e.getDescription());
            eId = api.createConcept(
                descriptions,
                relationships,
                e.getDescription(),
                branch,
                moduleId,
                "entity");
            eIdMap.put(e.getId(), eId);
        } else if (!relationships.isEmpty()) {
            log.info(" - updating " + e.getId() + " : " + e.getDescription() + " with relationships " + relationships);
            api.updateConcept(
                descriptions,
                relationships,
                e.getDescription(),
                branch,
                moduleId,
                "entity",
                eId);
        }

        return eId;
    }

    private Long storeValueList(final EccairsAttribute a)
        throws JsonProcessingException, UnirestException {
        final Map<Long, Set<String>> descriptions = createDescriptions(a, "value");
        final Map<Long, Set<String>> descriptions2 = new HashMap<>();
        descriptions.keySet().forEach(k -> {
            descriptions2.put(k, descriptions.get(k).stream().map(d -> "Value for " + d).collect(
                Collectors.toSet()));
        });
        final Map<Long, Set<String>> descriptions3 = new HashMap<>();
        descriptions.keySet().forEach(k -> {
            descriptions3.put(k, descriptions.get(k).stream().map(d -> "RefSet for " + d).collect(
                Collectors.toSet()));
        });

        final Map<Long, Set<Long>> relationships = createRelationships(SnomedEccairsConstants.VALUE);

        final Long id = api.createConcept(
            descriptions2,
            relationships,
            a.getDescription(),
            branch,
            moduleId,
            "value");

        final Map<Long, Set<Long>> relationships3 = new HashMap<>();
        relationships3.put(SnomedConstants.IS_A, Collections.singleton(
            SnomedConstants.SIMPLE_TYPE_REFERENCE_SET));
        final Long idRefSet = api.createConcept(
            descriptions3,
            relationships3,
            a.getDescription(),
            branch,
            moduleId,
            "foundation metadata concept");

        for (final EccairsValue eccairsValue : a.getValues()) {
            storeValue(eccairsValue, id, idRefSet);
        }

        return id;
    }

    private Long storeValue(final EccairsValue value, final Long parentId, final Long idRefSet)
        throws JsonProcessingException, UnirestException {
        log.info("[" + ++valueCount + "] Value (" + value.getId() + ") : " + value.getDescription());

        final Map<Long, Set<String>> descriptions = createDescriptions(value, "value");
        final Map<Long, Set<Long>> relationships = createRelationships(parentId);

        final Long id = api.createConcept(
            descriptions,
            relationships,
            value.getDescription(),
            branch,
            moduleId,
            "value");


        api.addMemberToRefset(
            id,
            idRefSet,
            branch,
            moduleId);

        if (value.getValues() != null && !sample) {
            for (final EccairsValue subV : value.getValues()) {
                storeValue(subV, id, idRefSet);
            }
        }
        return id;
    }
}
