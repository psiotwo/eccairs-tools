package com.github.psiotwo.eccairs.rdf;

import java.util.HashMap;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Map;

public class EccairsUtils {

    private static final Map<String,String> languages = new HashMap<>();
    static {
        languages.put("English", "en");
        languages.put("Czech", "cs");
    }


    /**
     * Returns URL of the ontology, given baseUri, taxonomy name and taxonomy version.
     *
     * @param baseUrl         base URL of the ontology
     * @param taxonomyName    name of the ontology.
     * @param taxonomyVersion version of the ontology.
     * @return URL of the ontology.
     */
    public static String getVersionedOntologyUrl(final String baseUrl,
                                                 final String taxonomyName,
                                                 final String taxonomyVersion) {
        return getOntologyUrl(baseUrl, taxonomyName) + "-" + taxonomyVersion;
    }

    /**
     * Returns version-agnostic URL of the ontology, given baseUri and taxonomy name.
     *
     * @param baseUrl      base URL of the ontology
     * @param taxonomyName name of the ontology.
     * @return URL of the ontology.
     */
    public static String getOntologyUrl(final String baseUrl,
                                        final String taxonomyName) {
        return baseUrl
            + taxonomyName.toLowerCase(Locale.ROOT).replace(" ", "/");
    }

    /**
     * Maps an ECCAIRS language code to the language tag.
     *
     * @param eccairsLanguage ECCAIRS language string
     * @return RDF language tag
     */
    public static String getLangCodeForEccairsLanguage(final String eccairsLanguage) {
        return languages.get(eccairsLanguage);
    }

    /**
     * Generates an ontology prefix based on the dictionary name and version.
     *
     * @return ontology prefix, e.g. e-a-3_4_0_2
     */
    public static String generateOntologyPrefix(final EccairsDictionary dictionary) {
        return Arrays.stream(dictionary.getTaxonomy().split(" "))
            .map(c -> c.toLowerCase(Locale.ROOT).charAt(0) + "")
            .collect(Collectors.joining("-"))
            + "-" + dictionary.getVersion().replace(".", "_");
    }
}
