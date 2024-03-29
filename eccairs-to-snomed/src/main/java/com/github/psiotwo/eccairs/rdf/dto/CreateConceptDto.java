package com.github.psiotwo.eccairs.rdf.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class CreateConceptDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long conceptId;

    private boolean active;

    private String commitComment;

    private long moduleId;

    private long defaultModuleId;

    private String namespaceId;

    private PreferredTerm pt;

    private List<CreateConceptDescriptionDto> descriptions;

    private List<CreateClassAxiomDto> classAxioms;
}
