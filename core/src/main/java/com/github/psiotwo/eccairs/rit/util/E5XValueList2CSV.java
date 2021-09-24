package com.github.psiotwo.eccairs.rit.util;

import static java.util.Objects.requireNonNull;


import com.github.psiotwo.eccairs.rit.model.Value;
import com.github.psiotwo.eccairs.rit.model.ValueList;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class E5XValueList2CSV {

    private final String dir;

    public E5XValueList2CSV(String dir) {
        this.dir = dir;
    }

    public void export() {
        final File[] files = new File(dir + "/documented schema")
            .listFiles(f -> f.getName().startsWith("VL"));
        if (files == null) {
            return;
        }
        Arrays.stream(requireNonNull(files))
            .filter(f -> !f.getName().startsWith("VL1085_")) // black listing country list
            .filter(f -> !f.getName().startsWith("VL1182_")) // black listing aircraft types
            .filter(f -> !f.getName().startsWith("VL1016_")) // black listing operators
            .filter(f -> !f.getName().startsWith("VL1183_")) // black listing reporting entities
            .filter(f -> !f.getName().startsWith("VL1007_")) // black listing manufacturers/models
            .forEach(ff -> {
                    String vlName = ff.getName().substring(0, ff.getName().length() - 4);
                    final ValueList valueList = new E5XXSDValueListParser().parse(vlName, ff);
                    try {
                        final CSVWriter w =
                            new CSVWriter(new FileWriter(vlName + ".csv", Charset.defaultCharset()));
                        w.writeNext(new String[] {"valuelistName", "valueId", "valueDescription",
                            "valueDetailedDescription", "valueExplanation"});

                        for (Value v : valueList.getValues()) {
                            w.writeNext(new String[] {vlName, v.getValueId() + "", v.getDescription(),
                                v.getDetailedDescription(), v.getExplanation()});
                        }
                    } catch (IOException e) {
                        log.error("Error during valuelist serialization.", e);
                    }
                }
            );
    }

    public static void main(String[] args) {
        new E5XValueList2CSV(args[0]).export();
    }
}
