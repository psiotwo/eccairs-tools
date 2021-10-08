package com.github.psiotwo.eccairs;

import com.github.psiotwo.eccairs.core.EccairsTaxonomyParser;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EccairsService {

    final EccairsDao dao;

    @Autowired
    public EccairsService(final EccairsDao dao) {
        this.dao = dao;
    }

    /**
     * Parses and imports ECCAIRS taxonomy.
     *
     * @param inputStream source of the taxonomy
     * @return parsed taxonomy
     */
    public EccairsDictionary importEccairs(final InputStream inputStream) {
        final EccairsTaxonomyParser parser = new EccairsTaxonomyParser();
        final EccairsDictionary dictionary = parser.parse(inputStream);
        dao.saveEccairs(dictionary);
        return dictionary;
    }

    /**
     * Checks whether ECCAIRS taxonomy exists.
     *
     * @param taxonomyName name of the ECCAIRS taxonomy
     * @param taxonomyVersion version of the ECCAIRS taxonomy
     * @return true if the taxonomy exists, false otherwise
     */
    public boolean eccairsTaxonomyExists(final String taxonomyName, final String taxonomyVersion) {
        return dao.eccairsTaxonomyExists(taxonomyName, taxonomyVersion);
    }
}
