package com.github.psiotwo.eccairs.core;

import static org.junit.jupiter.api.Assertions.*;


import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EccairsTaxonomyUtilsTest {

    @Test
    void testRenderValuesCorrectlyRendersValuesForIds() {
        final List<EccairsValue> selectedValues = new ArrayList<>();
        selectedValues.add(new EccairsValue().setId(1).setDescription("D1"));
        selectedValues.add(new EccairsValue().setId(5).setDescription("D5"));

        final List<EccairsValue> values = new ArrayList<>(selectedValues);
        values.add(new EccairsValue().setId(2));
        values.add(new EccairsValue().setId(12));

        assertEquals(Arrays.asList("1 - D1", "5 - D5"), EccairsTaxonomyUtils.renderValues(values, Arrays.asList(1,5)));
    }

    @Test
    void testGetValuesReturnsCorrectlyNestedValues() {
        final List<EccairsValue> rootValues = new ArrayList<>();
        final EccairsValue v1 = new EccairsValue().setId(1);
        rootValues.add(v1);
        rootValues.add(new EccairsValue().setId(2));

        final List<EccairsValue> v1Values = new ArrayList<>();
        v1Values.add(new EccairsValue().setId(3));
        v1.setValues(v1Values);

        final EccairsAttribute attribute = new EccairsAttribute();
        attribute.setValues(rootValues);

        final Set<EccairsValue> values = new HashSet<>(rootValues);
        values.addAll(v1Values);

        assertEquals(values, new HashSet<>(EccairsTaxonomyUtils.getValues(attribute)));
    }
}