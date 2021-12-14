package com.github.psiotwo.eccairs.rdf.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class CreateConceptRelationshipDto {

    private boolean active;

    private long destinationId;

    private long typeId;

    private boolean inferred;
}
