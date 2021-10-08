package com.github.psiotwo.eccairs;

import com.github.psiotwo.eccairs.core.model.EccairsDictionary;

public interface EccairsDao {

    /**
     * Saves ECCAIRS dictionary to the repository.
     *
     * @param dictionary dictionary to save
     */
    void saveEccairs(EccairsDictionary dictionary);

    /**
     * Checks whether the taxonomy with the given name and version exists.
     *
     * @param taxonomyName name of the ECCAIRS taxonomy, e.g. aviation
     * @param taxonomyVersion version of the ECCAIRS taxonomy, e.g. 3.4.0.2
     * @return true if the taxonomy is present in the repository
     */
    boolean eccairsTaxonomyExists(final String taxonomyName, final String taxonomyVersion);
}
