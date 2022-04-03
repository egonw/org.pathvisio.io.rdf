package org.pathvisio.io.rdf.gpml;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.pathvisio.libgpml.model.PathwayModel;

/**
 * Tool to convert a {@link Pathway} model into a Jena RDF model.
 */
public class Convertor {

	public static Model convertGpml(PathwayModel pathway) {
		Model model = ModelFactory.createDefaultModel();
		return model;
	}
	
}
