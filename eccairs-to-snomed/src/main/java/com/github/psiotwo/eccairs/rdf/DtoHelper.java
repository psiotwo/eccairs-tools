package com.github.psiotwo.eccairs.rdf;

import static com.github.psiotwo.eccairs.rdf.SnomedConstants.ENTIRE_TERM_CASE_SENSITIVE;
import static com.github.psiotwo.eccairs.rdf.SnomedConstants.EN_UK;
import static com.github.psiotwo.eccairs.rdf.SnomedConstants.EN_US;
import static com.github.psiotwo.eccairs.rdf.SnomedConstants.PK_NAMESPACE;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.psiotwo.eccairs.rdf.dto.ConcreteValue;
import com.github.psiotwo.eccairs.rdf.dto.CreateClassAxiomDto;
import com.github.psiotwo.eccairs.rdf.dto.CreateConceptDescriptionDto;
import com.github.psiotwo.eccairs.rdf.dto.CreateConceptDto;
import com.github.psiotwo.eccairs.rdf.dto.CreateConceptRelationshipDto;
import com.github.psiotwo.eccairs.rdf.dto.CreateRefsetMemberDto;
import com.github.psiotwo.eccairs.rdf.dto.PreferredTerm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DtoHelper {

    private static final Map<String, String> prefAcceptabilityMap = new HashMap<>();

    static {
        prefAcceptabilityMap.put(EN_US + "", "PREFERRED");
        prefAcceptabilityMap.put(EN_UK + "", "PREFERRED");
    }

    public static String postRefsetMemberPayload(final Long member,
                                                 final Long refsetId,
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

    private static String getDataType(final Class c) {
        if (c == Integer.class) {
            return "INTEGER";
        } else if (c == Double.class) {
            return "DECIMAL";
        } else  {
            return "STRING";
        }
    }

    private static ConcreteValue createConcreteValue(final Object object) {
        final String dataType = getDataType(object.getClass());
            return new ConcreteValue()
                .setValue(object.toString())
                .setValueWithPrefix(dataType.equals("STRING") ? "\"" + object + "\"" : "#" + object)
                .setDataType(dataType);
    }

    public static String conceptPayload(final Map<Long, Set<String>> descriptions,
                                        final Map<Long, Set<Object>> relationships,
                                        final String preferredTerm,
                                        final long moduleId,
                                        final Long id)
        throws JsonProcessingException {
        if (descriptions.get(SnomedConstants.FSN).isEmpty()) {
            throw new IllegalArgumentException();
        }

        final List<CreateConceptDescriptionDto> descriptionPayload = new ArrayList<>();

        descriptionPayload.add(
            createConceptDescriptionDtoInModel(preferredTerm, SnomedConstants.SYNONYM, true, moduleId));

        descriptions.forEach((k, v) -> {
            v.forEach(vv -> {
                descriptionPayload.add(createConceptDescriptionDtoInModel(vv, k, false, moduleId));
            });
        });

        final List<CreateConceptRelationshipDto> relationshipPayload = new ArrayList<>();
        relationships.forEach((k, v) -> {
            v.forEach(vv -> {
                if (vv instanceof Long) {
//                    relationshipPayload.add(createConceptRelationshipDtoInModel((long) vv, k, true, moduleId));
                    relationshipPayload.add(createConceptRelationshipDtoInModel((long) vv, k, false, moduleId));
                } else {
                    final ConcreteValue concreteValue = createConcreteValue(vv);
//                    relationshipPayload.add(createConceptRelationshipDtoInModel(concreteValue, k, true, moduleId));
                    relationshipPayload.add(createConceptRelationshipDtoInModel(concreteValue, k, false, moduleId));
                }
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
        final boolean preferred,
        final long moduleId) {
        return new CreateConceptDescriptionDto()
            .setActive(true)
            .setLanguageCode("en")
            .setCaseSignificanceId(ENTIRE_TERM_CASE_SENSITIVE + "")
            .setTerm(preferredLabel)
            .setAcceptabilityMap(preferred ? prefAcceptabilityMap : Collections.emptyMap())
            .setTypeId(typeId)
            .setModuleId(moduleId);
    }

    private static CreateConceptRelationshipDto createConceptRelationshipDtoInModel(
        final long destinationId,
        final long typeId,
        final boolean inferred,
        final long moduleId) {
        return new CreateConceptRelationshipDto()
            .setActive(true)
            .setDestinationId(destinationId)
            .setTypeId(typeId)
            .setModuleId(moduleId)
            .setInferred(inferred);
    }

    private static CreateConceptRelationshipDto createConceptRelationshipDtoInModel(
        final ConcreteValue concreteValue,
        final long typeId,
        final boolean inferred,
        final long moduleId) {
        return new CreateConceptRelationshipDto()
            .setActive(true)
            .setConcrete(true)
            .setModuleId(moduleId)
            .setConcreteValue(concreteValue)
            .setTypeId(typeId)
            .setInferred(inferred);
    }
}