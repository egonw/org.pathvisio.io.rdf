package org.pathvisio.io.rdf.wp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.junit.Test;
import org.pathvisio.libgpml.io.ConverterException;
import org.pathvisio.libgpml.model.PathwayModel;

public class ConvertorTest {

	@Test
	public void convertGpml() throws ConverterException {
		Convertor convertor = new Convertor(null);

		// read the pathway
		PathwayModel pathway = new PathwayModel();
		InputStream gpmlStream = getClass().getResourceAsStream("/WP4846.gpml"); 
		pathway.readFromXml(gpmlStream, true);

		// convert the content
		DataSource wpSource = DataSource.register("Wp", "WikiPathways").asDataSource();
		pathway.getPathway().setXref(new Xref("WP4846", wpSource));
		Model model = convertor.convertWp(pathway);

		// serialize RDF
		model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
		model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
		model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
		model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		model.setNsPrefix("wp", "http://vocabularies.wikipathways.org/wp#");
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		model.write(output, "TURTLE");
		System.out.println(new String(output.toByteArray()));
	}
	
}
