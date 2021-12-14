package com.github.psiotwo.eccairs.rdf.snowowl;

import static com.github.psiotwo.eccairs.rdf.SnomedConstants.ENTIRE_TERM_CASE_SENSITIVE;
import static com.github.psiotwo.eccairs.rdf.SnomedConstants.EN_UK;
import static com.github.psiotwo.eccairs.rdf.SnomedConstants.EN_US;
import static com.github.psiotwo.eccairs.rdf.SnomedConstants.PK_NAMESPACE;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.psiotwo.eccairs.rdf.SnomedConstants;
import com.github.psiotwo.eccairs.rdf.snowowl.model.CreateClassAxiomDto;
import com.github.psiotwo.eccairs.rdf.snowowl.model.CreateConceptDescriptionDto;
import com.github.psiotwo.eccairs.rdf.snowowl.model.CreateConceptDto;
import com.github.psiotwo.eccairs.rdf.snowowl.model.CreateConceptRelationshipDto;
import com.github.psiotwo.eccairs.rdf.snowowl.model.CreateRefsetMemberDto;
import com.github.psiotwo.eccairs.rdf.snowowl.model.PreferredTerm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SnowowlDtoHelper {

    private static final Map<String, String> prefAcceptabilityMap = new HashMap<>();

    static {
        prefAcceptabilityMap.put(EN_US + "", "PREFERRED");
        prefAcceptabilityMap.put(EN_UK + "", "PREFERRED");
    }

    public static String postRefsetMemberPayload(final Long member, final Long refsetId,
                                                 final Long moduleId)
        throws JsonProcessingException {
        final CreateRefsetMemberDto dto = new CreateRefsetMemberDto()
            .setActive(true)
            .setRefsetId(refsetId)
            .setReferencedComponentId(member)
            .setModuleId(moduleId);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(dto);
    }


    public static String conceptPayload(final Map<Long, Set<String>> descriptions,
                                        final Map<Long, Set<Long>> relationships,
                                        final String preferredTerm,
                                        final long moduleId, String semanticTag,
                                        final Long id)
        throws JsonProcessingException {
        if (descriptions.get(SnomedConstants.FSN).isEmpty()) {
            throw new IllegalArgumentException();
        }

        final List<CreateConceptDescriptionDto> descriptionPayload = new ArrayList<>();

        descriptionPayload.add(
            createConceptDescriptionDtoInModel(preferredTerm, SnomedConstants.SYNONYM, true));

        descriptions.forEach((k, v) -> {
            v.forEach(vv -> {
                descriptionPayload.add(createConceptDescriptionDtoInModel(vv, k, false));
            });
        });

        final List<CreateConceptRelationshipDto> relationshipPayload = new ArrayList<>();
        relationships.forEach((k, v) -> {
            v.forEach(vv -> {
                relationshipPayload.add(createConceptRelationshipDtoInModel(vv, k, true));
                relationshipPayload.add(createConceptRelationshipDtoInModel(vv, k, false));
            });
        });

        final List<CreateClassAxiomDto> ccaDto = Collections.singletonList(
            new CreateClassAxiomDto().setRelationships(relationshipPayload));

        final CreateConceptDto createConceptDto = new CreateConceptDto()
            .setActive(true)
            .setModuleId(moduleId)
            .setCommitComment("Creating concept " + descriptions)
            .setDefaultModuleId(moduleId)
            .setNamespaceId(PK_NAMESPACE + "")
            .setDescriptions(descriptionPayload)
            .setClassAxioms(ccaDto)
            .setPt(new PreferredTerm()
                .setLang("en")
                .setTerm(preferredTerm)
            );

        if (id != null) {
            createConceptDto.setConceptId(id);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(createConceptDto);
        return s;
    }

    private static CreateConceptDescriptionDto createConceptDescriptionDtoInModel(
        final String preferredLabel,
        final long typeId,
        final boolean preferred) {
        return new CreateConceptDescriptionDto()
            .setActive(true)
            .setLanguageCode("en")
            .setCaseSignificanceId(ENTIRE_TERM_CASE_SENSITIVE + "")
            .setTerm(preferredLabel)
            .setAcceptabilityMap(preferred ? prefAcceptabilityMap : Collections.emptyMap())
            .setTypeId(typeId);
    }

    private static CreateConceptRelationshipDto createConceptRelationshipDtoInModel(
        final long destinationId,
        final long typeId,
        final boolean inferred) {
        return new CreateConceptRelationshipDto()
            .setActive(true)
            .setDestinationId(destinationId)
            .setTypeId(typeId)
            .setInferred(inferred);
    }
}