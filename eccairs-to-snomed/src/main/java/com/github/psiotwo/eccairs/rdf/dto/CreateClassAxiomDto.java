package com.github.psiotwo.eccairs.rdf.dto;

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
