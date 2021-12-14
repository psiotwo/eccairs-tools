package com.github.psiotwo.eccairs.rdf.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class CreateRelationshipDto {

    private boolean active;

    private String commitComment;

    private long moduleId;

    private long defaultModuleId;

    private String namespaceId;

    private long sourceId;

    private long destinationId;

    private long typeId;

}
