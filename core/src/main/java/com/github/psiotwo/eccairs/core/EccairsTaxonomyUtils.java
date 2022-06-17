package com.github.psiotwo.eccairs.core;

import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import com.github.psiotwo.eccairs.core.model.EccairsTerm;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EccairsTaxonomyUtils {

    /**
     * Lists all ECCAIRS entities in the dictionary.
     *
     * @param dictionary dictionary to list entities from
     * @return list of entities
     */
    public static List<EccairsEntity> entities(EccairsDictionary dictionary) {
        final List<EccairsEntity> set = new ArrayList<>();
        dictionary.getEntities().forEach(e ->
            entities(e, set)
        );
        return set;
    }

    private static void entities(EccairsEntity e, List<EccairsEntity> set) {
        if (e.getIsLink() == null) {
            set.add(e);
        }
        if (e.getEntities() != null) {
            e.getEntities().forEach(c ->
                entities(c, set));
        }
    }

    public static EccairsEntity getEntityForId(final EccairsDictionary d, final int id) {
        return entities(d).stream().filter( e -> e.getId() == id ).findFirst().orElse(null);
    }

    /**
     * Lists all ECCAIRS attributes in all entities in the dictionary.
     *
     * @param dictionary dictionary to analyse
     * @return list of attributes
     */
    public static List<EccairsAttribute> attributes(EccairsDictionary dictionary) {
        return entities(dictionary)
            .stream()
            .filter(e -> e.getAttributes() != null)
            .flatMap(e -> e.getAttributes().stream())
            .collect(Collectors.toList());
    }

    /**
     * Prints basic statistics about an ECCAIRS dictionary.
     *
     * @param dictionary ECCAIRS dictionary
     * @param os         PrintStream to write to
     */
    public static void statistics(final EccairsDictionary dictionary, final PrintStream os) {
        os.println(dictionary.toString());

        final List<EccairsEntity> entities = EccairsTaxonomyUtils.entities(dictionary);
        printEccairsTermListStatistics(entities, "entities", os);

        List<EccairsAttribute> attributes = EccairsTaxonomyUtils.attributes(dictionary);
        printEccairsTermListStatistics(attributes, "attributes", os);
    }

    private static <T extends EccairsTerm> void printEccairsTermListStatistics(
        final List<T> terms,
        final String pluralLabel,
        final PrintStream os) {
        os.println(" - # " + pluralLabel + " = " + terms.size());
        os.println(
            " - # distinct " + pluralLabel + " = " + terms.stream().map(EccairsTerm::getId).distinct()
                .count());
        os.println(terms.stream()
            .sorted(Comparator.comparingInt(EccairsTerm::getId))
            .map(e -> "        - " + e.getId() + " - " + e.getDescription())
            .collect(Collectors.joining("\n")));
    }

    /**
     * Returns attribute list of all values (recursively) subordinated to the attribute.
     *
     * @param attribute ECCAIRS attribute
     * @return list of values
     */
    public static List<EccairsValue> getValues(final EccairsAttribute attribute) {
        final List<EccairsValue> list = new ArrayList<>();
        if (attribute.getValues() != null) {
            attribute.getValues().forEach(v -> getValues(v, list));
        }
        return list;
    }

    private static void getValues(final EccairsValue a, final List<EccairsValue> list) {
        list.add(a);
        if (a.getValues() != null) {
            a.getValues().forEach(v -> getValues(v, list));
        }
    }

    public static List<String> renderValues(final List<EccairsValue> values,
                                            final List<Integer> valueIds) {
        return
            values
                .stream().filter(value -> valueIds.contains(value.getId()))
                .map(v -> v.getId() + " - " + v.getDescription())
                .collect(Collectors.toList());
    }

    public static String getTaxonomyName(final String taxonomyValue) {
        return taxonomyValue.replaceAll("([\\d]+[.])*[\\d]+","").trim();
    }
}