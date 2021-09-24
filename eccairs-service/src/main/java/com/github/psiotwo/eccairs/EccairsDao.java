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
     * @param taxonomyNameAndVersion name and version of the ECCAIRS taxonomy, e.g. eccairs-aviation-3.4.0.2
     * @return true if the taxonomy is present in the repository
     */
    boolean eccairsTaxonomyExists(final String taxonomyNameAndVersion);
}
