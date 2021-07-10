package com.github.psiotwo.eccairs.core;

import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EccairsTaxonomyParserTest {

    @Test
    void testParseSuceedsForValidTaxonomy() {
        final InputStream inputStream = getClass().getResourceAsStream("/ECCAIRS Aviation v.4.1.0.7-sample.xml");
        Assertions.assertNotNull(new EccairsTaxonomyParser().parse(inputStream));
    }
}