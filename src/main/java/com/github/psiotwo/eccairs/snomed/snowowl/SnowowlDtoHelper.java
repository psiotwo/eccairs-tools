package com.github.psiotwo.eccairs.snomed.snowowl;

import static com.github.psiotwo.eccairs.snomed.SnomedConstants.ENTIRE_TERM_CASE_SENSITIVE;
import static com.github.psiotwo.eccairs.snomed.SnomedConstants.EN_UK;
import static com.github.psiotwo.eccairs.snomed.SnomedConstants.EN_US;
import static com.github.psiotwo.eccairs.snomed.SnomedConstants.PK_NAMESPACE;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.psiotwo.eccairs.snomed.SnomedConstants;
import com.github.psiotwo.eccairs.snomed.SnomedCtStoreApi;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateConceptDescriptionDto;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateConceptDto;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateConceptRelationshipDto;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateDescriptionDto;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateRelationshipDto;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class SnowowlDtoHelper {

    private static final Map<String,String> acceptabilityMap = new HashMap<>();

    public SnowowlDtoHelper(String serverUrl) {
        acceptabilityMap.put(EN_US + "", "PREFERRED");
        acceptabilityMap.put(EN_UK + "", "PREFERRED");
    }

    public static String postConceptPayloadInModel(final String conceptPL, final long parentId,
                                            final long moduleId, String semanticTag,
                                            final Long id)
        throws JsonProcessingException {
        final CreateConceptDto createConceptDto = new CreateConceptDto()
            .setActive(true)
            .setModuleId(moduleId)
            .setCommitComment("Creating concept " + conceptPL)
            .setDefaultModuleId(moduleId)
            .setNamespaceId(PK_NAMESPACE + "")
            .setDescriptions(Arrays.asList(
                createConceptDescriptionDtoInModel(conceptPL, SnomedConstants.FSN),
                createConceptDescriptionDtoInModel(conceptPL + " (" + semanticTag + ")",
                    SnomedConstants.SYNONYM)
            ))
            .setRelationships(Arrays.asList(
                createConceptRelationshipDtoInModel(parentId, SnomedConstants.IS_A)
            ));

        if (id != null) {
            createConceptDto.setId(id);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(createConceptDto);
        return s;
    }


    public static String postDescriptionPayloadInModel(final long conceptId,
                                                final String conceptPL,
                                                final long typeId,
                                                final long moduleId)
        throws JsonProcessingException {
        final CreateDescriptionDto createConceptDto
            = createDescriptionDtoInModel(conceptId, conceptPL, typeId, moduleId);

        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(createConceptDto);
        return s;
    }

    private static CreateDescriptionDto createDescriptionDtoInModel(final long conceptId,
                                                             final String term,
                                                             final long typeId,
                                                             final long moduleId) {
        return new CreateDescriptionDto()
            .setActive(true)
            .setLanguageCode("en")
            .setModuleId(moduleId)
            .setConceptId(conceptId)
            .setCommitComment("Creating description " + term + " for " + conceptId)
            .setDefaultModuleId(moduleId)
            .setNamespaceId(PK_NAMESPACE + "")
            .setCaseSignificanceId(ENTIRE_TERM_CASE_SENSITIVE + "")
            .setTerm(term)
            .setAcceptability(acceptabilityMap)
            .setTypeId(typeId);
    }

    private static CreateConceptDescriptionDto createConceptDescriptionDtoInModel(
        final String preferredLabel,
        final long typeId) {
        return new CreateConceptDescriptionDto()
            .setActive(true)
            .setLanguageCode("en")
            .setCaseSignificanceId(ENTIRE_TERM_CASE_SENSITIVE + "")
            .setTerm(preferredLabel)
            .setAcceptability(acceptabilityMap)
            .setTypeId(typeId);
    }

    private static CreateConceptRelationshipDto createConceptRelationshipDtoInModel(
        final long destinationId,
        final long typeId) {
        return new CreateConceptRelationshipDto()
            .setActive(true)
            .setDestinationId(destinationId)
            .setTypeId(typeId);
    }

    public static String postRelationshipPayloadInModel(final long source, final long target,
                                                 final long attribute,
                                                 final long moduleId)
        throws JsonProcessingException {
        final CreateRelationshipDto createConceptDto = new CreateRelationshipDto()
            .setActive(true)
            .setModuleId(moduleId)
            .setCommitComment("Creating relationship " + attribute)
            .setDefaultModuleId(moduleId)
            .setNamespaceId(PK_NAMESPACE + "")
            .setSourceId(source)
            .setTypeId(attribute)
            .setDestinationId(target);

        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(createConceptDto);
        return s;
    }
}