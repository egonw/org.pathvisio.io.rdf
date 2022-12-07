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
package org.pathvisio.io.rdf.wp;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.bridgedb.IDMapperStack;
import org.pathvisio.io.rdf.ontologies.Wp;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.PathwayModel;

/**
 * Tool to convert a {@link Pathway} model into a Jena RDF model.
 */
public class Convertor {

	PathwayModel pathway;
	DataNodeConvertor dataNodeConvertor;
	IDMapperStack mapper;

	// cached things
	Resource pwyRes;

	public Convertor(PathwayModel pathway) throws Exception {
		this.pathway = pathway;
		dataNodeConvertor = new DataNodeConvertor(this);
		mapper = maps();
	}

	public Model asRDF() {
		Model model = ModelFactory.createDefaultModel();

		// pathway
		pwyRes = generatePathwayResource(pathway.getPathway(), model);
		generateDataNodeResources(pathway.getDataNodes(), model);
		
		return model;
	}

	private void generateDataNodeResources(List<DataNode> dataNodes, Model model) {
		for (DataNode node : dataNodes) {
			dataNodeConvertor.convertDataNode(node, model, mapper);
		}
	}

	private Resource generatePathwayResource(Pathway pathway, Model model) {
		String wpId = pathway.getXref().getId();
		String revision = pathway.getVersion().trim().replaceAll(" ", "_");

		Resource pwyRes = model.createResource(Utils.IDENTIFIERS_ORG_URL + "/wikipathways/" + wpId + "_r" + revision);
		pwyRes.addProperty(RDF.type, Wp.Pathway);
		pwyRes.addProperty(RDF.type, SKOS.Collection);
		pwyRes.addProperty(DC_11.identifier, model.createResource(Utils.IDENTIFIERS_ORG_URL + "/wikipathways/" + wpId));
		pwyRes.addLiteral(DC_11.source, "WikiPathways");
		pwyRes.addLiteral(DCTerms.identifier, wpId);
		pwyRes.addLiteral(DC_11.title, model.createLiteral(pathway.getTitle(), "en"));
		pwyRes.addLiteral(DCTerms.description, pathway.getDescription());
		pwyRes.addLiteral(Wp.organismName, pathway.getOrganism());
		pwyRes.addProperty(Wp.isAbout, model.createResource(Utils.WP_RDF_URL + "/Pathway/" + wpId + "_r" + revision));
		pwyRes.addProperty(FOAF.page, model.createResource("http://www.wikipathways.org/instance/" + wpId + "_r" + revision));
 
		return pwyRes;
	}

	private static IDMapperStack maps() throws Exception {
		final Properties prop = new Properties();
		String derbyFolder = "/tmp/" + System.getProperty("OPSBRIDGEDB", "OPSBRIDGEDB");
		if (new File(derbyFolder).exists()) {
  	        prop.load(new FileInputStream(derbyFolder + "/config.properties"));
		    IDMapperStack mapper = IdentifierConvertor.createBridgeDbMapper(prop);
		    return mapper;
		} else {
			System.out.println("WARN: BridgeDb config file folder does not exist: " + derbyFolder);
		}
		return new IDMapperStack();
	}
}
