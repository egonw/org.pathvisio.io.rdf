# org.pathvisio.io.rdf

Library for saving GPMLRDF and WPRDF for a GPML data model. It consists of two parts,
a libGPML extension to convert GPML into RDF, and a command line tool that allows one
to do just that from the command line.

## Installing

In this directory, follow the following instructions:

```
cd org.pathvisio.io.rdf
mvn clean install
```

And

```
cd org.wikipathways.rdf
mvn clean install
```

## Creating WPRDF and GPMLRDF

```
cd org.wikipathways.rdf
java -cp target/gpml2rdf-4.0.0-SNAPSHOT.jar org.wikipathways.wp2rdf.CreateGPMLRDF path/to/WP4297.gpml WP4297.ttl
java -cp target/gpml2rdf-4.0.0-SNAPSHOT.jar org.wikipathways.wp2rdf.CreateWPRDF path/to/WP4297.gpml WP4297.ttl
```
