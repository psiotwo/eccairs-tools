package com.github.psiotwo.eccairs.rdf.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class PreferredTerm {

    private String lang;

    private String term;
}
