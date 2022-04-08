// Copyright (c) 2015 BiGCaT Bioinformatics
//               2022 Egon Willighagen <egon.willighagen@gmail.com>
// 
// Conversion from GPML pathways to RDF
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
package org.pathvisio.io.rdf.gpml;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.pathvisio.io.rdf.ontologies.Gpml;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.PathwayModel;

/**
 * Tool to convert a {@link Pathway} model into a Jena RDF model.
 */
public class Convertor {

	public static Model convertGpml(PathwayModel pathway) {
		Model model = ModelFactory.createDefaultModel();
		parsePathwayInfoGpml(pathway.getPathway(), model);
		return model;
	}

	/**
	 * conversion only GPML vocabulary
	 */
	public static Resource parsePathwayInfoGpml(Pathway p, Model model) {
		String wpId = p.getXref().getId();
		String revision = p.getVersion();

		Resource pwyRes = model.createResource(Utils.WP_RDF_URL + "/Pathway/" + wpId + "_r" + revision.trim().replaceAll(" ", "_"));
		pwyRes.addProperty(RDFS.seeAlso, model.createResource("https://www.wikipathways.org/instance/" + wpId + "_r" + revision.trim().replaceAll(" ", "_")));

		// FIXME: 
		//if (tags.contains("Curation:AnalysisCollection")) {
		//	pwyRes.addProperty(RDFS.seeAlso, model.createResource("https://scholia.toolforge.org/wikipathways/" + wpId));
		//}

		// Required Attributes
		pwyRes.addLiteral(Gpml.ORGANISM, p.getOrganism());
		pwyRes.addLiteral(Gpml.BOARD_HEIGHT, p.getBoardHeight());
		pwyRes.addLiteral(Gpml.BOARD_WIDTH, p.getBoardWidth());
		pwyRes.addLiteral(Gpml.NAME, p.getTitle());
		
		// Optional Attributes
		if(p.getVersion() != null) pwyRes.addLiteral(Gpml.VERSION, p.getVersion());
		// FIXME: if(p.getCopyright() != null) pwyRes.addLiteral(Gpml.LICENSE, p.getCopyright());
		// FIXME: if(p.getAuthor() != null) pwyRes.addLiteral(Gpml.AUTHOR, p.getAuthor());
		// FIXME: if(p.getEmail() != null) pwyRes.addLiteral(Gpml.EMAIL, p.getEmail());
		// FIXME: if(p.getMaintainer() != null) pwyRes.addLiteral(Gpml.MAINTAINER, p.getMaintainer());
		// FIXME: if(p.getLastModified() != null) pwyRes.addLiteral(Gpml.LAST_MODIFIED, p.getLastModified());
		// FIXME: if(p.getMapInfoDataSource() != null) pwyRes.addLiteral(Gpml.DATA_SOURCE, p.getMapInfoDataSource());
		
		// FIXME:
		//for(String s : p.getBiopaxRefs()) {
		//	pwyRes.addLiteral(Gpml.BIOPAX_REF, s);
		//}
		return pwyRes;
	}

}
