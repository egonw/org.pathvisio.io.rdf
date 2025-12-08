// Copyright (c) 2015 BiGCaT Bioinformatics
//               2022-2025 Egon Willighagen <egon.willighagen@gmail.com>
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.pathvisio.io.rdf.ontologies.CITO;
import org.pathvisio.io.rdf.ontologies.Pav;
import org.pathvisio.io.rdf.ontologies.Wp;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.Annotation;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.Group;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.PathwayElement.CitationRef;
import org.pathvisio.libgpml.model.PathwayModel;

/**
 * Tool to convert a {@link Pathway} model into a Jena RDF model.
 */
public class Convertor {

	PathwayModel pathway;
	DataNodeConvertor dataNodeConvertor;
	InteractionConvertor interactionConvertor;
	GroupConvertor groupConvertor;
	IDMapperStack mapper;

	// cached things
	String domainName;
	Resource pwyRes;
	Map<String, Resource> datanodes;

	public Convertor(PathwayModel pathway) throws Exception {
		this(pathway, Utils.WP_RDF_URL);
	}
	public Convertor(PathwayModel pathway, String domainName) throws Exception {
		this.pathway = pathway;
		this.domainName = domainName;
		this.datanodes = new HashMap<>();
		dataNodeConvertor = new DataNodeConvertor(this, domainName, mapper);
		interactionConvertor = new InteractionConvertor(this, domainName, mapper);
		groupConvertor = new GroupConvertor(this, domainName);
	}

	public Model asRDF() {
		Model model = ModelFactory.createDefaultModel();

		// pathway
		pwyRes = generatePathwayResource(model);
		generateDataNodeResources(pathway.getDataNodes(), model);
		generateInteractionResources(pathway.getInteractions(), model);
		generateGroupResources(pathway.getGroups(), model);
		
		return model;
	}

	private void generateGroupResources(List<Group> groups, Model model) {
		String wpId = this.pathway.getPathway().getXref().getId();
		String revision = Utils.getRevisionFromVersion(wpId, pathway.getPathway().getVersion());

		for (Group group : groups) {
			groupConvertor.convertGroup(group, model, wpId, revision);
		}
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
		pwyRes.addProperty(Wp.isAbout, model.createResource(this.domainName + "/Pathway/" + wpId + "_r" + revision));
		pwyRes.addProperty(FOAF.page, model.createResource("http://www.wikipathways.org/instance/" + wpId + "_r" + revision));

		Map<String,String> others = new HashMap<>();
		others.put("Physcomitrium patens","3218");
		others.put("Ocimum basilicum", "1753");
		others.put("Ulva curvata", "135247");
		others.put("Papaver somniferum", "3469");
		others.put("Triglochin maritima", "55501");
		others.put("Phaseolus vulgaris", "3885");
		others.put("Petunia x hybrida", "4102");
		others.put("Erythroxylum coca", "289672");
		others.put("Agapanthus africanus", "51501");
		others.put("Artemisia annua", "35608");
		others.put("", "");

		// organism info
		String organism = pathway.getOrganism();
		if (organism.contains(",")) {
			String[] organismStrings = organism.split(",");
			for (String singleOrganism : organismStrings) {
				singleOrganism = singleOrganism.trim();
				String taxonID = Organism.fromLatinName(singleOrganism) != null ?
						Organism.fromLatinName(singleOrganism).taxonomyID().getId() :
							others.get(singleOrganism);
				if (taxonID == null) taxonID = "131567"; // cellular organisms
				pwyRes.addLiteral(Wp.organismName, singleOrganism);
				Resource organismRes = model.createResource("http://purl.obolibrary.org/obo/NCBITaxon_" + taxonID);
				pwyRes.addProperty(Wp.organism, organismRes);
				organismRes.addProperty(model.createProperty("http://purl.obolibrary.org/obo/NCIT_C179773"), taxonID);
			}
		} else {
			String taxonID = Organism.fromLatinName(organism) != null ?
					Organism.fromLatinName(organism).taxonomyID().getId() :
						others.get(organism);
			if (taxonID == null) taxonID = "131567"; // cellular organisms
			pwyRes.addLiteral(Wp.organismName, pathway.getOrganism());
			Resource organismRes = model.createResource("http://purl.obolibrary.org/obo/NCBITaxon_" + taxonID);
			pwyRes.addProperty(Wp.organism, organismRes);
			organismRes.addProperty(model.createProperty("http://purl.obolibrary.org/obo/NCIT_C179773"), taxonID);
		}

		// ontology tags
		for (Annotation annot : this.pathway.getAnnotations()) {
			if (annot.getXref() != null) {
				String ontoTag = annot.getXref().getDataSource().getSystemCode() + "_" + annot.getXref().getId();
				ontoTag = ontoTag.replace("Do_", "DOID_");
				ontoTag = ontoTag.replace("cl_", "CL_");
				pwyRes.addProperty(Wp.ontologyTag, model.createResource(Utils.PURL_OBO_LIB + ontoTag));
				if (ontoTag.contains("PW_")) {
					pwyRes.addProperty(Wp.pathwayOntologyTag, model.createResource(Utils.PURL_OBO_LIB + ontoTag));
				} else if (ontoTag.contains("DOID_")) {
					pwyRes.addProperty(Wp.diseaseOntologyTag, model.createResource(Utils.PURL_OBO_LIB + ontoTag));
				} else if (ontoTag.contains("CL_")) {
					pwyRes.addProperty(Wp.cellTypeOntologyTag, model.createResource(Utils.PURL_OBO_LIB + ontoTag));
				}
			}
		}

		// references
		for (CitationRef ref : pathway.getCitationRefs()) {
			Xref citationXref = ref.getCitation().getXref();
			String fullName = citationXref.getDataSource().getFullName();
			if ("PubMed".equals(fullName) || "DOI".equals(fullName)) {
				addCitation(model, pwyRes, citationXref);
			}
		}

		// image
		if (Utils.WP_RDF_URL.equals(this.domainName)) {
			Resource pngRes = model.createResource("https://www.wikipathways.org//wpi/wpi.php?action=downloadFile&type=png&pwTitle=Pathway:" + wpId + "&oldid=r" + revision);
			pwyRes.addProperty(FOAF.img, pngRes);
			pngRes.addProperty(RDF.type, FOAF.Image);
			pngRes.addLiteral(DCTerms.format, "image/png");
			Resource svgRes = model.createResource("https://www.wikipathways.org//wpi/wpi.php?action=downloadFile&type=svg&pwTitle=Pathway:" + wpId + "&oldid=r" + revision);
			pwyRes.addProperty(FOAF.img, svgRes);
			svgRes.addProperty(RDF.type, FOAF.Image);
			svgRes.addLiteral(DCTerms.format, "image/svg+xml");
		}
 
		return pwyRes;
	}

	protected void addCitation(Model model, Resource resource, Xref citationXref) {
		String fullName = citationXref.getDataSource().getFullName();
		if ("PubMed".equals(fullName)) {
			String pmid = citationXref.getId().trim();
			try {
				Integer.parseInt(pmid);
				Resource pmResource = model.createResource(Utils.IDENTIFIERS_ORG_URL + "/pubmed/" + pmid);
				pmResource.addProperty(RDF.type, Wp.PublicationReference);
				pmResource.addProperty(DC.source, fullName);
				pmResource.addLiteral(DCTerms.identifier, pmid);
				pmResource.addProperty(FOAF.page, model.createResource("http://www.ncbi.nlm.nih.gov/pubmed/" + pmid));
				pmResource.addProperty(DCTerms.isPartOf, resource);
				resource.addProperty(DCTerms.references, pmResource);
				resource.addProperty(CITO.cites, pmResource);
				this.pwyRes.addProperty(CITO.cites, pmResource);
			} catch (Exception e) {} // not an integer
		}
	}

}
