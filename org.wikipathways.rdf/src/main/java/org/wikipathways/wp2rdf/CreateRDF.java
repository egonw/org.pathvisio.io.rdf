// Copyright 2022 Egon Willighagen
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.wikipathways.wp2rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.jena.rdf.model.Model;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.PathwayModel;

public class CreateRDF {

	public static void main(String[] args) throws Exception {
		final Options options = new Options();
		options.addOption(new Option("h", "help", false, "Display the help information."));
		options.addOption(new Option("r", "revision", true, "Revision of the pathway."));
		options.addOption(new Option("d", "domain", true, "Domain name to use for the Resource IRIs."));

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		
		if (cmd.hasOption("h") || args.length < 4) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("CreateRDF [GPML] [GPMLRDF_FOLDER] [WPRDF_FOLDER] [VERSION]", options);
			System.exit(0);
		}

		args = cmd.getArgs();
        String gpmlFile = args[0];
        String gpmlrdfFolder  = args[1];
        String wprdfFolder  = args[2];
        String version  = args[3];
        String prefix = "WP";
        int index = gpmlFile.lastIndexOf("WP");
        if (index == -1 ) {
            index = gpmlFile.indexOf("PC");
            prefix = "PC";
		}
        String localFile = gpmlFile.substring(index);
        String wpid      = localFile.substring(0,localFile.indexOf("."));

        String gpmlrdfFile  = gpmlrdfFolder + prefix + wpid + ".ttl";
        String wprdfFile  = wprdfFolder + prefix + wpid + ".ttl";

        DataSourceTxt.init();
        DataSource wpSource = DataSource.register("Wp", "WikiPathways").asDataSource();

        PathwayModel pathway = new PathwayModel();
		InputStream gpmlStream = new FileInputStream(new File(gpmlFile)); 
		pathway.readFromXml(gpmlStream, false);
		
		pathway.getPathway().setXref(new Xref(wpid, wpSource));
		if (cmd.hasOption('r')) pathway.getPathway().setVersion(cmd.getOptionValue('r'));

		String baseIRI = null;
		if (cmd.hasOption('d')) {
			baseIRI = "http://" + cmd.getOptionValue('d');
		} else {
			baseIRI = Utils.WP_RDF_URL;
		}

		// generate the GPMLRDF content
		try {
			Model model = new org.pathvisio.io.rdf.gpml.Convertor(pathway, baseIRI).asRDF();

			// serialize RDF
			model.setNsPrefix("gpml", "http://vocabularies.wikipathways.org/gpml#");
			model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
			FileOutputStream output = new FileOutputStream(gpmlrdfFile);
			model.write(output, "TURTLE");
			output.flush();
			output.close();
		} catch (Exception exception) {
			// skip
			System.out.println("Error while creating GPMLRDF for " + wpid + ": " + exception.getMessage());
		}
		
		// generate the WPRDF content
		try {
			Model model = new org.pathvisio.io.rdf.wp.Convertor(pathway).asRDF();

			// serialize RDF
			model.setNsPrefix("biopax", "http://www.biopax.org/release/biopax-level3.owl#");
			model.setNsPrefix("cito", "http://purl.org/spar/cito/");
			model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
			model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
			model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
			model.setNsPrefix("freq", "http://purl.org/cld/freq/");
			model.setNsPrefix("gpml", "http://vocabularies.wikipathways.org/gpml#");
			model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
			model.setNsPrefix("pav", "http://purl.org/pav/");
			model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
			model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
			model.setNsPrefix("void", "http://rdfs.org/ns/void#");
			model.setNsPrefix("wp", "http://vocabularies.wikipathways.org/wp#");
			model.setNsPrefix("wprdf", "http://rdf.wikipathways.org/");
			model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
			FileOutputStream output = new FileOutputStream(wprdfFile);
			model.write(output, "TURTLE");
			output.flush();
			output.close();
		} catch (Exception exception) {
			// skip
			System.out.println("Error while creating WPRDF for " + wpid + ": " + exception.getMessage());
		}
	}
	
}
