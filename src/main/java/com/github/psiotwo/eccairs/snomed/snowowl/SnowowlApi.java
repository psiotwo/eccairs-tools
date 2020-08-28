package com.github.psiotwo.eccairs.snomed.snowowl;


import static com.github.psiotwo.eccairs.snomed.SnomedConstants.ENTIRE_TERM_CASE_SENSITIVE;
import static com.github.psiotwo.eccairs.snomed.SnomedConstants.EN_UK;
import static com.github.psiotwo.eccairs.snomed.SnomedConstants.EN_US;
import static com.github.psiotwo.eccairs.snomed.SnomedConstants.PK_NAMESPACE;


import com.github.psiotwo.eccairs.snomed.SnomedConstants;
import com.github.psiotwo.eccairs.snomed.SnomedCtStoreApi;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateConceptDescriptionDto;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateConceptDto;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateConceptRelationshipDto;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateDescriptionDto;
import com.github.psiotwo.eccairs.snomed.snowowl.model.CreateRelationshipDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Map;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class SnowowlApi implements SnomedCtStoreApi {

    public static final String SNOMED_CT_V_3 = "/snomed-ct/v3/";
    private final String serverUrl;

    public SnowowlApi(String serverUrl) {
        this.serverUrl = serverUrl;
        Unirest.config()
            .socketTimeout(0);
    }

    public String createBranch(final String parentPath, final String childName)
        throws UnirestException {
        final JSONObject object = new JSONObject();
        object.put("parent", parentPath);
        object.put("name", childName);

        final HttpResponse<String> jsonResponse
            = Unirest.post(serverUrl + SNOMED_CT_V_3 + "branches")
            .header("content-type", "application/json")
            .body(object)
            .asString();

        return jsonResponse.getBody();
    }

    @Override
    public void start() {

    }

    @Override
    public void finish() {

    }

    public String postConceptPayloadInModel(final String conceptPL, final long parentId,
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


    public String postDescriptionPayloadInModel(final long conceptId,
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

    private CreateDescriptionDto createDescriptionDtoInModel(final long conceptId,
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
            .setAcceptability(Map.of(
                EN_US + "", "PREFERRED",
                EN_UK + "", "PREFERRED"
            )).
                setTypeId(typeId);
    }

    private CreateConceptDescriptionDto createConceptDescriptionDtoInModel(
        final String preferredLabel,
        final long typeId) {
        return new CreateConceptDescriptionDto()
            .setActive(true)
            .setLanguageCode("en")
            .setCaseSignificanceId(ENTIRE_TERM_CASE_SENSITIVE + "")
            .setTerm(preferredLabel)
            .setAcceptability(Map.of(
                EN_US + "", "PREFERRED",
                EN_UK + "", "PREFERRED"
            )).
                setTypeId(typeId);
    }

    private CreateConceptRelationshipDto createConceptRelationshipDtoInModel(
        final long destinationId,
        final long typeId) {
        return new CreateConceptRelationshipDto()
            .setActive(true)
            .setDestinationId(destinationId)
            .setTypeId(typeId);
    }

    public long createConcept(final String conceptPL, final long parentId, final String branch,
                              final long moduleId, String semanticTag)
        throws UnirestException, JsonProcessingException {
        return createConcept(conceptPL, parentId, branch, moduleId, semanticTag, null);
    }

    /**
     * Silently ignoring when the concept already exists.
     *
     * @param conceptPL
     * @param parentId
     * @param branch
     * @param moduleId
     * @param semanticTag
     * @param id
     * @return
     * @throws UnirestException
     * @throws JsonProcessingException
     */
    public long createConcept(final String conceptPL, final long parentId, final String branch,
                              final long moduleId, String semanticTag, Long id)
        throws UnirestException, JsonProcessingException {
        final HttpResponse<String> jsonResponse
            = Unirest.post(serverUrl + SNOMED_CT_V_3 + branch + "/concepts")
            .header("content-type", "application/json")
            .body(postConceptPayloadInModel(conceptPL, parentId, moduleId, semanticTag, id))
            .asString();
        if (jsonResponse.isSuccess()) {
            String location = jsonResponse.getHeaders().get("Location").get(0);
            return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
        } else {
            if ( jsonResponse.getStatus() != 409 ) {
                throw new RuntimeException(
                    "Concept creation failed for " + conceptPL + " with error " + jsonResponse
                        .getBody());
            }
            return id;
        }
    }

    public String postRelationshipPayloadInModel(final long source, final long target,
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

    public void addRelationship(final long source, final long target, final long attribute,
                                final String branch,
                                final long moduleId)
        throws UnirestException, JsonProcessingException {
        final HttpResponse<String> jsonResponse
            = Unirest.post(serverUrl + SNOMED_CT_V_3 + branch + "/relationships")
            .header("content-type", "application/json")
            .body(postRelationshipPayloadInModel(source, target, attribute, moduleId))
            .asString();
        if (jsonResponse.isSuccess()) {
            return;// jsonResponse.getHeaders().get("Location").get(0);
        } else {
            throw new RuntimeException(
                "Adding relationships failed for " + source + " , " + target + " : " + attribute
                    + " with error " + jsonResponse.getBody());
        }
    }

    public void addDescription(final long conceptId,
                               final long descriptionTypeId,
                               final String description,
                               final String branch,
                               final long moduleId)
        throws UnirestException, JsonProcessingException {
        final HttpResponse<String> jsonResponse
            = Unirest.post(serverUrl + SNOMED_CT_V_3 + branch + "/descriptions")
            .header("content-type", "application/json")
            .body(
                postDescriptionPayloadInModel(conceptId, description, descriptionTypeId, moduleId))
            .asString();
        if (jsonResponse.isSuccess()) {
            return;
        } else {
            throw new RuntimeException(
                "Concept creation failed for '" + description + "' with error " + jsonResponse
                    .getBody());
        }
    }
}