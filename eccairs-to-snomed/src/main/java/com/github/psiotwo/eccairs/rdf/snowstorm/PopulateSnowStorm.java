package com.github.psiotwo.eccairs.rdf.snowstorm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.psiotwo.eccairs.rdf.PopulateSnomedServer;

/**
 * This script populates moves data from ECCAIRS distribution to SnowOwl and exports them
 * to SnowStorm.
 */
public class PopulateSnowStorm {
    public static void main(String[] args) throws JsonProcessingException {
//        final PopulateSnowowl pso = new PopulateSnowowl(args[0], args[1], args[2]);

        final SnowstormApi snowstorm = new SnowstormApi(args[0]);
//        final PopulateSnomedServer pop =
        new PopulateSnomedServer(snowstorm, args[1], args[2]);
//
//        snowstorm.createBranch("MAIN",args[1]);
//        File f = new File(args[3]);
//        snowstorm.importData("MAIN/" + args[1], f);
    }
}