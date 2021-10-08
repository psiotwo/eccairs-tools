package com.github.psiotwo.eccairs.rdf;

import java.util.Locale;

public class EccairsUtils {

    /**
     * Returns URL of the ontology, given baseUri, taxonomy name and taxonomy version.
     *
     * @param baseUrl base URL of the ontology
     * @param taxonomyName name of the ontology.
     * @param taxonomyVersion version of the ontology.
     * @return URL of the ontology.
     */
    public static String getOntologyUrl(final String baseUrl,
                                        final String taxonomyName,
                                        final String taxonomyVersion) {
        return baseUrl
            + taxonomyName.toLowerCase(Locale.ROOT).replace(" ", "/")
            + "-" + taxonomyVersion;
    }
}
