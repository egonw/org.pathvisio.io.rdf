[![build](https://github.com/egonw/org.pathvisio.io.rdf/actions/workflows/maven.yml/badge.svg)](https://github.com/egonw/org.pathvisio.io.rdf/actions/workflows/maven.yml)

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

Or generate them in one go with:

```
cd org.wikipathways.rdf
java -cp org.wikipathways.rdf/target/gpml2rdf-4.0.3.jar org.wikipathways.wp2rdf.CreateRDF WP1028.gpml WP1028.gpml.ttl WP1028.wp.ttl
```

### Using ROBOT to find differences

```
./robot diff --left WP1028.wp.real.ttl --right WP1028.wp.ttl
```


## Release alpha versions

This code is nowhere close to be replacing the current GPMLRDF, but a 4.0.0 series will
be released as alpha to start putting together the full toolchain, which requires testing
the whole workflow in action.

Version are updated with:

```
mvn versions:set -DnewVersion=4.0.0
git commit -m "New release" -a
```

