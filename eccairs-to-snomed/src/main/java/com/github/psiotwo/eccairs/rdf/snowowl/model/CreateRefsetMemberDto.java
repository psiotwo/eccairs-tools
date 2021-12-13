package com.github.psiotwo.eccairs.rdf.snowowl.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class CreateRefsetMemberDto {

    private boolean active;

    private long referencedComponentId;

    private long moduleId;

    private long refsetId;
}
