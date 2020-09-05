package com.github.psiotwo.eccairs.snomed.snowowl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.psiotwo.eccairs.core.EccairsTaxonomyParser;
import com.github.psiotwo.eccairs.core.model.EccairsAttribute;
import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.core.model.EccairsEntity;
import com.github.psiotwo.eccairs.core.model.EccairsTerm;
import com.github.psiotwo.eccairs.core.model.EccairsValue;
import com.github.psiotwo.eccairs.snomed.PopulateSnomedServer;
import com.github.psiotwo.eccairs.snomed.SnomedConstants;
import com.github.psiotwo.eccairs.snomed.SnomedCtStoreApi;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopulateSnowowl {

    public static void main(String[] args) throws UnirestException, JsonProcessingException {
        final SnowowlApi api = new SnowowlApi(args[0]);
        new PopulateSnomedServer(api, args[1], args[2]);
    }
}
