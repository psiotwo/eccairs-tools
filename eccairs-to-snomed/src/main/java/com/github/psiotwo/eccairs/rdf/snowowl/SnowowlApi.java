package com.github.psiotwo.eccairs.rdf.snowowl;

import static com.github.psiotwo.eccairs.rdf.snowowl.SnowowlDtoHelper.postRefsetMemberPayload;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.psiotwo.eccairs.rdf.SnomedCtStoreApi;
import java.util.Map;
import java.util.Set;
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

    public void addMemberToRefset(final Long member,
                                  final Long refsetId,
                                  final String branch,
                                  final long moduleId)
        throws UnirestException, JsonProcessingException {
        final String c =
            postRefsetMemberPayload(member,refsetId,moduleId);
        final HttpResponse<String> jsonResponse;
        jsonResponse = Unirest.post(serverUrl + "/" + branch + "/members")
            .header("content-type", "application/json")
            .body(c)
            .asString();
        if (!jsonResponse.isSuccess()) {
            if (jsonResponse.getStatus() != 409) {
                throw new RuntimeException(
                    "Refset member creation failed with error " + jsonResponse
                        .getBody());
            }
        }
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

    public long createConcept(final Map<Long, Set<String>> descriptions,
                              final Map<Long, Set<Long>> relationships,
                              final String preferredTermInEnglish,
                              final String branch,
                              final long moduleId, String semanticTag, Long id)
        throws UnirestException, JsonProcessingException {
        final HttpResponse<String> jsonResponse
            = Unirest.post(serverUrl + SNOMED_CT_V_3 + branch + "/concepts")
            .header("content-type", "application/json")
            .body(SnowowlDtoHelper.conceptPayload(descriptions, relationships, preferredTermInEnglish, moduleId,
                semanticTag, id))
            .asString();
        if (jsonResponse.isSuccess()) {
            String location = jsonResponse.getHeaders().get("Location").get(0);
            return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
        } else {
            if (jsonResponse.getStatus() != 409) {
                throw new RuntimeException(
                    "Concept creation failed for " + descriptions + " with error " + jsonResponse
                        .getBody());
            }
            return id;
        }
    }


    public long updateConcept(final Map<Long, Set<String>> descriptions,
                              final Map<Long, Set<Long>> relationships,
                              final String preferredTermInEnglish,
                              final String branch,
                              final long moduleId, String semanticTag, Long id)
        throws UnirestException, JsonProcessingException {
        final HttpResponse<String> jsonResponse
            = Unirest.put(serverUrl + SNOMED_CT_V_3 + branch + "/concepts/" + id)
            .header("content-type", "application/json")
            .body(SnowowlDtoHelper.conceptPayload(descriptions, relationships, preferredTermInEnglish, moduleId,
                semanticTag, id))
            .asString();
        if (jsonResponse.isSuccess()) {
            String location = jsonResponse.getHeaders().get("Location").get(0);
            return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
        } else {
            if (jsonResponse.getStatus() != 409) {
                throw new RuntimeException(
                    "Concept creation failed for " + descriptions + " with error " + jsonResponse
                        .getBody());
            }
            return id;
        }
    }
}