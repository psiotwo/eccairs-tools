package com.github.psiotwo.eccairs.rdf.snowstorm;

import static com.github.psiotwo.eccairs.rdf.DtoHelper.conceptPayload;
import static com.github.psiotwo.eccairs.rdf.DtoHelper.postRefsetMemberPayload;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.psiotwo.eccairs.rdf.SnomedCtStoreApi;
import java.util.Map;
import java.util.Set;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class SnowstormApi implements SnomedCtStoreApi {

    private final String serverUrl;

    public SnowstormApi(String serverUrl) {
        this.serverUrl = serverUrl;
        Unirest.config()
            .socketTimeout(0)
            .connectTimeout(0)
            .automaticRetries(true)
            .retryAfter(true,20);
    }

    public String createBranch(final String parentPath, final String childName)
        throws UnirestException {
        final JSONObject object = new JSONObject();
        object.put("parent", parentPath);
        object.put("name", childName);

        final HttpResponse<String> jsonResponse
            = Unirest.post(serverUrl + "/branches")
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


    public long createConcept(final Map<Long, Set<String>> descriptions,
                              final Map<Long, Set<Object>> relationships,
                              final String preferredTerm,
                              final String branch,
                              final long moduleId,
                              final String semanticTag,
                              final Long id)
        throws UnirestException, JsonProcessingException {
        final String c =
            conceptPayload(descriptions, relationships, preferredTerm, moduleId, id);
        final HttpResponse<String> jsonResponse;
        jsonResponse = Unirest.post(serverUrl + "/browser/" + branch + "/concepts")
            .header("content-type", "application/json")
            .body(c)
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
                              final Map<Long, Set<Object>> relationships,
                              final String preferredTerm,
                              final String branch,
                              final long moduleId,
                              final String semanticTag,
                              final Long id)
        throws UnirestException, JsonProcessingException {
        final String c =
            conceptPayload(descriptions, relationships, preferredTerm, moduleId, id);
        final HttpResponse<String> jsonResponse =
            Unirest.put(serverUrl + "/browser/" + branch + "/concepts/" + id)
                .header("content-type", "application/json")
                .body(c)
                .asString();
        if (!jsonResponse.isSuccess()) {
            if (jsonResponse.getStatus() != 409) {
                throw new RuntimeException(
                    "Concept creation failed for " + descriptions + " with error " + jsonResponse
                        .getBody());
            }
        }
        return id;
    }
}