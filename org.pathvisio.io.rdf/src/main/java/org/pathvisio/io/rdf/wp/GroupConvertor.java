// Copyright 2015 BiGCaT Bioinformatics
//           2023-2025 Egon Willighagen <egon.willighagen@gmail.com>
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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.bridgedb.Xref;
import org.pathvisio.io.rdf.ontologies.Wp;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.Group;
import org.pathvisio.libgpml.model.Groupable;
import org.pathvisio.libgpml.model.PathwayElement.CitationRef;
import org.pathvisio.libgpml.model.type.GroupType;
import org.pathvisio.libgpml.model.type.ObjectType;

/**
 * 
 * @author mkutmon
 * @author ryanmiller
 * @author egonw
 *
 */
public class GroupConvertor {

	String domainName;
	Convertor convertor;
	
	protected GroupConvertor(Convertor convertor, String domainName) {
		this.convertor = convertor;
		this.domainName = domainName;
	}

	/**
	 * conversion only WP vocabulary
	 * semantic information about a complex group
	 */
	public void convertGroup(Group group, Model model, String wpId, String revision) {
		DataNode embeddedComplexDataNode = null;
		if(group.getType() == GroupType.COMPLEX) {
			List<Resource> participants = new ArrayList<Resource>();
			for(Groupable e : group.getPathwayElements()) {
				if (e instanceof DataNode) {
					DataNode node = (DataNode)e;
					if ("Complex".equals(node.getType().getName())) {
						// if it has a DataNode participant of @Type=Complex
						embeddedComplexDataNode = node;
					} else {
						Resource r = this.convertor.datanodes.get(node.getElementId());
						if(r != null) {
							participants.add(r);
						}
					}
				}
			}
			// TODO: what about complexes with only one data node?
			if(participants.size() > 1) {
				String graphId = group.getElementId();
				Resource groupRes = model.createResource(this.domainName + "/Pathway/" + wpId + "_r" + revision + "/Complex/" + graphId);
				groupRes.addProperty(RDF.type, Wp.DataNode);
				groupRes.addProperty(RDF.type, Wp.Complex);
				groupRes.addProperty(Wp.isAbout, model.createResource(this.domainName + "/Pathway/" + wpId + "_r" + revision + "/Group/" + graphId));
				groupRes.addProperty(DCTerms.isPartOf, this.convertor.pwyRes);
				if(group.getTextLabel() != null && !group.getTextLabel().equals("")) groupRes.addLiteral(RDFS.label, group.getTextLabel().replace("\n", " "));

				Resource complexBinding = model.createResource(this.domainName + "/Pathway/" + wpId + "_r" + revision + "/ComplexBinding/" + graphId);
				complexBinding.addProperty(RDF.type, Wp.Interaction);
				complexBinding.addProperty(RDF.type, Wp.Binding);
				complexBinding.addProperty(RDF.type, Wp.ComplexBinding);
				complexBinding.addProperty(Wp.participants, groupRes);
				complexBinding.addProperty(Wp.isAbout, model.createResource(this.domainName + "/Pathway/" + wpId + "_r" + revision + "/Group/" + graphId));
				complexBinding.addProperty(DCTerms.isPartOf, this.convertor.pwyRes);
				
				for(Resource r : participants) {
					groupRes.addProperty(Wp.participants, r);
					complexBinding.addProperty(Wp.participants, r);
					r.addProperty(DCTerms.isPartOf, groupRes);
				}
				
				// there is (potentially) one special participant
				if (embeddedComplexDataNode != null) {
					Xref idXref = embeddedComplexDataNode.getXref();
					if (idXref != null && idXref.getId() != null && idXref.getId().trim().length() > 0) {
						groupRes.addProperty(RDFS.label, embeddedComplexDataNode.getTextLabel());
						if (idXref.getDataSource() != null) {
							String idURL = idXref.getDataSource().getIdentifiersOrgUri(idXref.getId());
							if (idURL != null) idURL = idURL.replace("http://identifiers", "https://identifiers");
							groupRes.addProperty(DC.identifier, model.createResource(idURL));
							groupRes.addLiteral(DC.source, idXref.getDataSource().getFullName());
							groupRes.addLiteral(DCTerms.identifier, idXref.getId());
						}
					}
				}

				// references
				for (CitationRef ref : group.getCitationRefs()) {
					Xref citationXref = ref.getCitation().getXref();
					String fullName = citationXref.getDataSource().getFullName();
					if ("PubMed".equals(fullName) || "DOI".equals(fullName)) {
						this.convertor.addCitation(model, groupRes, citationXref);
						this.convertor.addCitation(model, complexBinding, citationXref);
					}
				}
			}
		}
		// TODO - for now only convert groups of type complex - what about other elements
	}

}
