package com.github.psiotwo.eccairs.rdf.snowowl.model;

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
    private Long id;

    private boolean active;

    private String commitComment;

    private long moduleId;

    private long defaultModuleId;

    private String namespaceId;

    private List<CreateConceptDescriptionDto> descriptions;

    private List<CreateConceptRelationshipDto> relationships;
}
