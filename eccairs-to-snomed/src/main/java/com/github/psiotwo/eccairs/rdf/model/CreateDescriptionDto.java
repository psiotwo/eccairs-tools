package com.github.psiotwo.eccairs.rdf.model;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class CreateDescriptionDto {

    private Map<String, String> acceptability;

    private long moduleId;

    private long conceptId;

    private String commitComment;

    private boolean active;

    private String caseSignificanceId;

    private long defaultModuleId;

    private String namespaceId;

    private String languageCode;

    private String term;

    private long typeId;
}
