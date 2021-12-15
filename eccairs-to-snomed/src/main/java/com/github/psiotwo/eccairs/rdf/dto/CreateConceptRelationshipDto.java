package com.github.psiotwo.eccairs.rdf.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    private boolean concrete;

    private long moduleId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ConcreteValue concreteValue;
}
