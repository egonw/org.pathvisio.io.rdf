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
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
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

	// cached things
	Resource pwyRes;

	public Convertor(PathwayModel pathway) {
		this.pathway = pathway;
		dataNodeConvertor = new DataNodeConvertor();
	}

	public Convertor(PathwayModel pathway, IDMapperStack mapper) {
		this(pathway);
		dataNodeConvertor = new DataNodeConvertor(mapper);
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
			dataNodeConvertor.convertDataNode(node, model);
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

	public void convertDataNode(DataNode elem, Model model) {
		if(elem.getXref() != null && elem.getXref().getId() != null && elem.getXref().getDataSource() != null) {
			if(!elem.getType().equals("Unknown")) {
				if (elem.getXref().getId() != null && elem.getXref().getId().trim().length() > 0) {
					Xref xref = elem.getXref();
					String xrefid = xref.getId(); 
					DataSource datasource = xref.getDataSource(); 
					String url = datasource.getIdentifiersOrgUri(xrefid);
					if (url != null) url = url.replace("http://identifiers", "https://identifiers");
					String foafURL = null;
					if (datasource.getKnownUrl(xrefid) != null) {
						foafURL = datasource.getKnownUrl(xrefid).replaceAll(" ", "_");
					}
					if ("HMDB".equals(xref.getDataSource().getFullName())) {
						if (xrefid.length() == 11) {
							// OK, all is fine
						} else if (xrefid.length() > 4) {
							xrefid = "HMDB00" + xrefid.substring(4);
						} // else, something really weird
						url = datasource.getIdentifiersOrgUri(xrefid);
						if (url != null) url = url.replace("http://identifiers", "https://identifiers");
					}
					if(url != null && !url.equals("")) {
						Resource datanodeRes = null; // = data.getDataNodes().get(xref);
						if(datanodeRes == null) {
							if (url.contains("chebi/CHEBI:")){
								String resourceURL = url.trim().replaceAll(" ", "_");
								datanodeRes = model.createResource(resourceURL);
								datanodeRes.addProperty(DC.identifier, model.createResource(resourceURL));
							}
							else if (url.contains("chebi")){
								String resourceURL = url.trim().replaceAll(" ", "_").replace("chebi/","chebi/CHEBI:");
								datanodeRes = model.createResource(resourceURL);
								datanodeRes.addProperty(DC.identifier, model.createResource(resourceURL));
							}
							else{
								String resourceURL = url.trim().replaceAll(" ", "_");
								datanodeRes = model.createResource(resourceURL);
								datanodeRes.addProperty(DC.identifier, model.createResource(resourceURL));
							}
							
							datanodeRes.addLiteral(DC.source, xref.getDataSource().getFullName());
							datanodeRes.addLiteral(DCTerms.identifier, xrefid);

							datanodeRes.addProperty(RDF.type, Wp.DataNode);
							
							switch (elem.getType().getName()) {
							case "GeneProduct":
								datanodeRes.addProperty(RDF.type, Wp.GeneProduct);
								// add id mapping step
								
								// // GpmlConverter.getUnifiedIdentifiers(model, mapper, idXref, datanodeRes);
								
								break;
								
							case "Protein":
								datanodeRes.addProperty(RDF.type, Wp.Protein);
								// add id mapping step
								
								// // GpmlConverter.getUnifiedIdentifiers(model, mapper, idXref, datanodeRes);
								
								break;
								
							case "Metabolite":
								datanodeRes.addProperty(RDF.type, Wp.Metabolite);
								// add id mapping step
								
								// GpmlConverter.getUnifiedIdentifiers(model, mapper, idXref, datanodeRes);
								
								break;
								
							case "Rna":
								datanodeRes.addProperty(RDF.type, Wp.Rna);
								// add id mapping step
								
								// GpmlConverter.getUnifiedIdentifiers(model, mapper, idXref, datanodeRes);

								
								break;
							case "Pathway":
								datanodeRes.addProperty(RDF.type, Wp.Pathway);
								// TODO: unified identifiers (e.g. for Reactome pathways!)
								break;
							case "Complex":
								datanodeRes.addProperty(RDF.type, Wp.Complex);
								// add id mapping step

								// GpmlConverter.getUnifiedIdentifiers(model, mapper, idXref, datanodeRes);

								break;
							default:
								break;
							}
									
							
							// data.getDataNodes().put(elem.getXref(), datanodeRes);
							// data.getPathwayElements().put(elem, datanodeRes);
						}
						// FOAF URL
						if (foafURL != null) {
							Resource foafResource = model.createResource(foafURL);
							datanodeRes.addProperty(FOAF.page, foafResource);
						}
						// TODO: what to do about those - are they pathway specific?
						// for(PublicationXref pubXref : elem.getBiopaxReferenceManager().getPublicationXRefs()) {
						//	PublicationXrefConverter.parsePublicationXrefWp(pubXref, datanodeRes, data.getPathwayRes(), model, mapper);
						// }

						datanodeRes.addProperty(Wp.isAbout, model.createResource(pwyRes.getURI() + "/DataNode/" + elem.getElementId()));
						// datanodeRes.addLiteral(RDFS.label, elem.getTextLabel().replace("\n", " ").trim());
						// datanodeRes.addProperty(DCTerms.isPartOf, data.getPathwayRes());
					}
				}
			}
		}
	}

}
