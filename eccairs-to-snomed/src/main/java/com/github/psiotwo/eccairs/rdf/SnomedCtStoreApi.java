package com.github.psiotwo.eccairs.rdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import kong.unirest.UnirestException;

public interface SnomedCtStoreApi {

    void start();

    void finish();


    void addMemberToRefset(final Long member,
                       final Long refsetId,
                       final String branch,
                       final long moduleId)
        throws UnirestException, JsonProcessingException;

    long createConcept(final Map<Long, Set<String>> descriptions,
                       final Map<Long, Set<Object>> relationships,
                       final String preferredTermInEnglish,
                       final String branch,
                       final long moduleId,
                       String semanticTag, Long id)
        throws UnirestException, JsonProcessingException;

    long updateConcept(final Map<Long, Set<String>> descriptions,
                       final Map<Long, Set<Object>> relationships,
                       final String preferredTermInEnglish,
                       final String branch,
                       final long moduleId,
                       String semanticTag, Long id)
        throws UnirestException, JsonProcessingException;

    default long createConcept(final Map<Long, Set<String>> descriptions,
                               final Map<Long, Set<Object>> relationships,
                               final String preferredTermInEnglish,
                               final String branch,
                               final long moduleId, String semanticTag)
        throws JsonProcessingException {
        return createConcept(descriptions, relationships, preferredTermInEnglish, branch, moduleId, semanticTag, null);
    }

    default long createConcept(final String conceptPL,
                               final long parentId,
                               final String branch,
                               final long moduleId,
                               final String semanticTag,
                               final Long id)
        throws UnirestException, JsonProcessingException {
        final Map<Long, Set<String>> descriptions = new HashMap<>();
        descriptions.put(SnomedConstants.FSN, Collections.singleton(conceptPL));
        final Map<Long, Set<Object>> relationships = new HashMap<>();
        relationships.put(SnomedConstants.IS_A, Collections.singleton(parentId));
        return createConcept(descriptions, relationships, conceptPL, branch, moduleId, semanticTag, id);
    }

    default long updateConcept(final String conceptPL,
                               final long parentId,
                               final String branch,
                               final long moduleId,
                               final String semanticTag,
                               final Long id)
        throws UnirestException, JsonProcessingException {
        final Map<Long, Set<String>> descriptions = new HashMap<>();
        descriptions.put(SnomedConstants.FSN, Collections.singleton(conceptPL));
        final Map<Long, Set<Object>> relationships = new HashMap<>();
        relationships.put(SnomedConstants.IS_A, Collections.singleton(parentId));
        return updateConcept(descriptions, relationships, conceptPL, branch, moduleId, semanticTag, id);
    }
}