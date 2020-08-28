package com.github.psiotwo.eccairs.snomed.snowowl.model;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class CreateConceptDescriptionDto {

    private Map<String, String> acceptability;

    private boolean active;

    private String caseSignificanceId;

    private String languageCode;

    private String term;

    private long typeId;
}
