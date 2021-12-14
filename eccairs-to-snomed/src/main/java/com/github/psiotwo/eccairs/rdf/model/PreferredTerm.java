package com.github.psiotwo.eccairs.rdf.model;

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
