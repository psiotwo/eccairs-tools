package com.github.psiotwo.eccairs.taxonomy;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class TaxonomyServiceResponse {

    @JsonRawValue
    private JsonNode data;

    private String returnCode;

    private String errorDetails;
}
