package org.pathvisio.io.rdf.gpml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.junit.Test;
import org.pathvisio.libgpml.model.PathwayModel;

public class ConvertorTest {

	@Test
	public void convertGpml() throws Exception {
		// read the pathway
		PathwayModel pathway = new PathwayModel();
		InputStream gpmlStream = getClass().getResourceAsStream("/WP4846.gpml"); 
		pathway.readFromXml(gpmlStream, true);

		// convert the content
		DataSource wpSource = DataSource.register("Wp", "WikiPathways").asDataSource();
		pathway.getPathway().setXref(new Xref("WP4846", wpSource));
		Model model = new Convertor(pathway).asRDF();

		// serialize RDF
		model.setNsPrefix("gpml", "http://vocabularies.wikipathways.org/gpml#");
		model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		model.write(output, "TURTLE");
		System.out.println(new String(output.toByteArray()));
	}
	
}
