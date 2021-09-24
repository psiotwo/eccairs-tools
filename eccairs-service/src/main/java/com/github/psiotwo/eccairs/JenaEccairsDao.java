package com.github.psiotwo.eccairs;

import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import com.github.psiotwo.eccairs.rdf.EccairsTaxonomyToRdf;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntModel;
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

    private Conf conf;

    @Autowired
    public JenaEccairsDao( final Conf conf ) {
        this.conf = conf;
    }

    public void saveEccairs(EccairsDictionary dictionary) {
        log.info("Saving: '{}, ver. {}'", dictionary.getTaxonomy(), dictionary.getVersion());
        final String graphUrl = getGraphUrl(dictionary.getTaxonomy(), dictionary.getVersion());

        final EccairsTaxonomyToRdf exporter = new EccairsTaxonomyToRdf(graphUrl, dictionary);
        final OntModel model = exporter.transform();
        log.info("- taxonomy file parsed.");

        final RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
            .destination(conf.getSparqlQueryEndpoint())
            .gspEndpoint(conf.getSparqlGspEndpointTemplate() + graphUrl);

        try (RDFConnection conn = builder.build()) {
            conn.put(model);
        }
        log.info("- taxonomy uploaded to graph {}", graphUrl);
    }

    private String getGraphUrl(final String taxonomyName, final String taxonomyVersion) {
        return conf.getBaseUri() + taxonomyName.toLowerCase(Locale.ROOT).replace(" ", "-")
            + "-" + taxonomyVersion;
    }

    public boolean eccairsTaxonomyExists(final String taxonomyNameAndVersion) {
        final String graphUrl = conf.getBaseUri() + taxonomyNameAndVersion;
        try (RDFConnection conn = RDFConnectionFactory.connect(conf.getSparqlQueryEndpoint())) {
            final ParameterizedSparqlString query = new ParameterizedSparqlString();
            query.setCommandText("ASK { GRAPH ?g { ?s ?p ?o } }");
            query.setIri("g", graphUrl);
            final QueryExecution e = conn.query(query.asQuery());
            return e.execAsk();
        }
    }
}
