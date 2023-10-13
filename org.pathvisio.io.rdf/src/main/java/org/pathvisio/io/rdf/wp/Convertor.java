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

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.bridgedb.IDMapperStack;
import org.bridgedb.bio.Organism;
import org.pathvisio.io.rdf.ontologies.Pav;
import org.pathvisio.io.rdf.ontologies.Wp;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.PathwayModel;

/**
 * Tool to convert a {@link Pathway} model into a Jena RDF model.
 */
public class Convertor {

	PathwayModel pathway;
	DataNodeConvertor dataNodeConvertor;
	InteractionConvertor interactionConvertor;
	IDMapperStack mapper;

	// cached things
	Resource pwyRes;

	public Convertor(PathwayModel pathway) throws Exception {
		this.pathway = pathway;
		dataNodeConvertor = new DataNodeConvertor(this, mapper);
		interactionConvertor = new InteractionConvertor(this, mapper);
	}

	public Model asRDF() {
		Model model = ModelFactory.createDefaultModel();

		// pathway
		pwyRes = generatePathwayResource(model);
		generateDataNodeResources(pathway.getDataNodes(), model);
		generateInteractionResources(pathway.getInteractions(), model);
		
		return model;
	}

	private void generateInteractionResources(List<Interaction> interactions, Model model) {
		String wpId = this.pathway.getPathway().getXref().getId();
		String revision = Utils.getRevisionFromVersion(wpId, pathway.getPathway().getVersion());

		for (Interaction interaction : interactions) {
			interactionConvertor.convertInteraction(interaction, model, wpId, revision);
		}
	}

	private void generateDataNodeResources(List<DataNode> dataNodes, Model model) {
		String wpId = this.pathway.getPathway().getXref().getId();
		String revision = Utils.getRevisionFromVersion(wpId, pathway.getPathway().getVersion());

		for (DataNode node : dataNodes) {
			dataNodeConvertor.convertDataNode(node, model, wpId, revision);
		}
	}

	private Resource generatePathwayResource(Model model) {
		Pathway pathway = this.pathway.getPathway();
		String wpId = pathway.getXref().getId();
		String revision = Utils.getRevisionFromVersion(wpId, pathway.getVersion());

		Resource pwyRes = model.createResource(Utils.IDENTIFIERS_ORG_URL + "/wikipathways/" + wpId + "_r" + revision);
		Resource pwyConceptRes = model.createResource(Utils.IDENTIFIERS_ORG_URL + "/wikipathways/" + wpId);
		pwyConceptRes.addProperty(Pav.hasVersion, pwyRes);
		pwyRes.addProperty(RDF.type, Wp.Pathway);
		pwyRes.addProperty(RDF.type, SKOS.Collection);
		pwyRes.addProperty(DC_11.identifier, model.createResource(Utils.IDENTIFIERS_ORG_URL + "/wikipathways/" + wpId));
		pwyRes.addLiteral(DC_11.source, "WikiPathways");
		pwyRes.addLiteral(DCTerms.identifier, wpId);
		pwyRes.addLiteral(DC_11.title, model.createLiteral(pathway.getTitle(), "en"));
		if (pathway.getDescription() != null)
			pwyRes.addLiteral(DCTerms.description, pathway.getDescription());
		pwyRes.addProperty(Wp.isAbout, model.createResource(Utils.WP_RDF_URL + "/Pathway/" + wpId + "_r" + revision));
		pwyRes.addProperty(FOAF.page, model.createResource("http://www.wikipathways.org/instance/" + wpId + "_r" + revision));

		// organism info
		String organism = pathway.getOrganism();
		String taxonID = Organism.fromLatinName(organism).taxonomyID().getId();
		pwyRes.addLiteral(Wp.organismName, pathway.getOrganism());
		Resource organismRes = model.createResource("http://purl.obolibrary.org/obo/NCBITaxon_" + taxonID);
		pwyRes.addProperty(Wp.organism, organismRes);
		organismRes.addProperty(model.createProperty("http://purl.obolibrary.org/obo/NCIT_C179773"), taxonID);

		// image
		Resource pngRes = model.createResource("https://www.wikipathways.org//wpi/wpi.php?action=downloadFile&type=png&pwTitle=Pathway:" + wpId + "&oldid=r" + revision);
		pwyRes.addProperty(FOAF.img, pngRes);
		pngRes.addProperty(RDF.type, FOAF.Image);
		pngRes.addLiteral(DCTerms.format, "image/png");
		Resource svgRes = model.createResource("https://www.wikipathways.org//wpi/wpi.php?action=downloadFile&type=svg&pwTitle=Pathway:" + wpId + "&oldid=r" + revision);
		pwyRes.addProperty(FOAF.img, svgRes);
		svgRes.addProperty(RDF.type, FOAF.Image);
		svgRes.addLiteral(DCTerms.format, "image/svg+xml");
 
		return pwyRes;
	}

}
