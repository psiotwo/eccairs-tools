package com.github.psiotwo.eccairs.rit.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ValueList {

    private String id;

    private List<Value> values = new ArrayList<>();
}
