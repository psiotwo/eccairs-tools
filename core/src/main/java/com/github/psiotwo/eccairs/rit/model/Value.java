package com.github.psiotwo.eccairs.rit.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Value {
    private String valueListId;
    private Integer valueId;
    private String description;
    private String detailedDescription;
    private String explanation;
}
