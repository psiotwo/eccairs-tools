package com.github.psiotwo.eccairs.rit.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RitAttribute {
    private int id;
    private String shortDescription;
    private String detailedDescription;
    private String synonymForRit;
    private int eccairsDatatype;
    private String umDefault;
    private String valueListId;
    private Integer specialAttributeId;
    private int minInstances;
    private int maxInstances;
    private int attributeSequence;
}
