package com.github.psiotwo.eccairs.taxonomy;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import com.github.psiotwo.eccairs.rdf.TaxonomyService;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

@Slf4j
@Component
public class EccairsTaxonomyService implements TaxonomyService {

    private static final int MAX_ATTEMPTS = 5;

    @Value("${eccairs.taxonomyService.url}")
    private String taxonomyServiceUrl;

    private final RestTemplate client;

    private Integer taxonomyVersionId;

    private DocumentContext taxonomyTree;

    /**
     * Maps ECCAIRS attribute taxonomy identifiers (codes) to ECCAIRS internal identifiers.
     * <p>
     * This is a cache for better performance
     */
    private final Map<Integer, Integer> attributeIdMap = new HashMap<>();

    public EccairsTaxonomyService(RestTemplateBuilder clientBuilder) {
        this.client = clientBuilder.setConnectTimeout(Duration.ofMinutes(5)).build();
        configureJsonPath();
    }

    @Override
    public boolean hasHierarchicalValueList(int attributeId) {
        if (taxonomyServiceUrl == null || taxonomyServiceUrl.isBlank()) {
            log.warn("Taxonomy service URL not configured.");
            return false;
        }
        initializeIfNecessary();
        // Internal ECCAIRS id
        final List<Integer> attIds = taxonomyTree.read("$..[?(@.tc==" + attributeId + " && @.type==\"A\")].id", new TypeRef<>() {});
        if (attIds.isEmpty()) {
            throw new IllegalArgumentException("Attribute with ECCAIRS ID " + attIds + " not found in the taxonomy tree!");
        }
        assert attIds.size() == 1;
        final Integer attId = attIds.get(0);
        attributeIdMap.put(attributeId, attId);
        final TaxonomyServiceResponse attribute = getResponse(() -> client.getForEntity(taxonomyServiceUrl + "/attributes/public/byID/{attributeID}?taxonomyId={taxonomyID}", TaxonomyServiceResponse.class, attId, taxonomyVersionId));
        assert attribute != null;
        try {
            return JsonPath.parse(attribute.getData().toString())
                           .read("$.attributeValueList.levels", Integer.class) > 1;
        } catch (PathNotFoundException e) {
            log.trace("Attribute {} does not have a value list.", attributeId);
            return false;
        }
    }

    private TaxonomyServiceResponse getResponse(Supplier<ResponseEntity<TaxonomyServiceResponse>> request) {
        try {
            return attemptRequest(request, 0);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new WebServiceIntegrationException("Unable to perform request.", e);
        }
    }

    private TaxonomyServiceResponse attemptRequest(Supplier<ResponseEntity<TaxonomyServiceResponse>> request,
                                                   int attempt) throws InterruptedException {
        try {
            final ResponseEntity<TaxonomyServiceResponse> resp = request.get();
            if (!resp.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to get response. Received {}.", resp);
                throw new WebServiceIntegrationException("Unable to retrieve response. Got status " + resp.getStatusCode());
            }
            return resp.getBody();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ConnectException && attempt <= MAX_ATTEMPTS) {
                log.warn("Failed to get response due to {}. Attempting again in 10s.", e.getMessage());
                Thread.sleep(10000L);
                return attemptRequest(request, attempt + 1);
            }
            throw new WebServiceIntegrationException("Unable to get response.", e);
        }
    }

    private void initializeIfNecessary() {
        if (taxonomyVersionId != null) {
            return;
        }
        this.taxonomyVersionId = loadTaxonomyVersionId();
        this.taxonomyTree = loadTaxonomyTree();
    }

    private Integer loadTaxonomyVersionId() {
        final TaxonomyServiceResponse versionInfo = getResponse(() -> client.getForEntity(taxonomyServiceUrl + "/version/public/", TaxonomyServiceResponse.class));
        assert versionInfo != null;
        return JsonPath.parse(versionInfo.getData().toString()).read("$.id", Integer.class);
    }

    private DocumentContext loadTaxonomyTree() {
        final TaxonomyServiceResponse tree = getResponse(() -> client.getForEntity(taxonomyServiceUrl + "/tree/public/", TaxonomyServiceResponse.class));
        assert tree != null;
        return JsonPath.parse(tree.getData().toString());
    }

    @Override
    public List<EccairsValue> getValueList(int attributeId) {
        if (taxonomyServiceUrl == null || taxonomyServiceUrl.isBlank()) {
            log.warn("Taxonomy service URL not configured.");
            return Collections.emptyList();
        }
        log.trace("Loading value list of attribute {}.", attributeId);
        final List<EccairsValue> result = new ArrayList<>();
        final Integer attId = attributeIdMap.get(attributeId);
        final TaxonomyServiceResponse topLevel = getResponse(() -> client.getForEntity(taxonomyServiceUrl + "/attributes/public/showFirstLevelValues?attributesList={attributeID}", TaxonomyServiceResponse.class, attId));
        topLevel.getData().get("map").get(Integer.toString(attId)).forEach(v -> {
            final EccairsValue ev = initEccairsValue(v);
            result.add(ev);
            if (v.get("hasChild") != null && v.get("hasChild").asBoolean()) {
                ev.setValues(getValueDescendants(attributeId, v.get("id").intValue(), 2));
            }
        });
        return result;
    }

    private EccairsValue initEccairsValue(JsonNode valueNode) {
        final EccairsValue ev = new EccairsValue();
        ev.setId(valueNode.get("identifier").intValue());
        ev.setDescription(valueNode.get("description").asText());
        ev.setDetailedDescription(valueNode.get("detailed").asText());
        ev.setLevel(valueNode.get("level").asText());
        ev.setExplanation(valueNode.get("explanation").asText());
        return ev;
    }

    private List<EccairsValue> getValueDescendants(int attributeId, int valId, int level) {
        log.trace("Loading value list of attribute {}, level {}.", attributeId, level);
        final List<EccairsValue> result = new ArrayList<>();
        final TaxonomyServiceResponse children = getResponse(() -> client.getForEntity(taxonomyServiceUrl + "/listofvalue/public/childrenLov/{valueID}", TaxonomyServiceResponse.class, valId));
        children.getData().get("list").forEach(v -> {
            final EccairsValue ev = initEccairsValue(v);
            result.add(ev);
            if (v.get("hasChild") != null && v.get("hasChild").asBoolean()) {
                ev.setValues(getValueDescendants(attributeId, v.get("id").intValue(), level + 1));
            }
        });
        return result;
    }

    private static void configureJsonPath() {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }
}
