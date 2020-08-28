package com.github.psiotwo.eccairs;

import com.github.psiotwo.eccairs.core.EccairsTaxonomyParser;
import com.github.psiotwo.eccairs.core.EccairsTaxonomyUtils;
import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import com.github.psiotwo.eccairs.rit.RitParser;
import com.github.psiotwo.eccairs.rit.model.RitModel;
import com.github.psiotwo.eccairs.rit.model.RitAttribute;
import com.github.psiotwo.eccairs.rit.model.RitEntity;
import com.github.psiotwo.eccairs.rit.model.Value;
import com.github.psiotwo.eccairs.rit.model.ValueList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RitToEccairsComparator {

    public static void main(String[] args) throws IOException {
        String ritDir = args[0];
        String eccairsTaxonomyFile = args[1];

        RitModel rit = new RitParser(ritDir).parse();

        EccairsDictionary eccairs = new EccairsTaxonomyParser()
            .parse(new File(eccairsTaxonomyFile));

        // compare entities
        for (RitEntity e : rit.getEntities().values()) {
            Optional<EccairsEntity> ex = EccairsTaxonomyUtils
                .entities(eccairs).stream().filter(ee -> ee.getId() == e.getId()).findAny();
            if (!ex.isPresent()) {
                log.info("Entity {} is not present in RIT.", e.getId());
            }
            if (!ex.get().getDescription().equals(e.getName())) {
                log.info("Entity {} has different descriptions in RIT and ECCAIRS {} : {}",
                    e.getId(),
                    ex.get().getDescription(), e.getName());
            }
        }

        // compare attributes
        for (RitAttribute e : rit.getAttributes().values()) {
            Optional<EccairsAttribute> ex = EccairsTaxonomyUtils.attributes(eccairs).stream()
                .filter(ee -> ee.getId() == e.getId()).findAny();
            if (!ex.isPresent()) {
                log.info("Attribute {} is not present in RIT.", e.getId());
            }
            if (!ex.get().getDescription().equals(e.getShortDescription())) {
                log.info("Attribute {} has different descriptions in RIT and ECCAIRS {} : {}",
                    e.getId(), ex.get().getDescription(), e.getShortDescription());
            }
        }

        // compare valuelists
        for (ValueList e : rit.getValueLists().values()) {
            Optional<RitAttribute> rex = rit.getAttributes().values()
                .stream()
                .filter(ee -> (ee.getValueListId() + "").equals(e.getId()))
                .findAny();
            if (!rex.isPresent()) {
                log.info("ValueList {} not bound to a RIT attribute.", e.getId());
                continue;
            }

            String valueListId = e.getId().substring(2, e.getId().indexOf("_"));
            Optional<EccairsAttribute> ex = EccairsTaxonomyUtils.attributes(eccairs)
                .stream()
                .filter(ee -> ee.getId() == (rex.get().getId()))
                .findAny();
            if (!ex.isPresent()) {
                log.info("ValueList {} not bound to an Eccairs attribute.", e.getId());
                continue;
            }

            List<Integer> list =
                e.getValues().stream().map(Value::getValueId).collect(Collectors.toList());
            List<EccairsValue> values = getValues(ex.get());
            List<Integer> valueIds =
                values.stream().map(v -> v.getId()).collect(Collectors.toList());
            if (!list.equals(valueIds)) {
                final List<Integer> ritNotEccairs = new ArrayList<>(list);
                ritNotEccairs.sort(Integer::compareTo);
                ritNotEccairs.removeAll(valueIds);
                final List<Integer> eccairsNotRit = new ArrayList<>(valueIds);
                eccairsNotRit.removeAll(list);
                log.error("RIT Valuelist {} for attribute {} differs from the Eccairs one",
                    valueListId, (ex.get().getId() + "-" + ex.get().getDescription()));
                log.error("   - RIT: {}", ritNotEccairs);
                log.error("   - ECC: {}", eccairsNotRit.stream()
                    .map(exe -> values.stream().filter(x -> x.getId() == exe).findAny().get())
                    .map(x -> x.getId() + "-" + x.getDescription()).collect(Collectors.toList()));
            }
            ;
        }
    }

    private static List<EccairsValue> getValues(EccairsAttribute a) {
        final List<EccairsValue> list = new ArrayList<>();
        if (a.getValues() != null) {
            a.getValues().forEach(v ->
                getValues(v, list)
            );
        }
        return list;
    }

    private static void getValues(EccairsValue a, List<EccairsValue> list) {
        list.add(a);
        if (a.getValues() != null) {
            a.getValues().forEach(v -> {
                getValues(v, list);
            });
        }
    }

}