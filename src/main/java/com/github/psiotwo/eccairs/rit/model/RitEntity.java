package com.github.psiotwo.eccairs.rit.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RitEntity {
    private int id;
    private String name;
    private String synonymForRit;
    private Set<RitAttribute> attributes = new HashSet<>();
    private String idPath;
    private String namePath;
    private String namePathForRit;
}
