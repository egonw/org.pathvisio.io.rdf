/* Copyright 2015 BiGCaT Bioinformatics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http:*www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pathvisio.io.rdf.wp;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.pathvisio.io.rdf.ontologies.Wp;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.PathwayElement.CitationRef;

public class DataNodeConvertor {

	IDMapperStack mapper;
	Convertor convertor;
	
	protected DataNodeConvertor(Convertor convertor) {
		this(convertor, null);
	}
	
	protected DataNodeConvertor(Convertor convertor, IDMapperStack mapper) {
		this.convertor = convertor;
		this.mapper = mapper;
	}

	public boolean validXref(Xref xref) {
		return xref != null && xref.getId() != null && xref.getDataSource() != null;
	}

	public void convertDataNode(DataNode elem, Model model, String wpId, String revision) {
		String nodeID = elem.getElementId();
		Xref xref = elem.getXref();
		if (validXref(xref)) {
			if(!elem.getType().equals("Unknown")) {
				if (xref.getId() != null && elem.getXref().getId().trim().length() > 0) {
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
							
							this.convertor.datanodes.put(nodeID, datanodeRes);
							datanodeRes.addLiteral(DC.source, xref.getDataSource().getFullName());
							datanodeRes.addLiteral(DCTerms.identifier, xrefid);

							datanodeRes.addProperty(RDF.type, Wp.DataNode);
							
							switch (elem.getType().getName()) {
							case "GeneProduct":
								datanodeRes.addProperty(RDF.type, Wp.GeneProduct);
								break;
								
							case "Protein":
								datanodeRes.addProperty(RDF.type, Wp.Protein);
								break;
								
							case "Metabolite":
								datanodeRes.addProperty(RDF.type, Wp.Metabolite);
								break;
								
							case "Rna":
								datanodeRes.addProperty(RDF.type, Wp.Rna);
								break;

							case "Pathway":
								datanodeRes.addProperty(RDF.type, Wp.Pathway);
								break;

							case "Complex":
								datanodeRes.addProperty(RDF.type, Wp.Complex);
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

						// references
						for (CitationRef ref : elem.getCitationRefs()) {
							Xref citationXref = ref.getCitation().getXref();
							String fullName = citationXref.getDataSource().getFullName();
							if ("PubMed".equals(fullName) || "DOI".equals(fullName)) {
								this.convertor.addCitation(model, datanodeRes, citationXref);
							}
						}

						datanodeRes.addProperty(Wp.isAbout, model.createResource(Utils.WP_RDF_URL + "/Pathway/" + wpId + "_r" + revision + "/DataNode/" + elem.getElementId()));
						datanodeRes.addLiteral(RDFS.label, elem.getTextLabel().replace("\n", " ").trim());
						datanodeRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);
					}
				}
			}
		}
	}

}
