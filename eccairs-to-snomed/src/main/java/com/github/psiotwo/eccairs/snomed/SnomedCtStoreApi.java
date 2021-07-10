package com.github.psiotwo.eccairs.snomed;

import com.fasterxml.jackson.core.JsonProcessingException;
import kong.unirest.UnirestException;

public interface SnomedCtStoreApi {

    void start();

    void finish();

    long createConcept(final String conceptPL, final long parentId, final String branch,
                       final long moduleId, String semanticTag)
        throws UnirestException, JsonProcessingException;

    long createConcept(final String conceptPL, final long parentId, final String branch,
                       final long moduleId, String semanticTag, Long id)
        throws UnirestException, JsonProcessingException;

    void addRelationship(final long source, final long target, final long attribute,
                         final String branch,
                         final long moduleId)
        throws UnirestException, JsonProcessingException;

    void addDescription(final long conceptId,
                        final long descriptionTypeId,
                        final String description,
                        final String branch,
                        final long moduleId)
        throws UnirestException, JsonProcessingException;
}