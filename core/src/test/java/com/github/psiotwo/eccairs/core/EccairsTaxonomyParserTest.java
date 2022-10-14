package com.github.psiotwo.eccairs.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EccairsTaxonomyParserTest {

    @Test
    void testParseSuceedsForValidTaxonomy() throws IOException {
        try (final InputStream inputStream = getClass().getResourceAsStream(
            "/ECCAIRS Aviation v.4.1.0.7-sample.xml")) {
            Assertions.assertNotNull(new EccairsTaxonomyParser().parse(inputStream));
        }
    }

    @Test
    void testParseTrimsDescription() throws IOException {
        try (final InputStream inputStream = getClass().getResourceAsStream(
                "/ECCAIRS Aviation v.4.1.0.7-sample.xml")) {
            final EccairsDictionary dictionary = new EccairsTaxonomyParser().parse(inputStream);
            Assertions.assertEquals(1, dictionary.getEntities().size());
            final EccairsEntity entity = dictionary.getEntities().get(0);
            Assertions.assertEquals(4, entity.getAttributes().size());
            final Optional<EccairsAttribute> attributeO = entity.getAttributes().stream().filter(a -> a.getId() == 127).findAny();
            if (attributeO.isEmpty()) {
                Assertions.fail("No attribute with ID=127 found");
            }
            final EccairsAttribute attribute = attributeO.get();
            Assertions.assertEquals(3, attribute.getValues().size());
            final EccairsValue value = attribute.getValues().get(2);

            Assertions.assertEquals("Unknown", value.getDescription());
        }
    }
}