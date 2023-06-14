package com.github.psiotwo.eccairs.rdf;

import com.github.psiotwo.eccairs.core.model.EccairsValue;

import java.util.List;

/**
 * Provides access to the ECCAIRS taxonomy service.
 */
public interface TaxonomyService {

    /**
     * Checks whether an attribute with the specified ECCAIRS ID has a hierarchical value list.
     *
     * @param attributeId Attribute ECCAIRS ID
     * @return {@code} true when attribute value list has more than one level, {@code false} otherwise
     */
    boolean hasHierarchicalValueList(int attributeId);

    /**
     * Gets a presumably hierarchical value list of an attribute with the specified ECCAIRS ID.
     *
     * @param attributeId Attribute ECCAIRS ID
     * @return List of top level attribute values
     */
    List<EccairsValue> getValueList(int attributeId);
}
