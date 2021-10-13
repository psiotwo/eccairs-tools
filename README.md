# ECCAIRS tools

This project provides tools for manipulating [ECCAIRS](https://eccairsportal.jrc.ec.europa.eu/index.php?id=2)  taxonomies. 

In particular it serves to:
- parse ECCAIRS Taxonomy distribution
- parse RIT Taxonomy distribution
- comparison of ECCAIRS and RIT versions
- (experimental) export of ECCAIRS into SNOMED format 

## Import ECCAIRS taxonomy into an RDF repository
1. Build the project using `gradle build`
2. Run the `eccairs-service` as ` ECCAIRS_SPARQLQUERYENDPOINT=<RDF4J_SERVER>/repositories/eccairs-aviation-<ECCAIRS_VERSION> ECCAIRS_SPARQLGSPENDPOINTTEMPLATE=<RDF4j_SERVER>/repositories/eccairs-aviation-<ECCAIRS_VERSION>/rdf-graphs/service?graph= java -jar eccairs-service-1.0-SNAPSHOT.jar` where `<ECCAIRS_VERSION>` is the version of the ECCAIRS taxonomy, e.g. `4.1.0.7` and `<RDF4J_SERVER>` is the URL of the RDF4J server. 
3. send the ECCAIRS taxonomy file as `curl --location --request POST 'http://localhost:18080/eccairs-service/taxonomy' --form 'taxonomyFile=@"ECCAIRS Aviation v.4.1.0.7.xml"'`
