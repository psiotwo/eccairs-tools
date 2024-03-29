package com.github.psiotwo.eccairs.rdf;

public class SnomedEccairsConstants {


    static final long ENTITY = 121000250102L;
    static final long VALUE = 141000250109L;
    static final long HAS_SUB_ENTITY = 1121000250103L;
    // correct is : 1931000250104L
    // now abusing 1142142004 has pack size, because SnowStorm does not allow us to create a MRCM
    static final long HAS_ID = 1142142004l;
    // ECCAIRS module
    static final long ECCAIRS_MODULE_ID = 21000250107L;
    static final long ECCAIRS_MODULE_ORGANIZATION_EXTENSION_ID = 31000250109L;

    private SnomedEccairsConstants() {
    }
}