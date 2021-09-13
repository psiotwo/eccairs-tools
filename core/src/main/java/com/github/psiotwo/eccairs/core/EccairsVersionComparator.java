package com.github.psiotwo.eccairs.core;

import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import com.github.psiotwo.eccairs.core.model.EccairsTerm;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class EccairsVersionComparator {

    private final EccairsDictionary origD;

    private final EccairsDictionary newD;

    /**
     * Compares two versions of Eccairs taxonomy.
     */
    public void compare() {
        // compare entities
        for (EccairsEntity newE : newD.getEntities()) {
            Optional<EccairsEntity> origE = EccairsTaxonomyUtils
                .entities(origD).stream().filter(ee -> ee.getId() == newE.getId()).findAny();
            if (origE.isEmpty()) {
                log.info("Entity {} is not present in {}.", newE.getId(), origD.getVersion());
                continue;
            }
            compareDescriptions(origE.get(), newE);
        }

        // compare attributes
        for (EccairsAttribute newA : EccairsTaxonomyUtils.attributes(newD)) {
            Optional<EccairsAttribute> origA = EccairsTaxonomyUtils.attributes(origD).stream()
                .filter(ee -> ee.getId() == newA.getId()).findAny();
            if (origA.isEmpty()) {
                log.info("Attribute {} ({}) is not present in {}.", newA.getId(),
                    newA.getDescription(),
                    origD.getVersion());
                continue;
            }
            compareDescriptions(origA.get(), newA);

            if (newA.getValues() != null) {
                final List<EccairsValue> newValues = EccairsTaxonomyUtils.getValues(newA);
                final List<Integer> newValueIds =
                    newValues.stream().map(EccairsValue::getId).collect(Collectors.toList());

                final List<EccairsValue> origValues = EccairsTaxonomyUtils.getValues(origA.get());
                final List<Integer> origValueIds =
                    origValues.stream().map(EccairsValue::getId).collect(Collectors.toList());

                final List<Integer> newNotOrigValues = new ArrayList<>(newValueIds);
                newNotOrigValues.removeAll(origValueIds);
                newNotOrigValues.sort(Integer::compareTo);

                final List<Integer> origNotNewValues = new ArrayList<>(origValueIds);
                origNotNewValues.removeAll(newValueIds);
                origNotNewValues.sort(Integer::compareTo);

                if (!newNotOrigValues.isEmpty() || !origNotNewValues.isEmpty()) {
                    log.error("Values for attribute {} ({}) differ: \n\t {} : {}\n\t {} : {}",
                        newA.getId(),
                        newA.getDescription(),
                        newD.getVersion(),
                        EccairsTaxonomyUtils.renderValues(newValues, newNotOrigValues),
                        origD.getVersion(),
                        EccairsTaxonomyUtils.renderValues(origValues, origNotNewValues)
                    );
                }
            }
        }
    }

    private void compareDescriptions(EccairsTerm origT, EccairsTerm newT) {
        if (!origT.getDescription().equals(newT.getDescription())) {
            log.info(
                "{} has different descriptions in both versions \n\t {} : {}\n\t {} : {}",
                newT.getId(),
                origD.getVersion(),
                origT.getDescription(),
                newD.getVersion(),
                newT.getDescription()
            );
        }
    }

    public static void main(String[] args) {
        String eccairsOriginalTaxonomyFile = args[0];
        String eccairsNewTaxonomyFile = args[1];

        EccairsDictionary eccairsOrig = new EccairsTaxonomyParser()
            .parse(new File(eccairsOriginalTaxonomyFile));

        EccairsDictionary eccairsNew = new EccairsTaxonomyParser()
            .parse(new File(eccairsNewTaxonomyFile));

        new EccairsVersionComparator(eccairsOrig, eccairsNew).compare();
    }
}