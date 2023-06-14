package com.github.psiotwo.eccairs;

import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.rdf.EccairsTaxonomyToRdf;
import com.github.psiotwo.eccairs.rdf.EccairsUtils;
import java.util.ArrayList;
import java.util.List;

import com.github.psiotwo.eccairs.rdf.TaxonomyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JenaEccairsDao implements EccairsDao {

    private final Conf conf;

    private final TaxonomyService taxonomyService;

    public JenaEccairsDao(final Conf conf, TaxonomyService taxonomyService) {
        this.conf = conf;
        this.taxonomyService = taxonomyService;
    }

    public void saveEccairs(EccairsDictionary dictionary) {
        log.info("Saving: '{}, ver. {}'", dictionary.getTaxonomy(), dictionary.getVersion());

        final EccairsTaxonomyToRdf exporter = new EccairsTaxonomyToRdf(conf.getBaseUri(), dictionary, taxonomyService);
        final Dataset dataset = exporter.transform();
        log.info("- taxonomy file parsed.");

        dataset.listNames().forEachRemaining(n -> {
            final RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
                .destination(conf.getSparqlQueryEndpoint())
                .gspEndpoint(conf.getSparqlGspEndpointTemplate() + n);
            try (RDFConnection conn = builder.build()) {
                log.info(" Saving {}", n);
                conn.put(dataset.getNamedModel(n));
            }
        });

        final List<String> namedGraphs = new ArrayList<>();
        dataset.listNames().forEachRemaining(namedGraphs::add);

        log.info("- taxonomy uploaded to graphs {}", namedGraphs);
    }

    public boolean eccairsTaxonomyExists(final String taxonomyName, final String taxonomyVersion) {
        final String graphUrl =
            EccairsUtils.getVersionedOntologyUrl(conf.getBaseUri(), taxonomyName, taxonomyVersion);
        try (RDFConnection conn = RDFConnectionFactory.connect(conf.getSparqlQueryEndpoint())) {
            final ParameterizedSparqlString query = new ParameterizedSparqlString();
            query.setCommandText("ASK { GRAPH ?g { ?s ?p ?o } }");
            query.setIri("g", graphUrl);
            final QueryExecution e = conn.query(query.asQuery());
            return e.execAsk();
        }
    }
}
