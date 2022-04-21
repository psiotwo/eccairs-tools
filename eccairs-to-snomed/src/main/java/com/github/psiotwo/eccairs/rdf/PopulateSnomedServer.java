package com.github.psiotwo.eccairs.rdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.psiotwo.eccairs.core.EccairsTaxonomyParser;
import com.github.psiotwo.eccairs.core.EccairsTaxonomyUtils;
import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import com.github.psiotwo.eccairs.core.model.EccairsTerm;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import com.google.common.base.Strings;
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

    private final SnomedCtStoreApi api;

    /**
     * White list takes precedence.
     */
//    private final List<Integer> whiteListEntities =
//        Arrays.asList(1, 3, 4, 12, 14, 15, 24);
    private final List<Integer> whiteListAttribute = null;
//        Arrays.asList(430, 390, 391, 385, 386, 392, 393, 394);
    private final List<Integer> blackListAttributes = Arrays.asList(5, 16, 21, 167, 215, 228);

    /**
     * Only generate first level of values - a sample.
     */
    private boolean sample = true;

    private final String branch;
    private long entityCount = 0;
    private long attributeCount = 0;
    private long valueCount = 0;

    /**
     * ECCAIRS id to SCTID
     */
    private final Map<Integer, Long> eIdMap = new HashMap<>();
    private final Map<Integer, Long> aIdMap = new HashMap<>();
    private final Map<Integer, Long> aRefSetIdMap = new HashMap<>();

    /**
     * Map from ECCAIRS id of the attribute to a map from an ECCAIRS id of the value to the SCTID of the value
     */
    private final Map<Integer, Map<Integer, Long>> aVIdMap = new HashMap<>();

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
        createEccairs(d);

        api.finish();
    }


    private void createEccairs(final EccairsDictionary d) throws JsonProcessingException {
        final Set<Integer> entityIds = new HashSet<>();

        for (final EccairsEntity e : d.getEntities()) {
            if (entityIds.contains(e.getId())) {
                continue;
            }
            storeEntity(d, EccairsTaxonomyUtils.getEntityForId(d, e.getId()), entityIds);
        }
    }

    private boolean isValid(final EccairsAttribute a) {
        if (whiteListAttribute != null && !whiteListAttribute.contains(a.getId())) {
            log.warn("Skipping not whitelisted attribute (" + a.getId() + ")");
            return false;
        } else if (whiteListAttribute == null && blackListAttributes.contains(a.getId())) {
            log.warn("Skipping blacklisted attribute (" + a.getId() + ")");
            return false;
        }
        return true;
    }

    private void initModel() throws UnirestException {
        try {
            api.createConcept("ECCAIRS entity", SnomedConstants.CONCEPT, branch, SnomedEccairsConstants.ECCAIRS_MODULE_ID,
                "entity",
                SnomedEccairsConstants.ENTITY);
            api.createConcept("ECCAIRS value", SnomedConstants.CONCEPT, branch, SnomedEccairsConstants.ECCAIRS_MODULE_ID,
                "value",
                SnomedEccairsConstants.VALUE);
            api.createConcept("Refers to", SnomedConstants.CONCEPT_MODEL_OBJECT_ATTRIBUTE,
                branch,
                SnomedEccairsConstants.ECCAIRS_MODULE_ID,
                "attribute",
                SnomedEccairsConstants.HAS_SUB_ENTITY);
            api.createConcept("ECCAIRS module", SnomedConstants.MODULE,
                branch,
                SnomedEccairsConstants.ECCAIRS_MODULE_ID,
                "core metadata concept", SnomedEccairsConstants.ECCAIRS_MODULE_ID);
            api.createConcept("ECCAIRS My Airport module", SnomedConstants.MODULE,
                branch,
                SnomedEccairsConstants.ECCAIRS_MODULE_ID,
                "core metadata concept", SnomedEccairsConstants.ECCAIRS_MODULE_ORGANIZATION_EXTENSION_ID);
            api.updateConcept("Has ECCAIRS id", SnomedConstants.CONCEPT_MODEL_DATA_ATTRIBUTE,
                branch,
                SnomedEccairsConstants.ECCAIRS_MODULE_ID,
                "attribute", SnomedEccairsConstants.HAS_ID);
        } catch (Exception e) {
            log.error("Skipping errors during ", e);
        }
    }

    private Map<Long, Set<String>> createDescriptions(final EccairsTerm e, final String semanticTag)
        throws UnirestException {
        final Map<Long, Set<String>> descriptions = new HashMap<>();
        descriptions.put(SnomedConstants.FSN, Collections.singleton(
            "[" + e.getId() + "] " + e.getDescription() + " (" + semanticTag + ")"));
        final Set<String> synonyms = new HashSet<>();
        if (Strings.nullToEmpty(e.getExplanation()).trim().isEmpty()) {
            descriptions.put(SnomedConstants.DEFINITION, Collections.singleton(e.getExplanation()));
        }
        if (Strings.nullToEmpty(e.getDetailedDescription()).trim().isEmpty()) {
            synonyms.add(e.getDetailedDescription());
        }

        descriptions.put(SnomedConstants.SYNONYM, synonyms);

        return descriptions;
    }

    private Map<Long, Set<Object>> createRelationships(final EccairsTerm t, final Long parent)
        throws UnirestException {
        final Map<Long, Set<Object>> relationships = new HashMap<>();
        final Set<Object> superClasses = new HashSet<>();
        superClasses.add(parent);
        relationships.put(SnomedConstants.IS_A, superClasses);
        relationships.put(SnomedEccairsConstants.HAS_ID, Collections.singleton(t.getId()));
        return relationships;
    }

    private long storeEntity(final EccairsDictionary d, final EccairsEntity e, final Set<Integer> entityIds)
        throws JsonProcessingException, UnirestException {
        entityIds.add(e.getId());

        log.info("[" + ++entityCount + "] Entity (" + e.getId() + ")" + e.getDescription());
        final Map<Long, Set<String>> descriptions = createDescriptions(e, "Entity");
        final Map<Long, Set<Object>> relationships =
            createRelationships(e, SnomedEccairsConstants.ENTITY);
//        final Map<Long, Set<Object>> dataRelationships = createDataRelationships(e);
        relationships.put(SnomedEccairsConstants.HAS_SUB_ENTITY, new HashSet<>());

        if (e.getEntities() != null) {
            for (final EccairsEntity ee : e.getEntities()) {
                Long childConceptId = null;
                if (entityIds.contains(ee.getId())) {
                    if (eIdMap.containsKey(ee.getId())) {
                        childConceptId = eIdMap.get(ee.getId());
                    } else {
                        continue;
                    }
                }

                if ( childConceptId == null ) {
                    childConceptId = storeEntity(d, EccairsTaxonomyUtils.getEntityForId(d, ee.getId()), entityIds);
                }

                relationships.get(SnomedEccairsConstants.HAS_SUB_ENTITY).add(childConceptId);
            }
        }
        if (e.getAttributes() != null) {
            for (final EccairsAttribute a : e.getAttributes()) {
                if (!isValid(a)) {
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
                        createRelationships(a,
                            valueListAttribute ? SnomedConstants.CONCEPT_MODEL_OBJECT_ATTRIBUTE :
                                SnomedConstants.CONCEPT_MODEL_DATA_ATTRIBUTE),
                        a.getDescription(),
                        branch,
                        SnomedEccairsConstants.ECCAIRS_MODULE_ID,
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
            log.info(
                " - skipping " + e.getId() + " : " + e.getDescription() + ", already created. ");
            eId = eIdMap.get(e.getId());
        }

        if (eId == null) {
            log.info(" - creating " + e.getId() + " : " + e.getDescription());
            eId = api.createConcept(
                descriptions,
                relationships,
                e.getDescription(),
                branch,
                SnomedEccairsConstants.ECCAIRS_MODULE_ID,
                "entity");
            eIdMap.put(e.getId(), eId);
        } else if (!relationships.isEmpty()) {
            log.info(
                " - updating " + e.getId() + " : " + e.getDescription() + " with relationships " +
                    relationships);
            api.updateConcept(
                descriptions,
                relationships,
                e.getDescription(),
                branch,
                SnomedEccairsConstants.ECCAIRS_MODULE_ID,
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
            descriptions3.put(k,
                descriptions.get(k).stream().map(d -> d + " reference set").collect(
                    Collectors.toSet()));
        });

        final Map<Long, Set<Object>> relationships =
            createRelationships(a, SnomedEccairsConstants.VALUE);

        final Long id = api.createConcept(
            descriptions2,
            relationships,
            a.getDescription(),
            branch,
            SnomedEccairsConstants.ECCAIRS_MODULE_ID,
            "value");

        final Map<Long, Set<Object>> relationships3 = new HashMap<>();
        relationships3.put(SnomedConstants.IS_A, Collections.singleton(
            SnomedConstants.SIMPLE_TYPE_REFERENCE_SET));
        final Long idRefSet = api.createConcept(
            descriptions3,
            relationships3,
            a.getDescription() + " reference set",
            branch,
            SnomedEccairsConstants.ECCAIRS_MODULE_ID,
            "foundation metadata concept");
        aRefSetIdMap.put(a.getId(), idRefSet);

        aVIdMap.putIfAbsent(a.getId(), new HashMap<>());

        for (final EccairsValue eccairsValue : a.getValues()) {
            storeValue(eccairsValue, id, idRefSet, SnomedEccairsConstants.ECCAIRS_MODULE_ID, aVIdMap.get(a.getId()));
        }

        return id;
    }

    private Long storeValue(final EccairsValue value, final Long parentId, final Long idRefSet, final Long moduleId,
                            final Map<Integer, Long> valueMap)
        throws JsonProcessingException, UnirestException {
        log.info(
            "[" + ++valueCount + "] Value (" + value.getId() + ") : " + value.getDescription());

        final Map<Long, Set<String>> descriptions = createDescriptions(value, "value");
        final Map<Long, Set<Object>> relationships = createRelationships(value, parentId);


        final Long id = api.createConcept(
            descriptions,
            relationships,
            value.getDescription(),
            branch,
            moduleId,
            "value");

        valueMap.put(value.getId(), id);

        api.addMemberToRefset(
            id,
            idRefSet,
            branch,
            moduleId);

        if (value.getValues() != null && !sample) {
            for (final EccairsValue subV : value.getValues()) {
                storeValue(subV, id, idRefSet, moduleId, valueMap);
            }
        }
        return id;
    }
}
