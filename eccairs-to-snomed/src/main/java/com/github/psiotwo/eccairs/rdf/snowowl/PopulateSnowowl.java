package com.github.psiotwo.eccairs.rdf.snowowl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.psiotwo.eccairs.rdf.PopulateSnomedServer;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopulateSnowowl {

    public static void main(String[] args) throws UnirestException, JsonProcessingException {
        final SnowowlApi api = new SnowowlApi(args[0]);
        new PopulateSnomedServer(api, args[1], args[2]);
    }
}
