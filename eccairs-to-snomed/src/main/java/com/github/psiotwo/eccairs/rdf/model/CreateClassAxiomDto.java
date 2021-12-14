package com.github.psiotwo.eccairs.rdf.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class CreateClassAxiomDto {

    private List<CreateConceptRelationshipDto> relationships;
}
