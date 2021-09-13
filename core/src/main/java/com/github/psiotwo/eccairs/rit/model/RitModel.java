package com.github.psiotwo.eccairs.rit.model;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RitModel {

    String dir;

    Map<Integer, RitEntity> entities = new LinkedHashMap<>();

    Map<Integer, RitAttribute> attributes = new LinkedHashMap<>();

    Map<String, ValueList> valueLists = new LinkedHashMap<>();

    public String toString() {
        return dir +
            System.lineSeparator() +
            MessageFormat.format(" - # entities = {0}",
                this.getEntities().size()) +
            System.lineSeparator() +
            MessageFormat.format(" - # attributes = {0}",
                this.getAttributes().size()) +
            System.lineSeparator() +
            MessageFormat.format(" - # valueLists = {0}",
                this.getValueLists().size()) +
            System.lineSeparator();
    }
}
