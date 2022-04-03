package org.pathvisio.io.rdf.gpml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.pathvisio.libgpml.io.ConverterException;
import org.pathvisio.libgpml.model.PathwayModel;

public class ConvertorTest {

	@Test
	public void convertGpml() throws ConverterException {
		PathwayModel pathway = new PathwayModel();
		InputStream gpmlStream = getClass().getResourceAsStream("/WP4846.gpml"); 
		pathway.readFromXml(gpmlStream, true);
		Model model = Convertor.convertGpml(pathway);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		model.write(output, "TURTLE");
		System.out.println(new String(output.toByteArray()));
	}
	
}
