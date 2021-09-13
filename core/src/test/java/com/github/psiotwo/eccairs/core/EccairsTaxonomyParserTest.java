package com.github.psiotwo.eccairs.core;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EccairsTaxonomyParserTest {

    @Test
    void testParseSuceedsForValidTaxonomy() throws IOException {
        try(final InputStream inputStream = getClass().getResourceAsStream("/ECCAIRS Aviation v.4.1.0.7-sample.xml")) {
            Assertions.assertNotNull(new EccairsTaxonomyParser().parse(inputStream));
        }
    }
}