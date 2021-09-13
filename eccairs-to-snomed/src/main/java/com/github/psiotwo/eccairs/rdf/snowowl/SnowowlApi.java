package com.github.psiotwo.eccairs.rdf.snowowl;

import static com.github.psiotwo.eccairs.rdf.SnomedConstants.EN_UK;
import static com.github.psiotwo.eccairs.rdf.SnomedConstants.EN_US;
import static com.github.psiotwo.eccairs.rdf.SnomedConstants.PK_NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.psiotwo.eccairs.rdf.SnomedCtStoreApi;
import com.github.psiotwo.eccairs.rdf.snowowl.model.CreateRelationshipDto;
import java.util.HashMap;
import java.util.Map;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class SnowowlApi implements SnomedCtStoreApi {

    public static final String SNOMED_CT_V_3 = "/snomed-ct/v3/";
    private final String serverUrl;

    private final Map<String,String> acceptabilityMap = new HashMap<>();

    public SnowowlApi(String serverUrl) {
        this.serverUrl = serverUrl;
        Unirest.config()
            .socketTimeout(0);

        acceptabilityMap.put(EN_US + "", "PREFERRED");
        acceptabilityMap.put(EN_UK + "", "PREFERRED");
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
            .body(SnowowlDtoHelper.postConceptPayloadInModel(conceptPL, parentId, moduleId, semanticTag, id))
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
                SnowowlDtoHelper.postDescriptionPayloadInModel(conceptId, description, descriptionTypeId, moduleId))
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