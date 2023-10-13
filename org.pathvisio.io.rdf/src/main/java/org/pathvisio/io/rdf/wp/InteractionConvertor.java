// Copyright 2015 BiGCaT Bioinformatics
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
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.bridgedb.IDMapperStack;
import org.pathvisio.io.rdf.ontologies.Wp;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.LineElement.Anchor;
import org.pathvisio.libgpml.model.PathwayObject;
import org.pathvisio.libgpml.model.type.ArrowHeadType;
import org.pathvisio.libgpml.model.type.ObjectType;

/**
 * 
 * @author mkutmon
 * @author ryanmiller
 *
 */
public class InteractionConvertor {

	IDMapperStack mapper;
	Convertor convertor;
	
	protected InteractionConvertor(Convertor convertor) {
		this(convertor, null);
	}
	
	protected InteractionConvertor(Convertor convertor, IDMapperStack mapper) {
		this.convertor = convertor;
		this.mapper = mapper;
	}

	/**
	 * conversion only WP vocabulary
	 * semantic information about interactions
	 */
	public void convertInteraction(Interaction interaction, Model model, String wpId, String revision) {
		System.out.println("Interaction: " + interaction.getElementId());
		if(pointingTowardsLine(interaction)) {
			System.out.println("pointing towards line. ignoring this interaction");
		} else {
			List<Interaction> participatingLines = new ArrayList<Interaction>();
			participatingLines.add(interaction);
			List<Interaction> regLines = new ArrayList<Interaction>();
			
			for (Anchor a : interaction.getAnchors()) {
				System.out.println("  anchor: " + a.getElementId());
				for (Interaction currLine : this.convertor.pathway.getInteractions()) {
					if (currLine.getObjectType().equals(ObjectType.INTERACTION)) {
						if (currLine.getStartElementRef() != null) {
							if(currLine.getStartElementRef().equals(a.getElementId())) {
								if(currLine.getStartArrowHeadType().equals(ArrowHeadType.UNDIRECTED)) {
									if(!participatingLines.contains(currLine)) participatingLines.add(currLine);
								} else {
									if(!regLines.contains(currLine)) regLines.add(currLine);
								}
							} 
						}
						if(currLine.getEndElementRef() != null) {
							if(currLine.getEndElementRef().equals(a.getElementId())) {
								if(currLine.getEndArrowHeadType().equals(ArrowHeadType.UNDIRECTED)) {
									if(!participatingLines.contains(currLine)) participatingLines.add(currLine);
								} else {
									if(!regLines.contains(currLine)) regLines.add(currLine);
								}
							}
						}
					}
				}
			}

			ArrowHeadType lt = getInteractionType(participatingLines);
			System.out.println("  line type: " + lt);
			if (lt == null) {
				System.out.println("WARNING - different line types in one interaction");
			} else if (lt.equals(ArrowHeadType.CATALYSIS)) {
				// will be handled as part of other interactions
			} else {
				String url = Utils.WP_RDF_URL + "/Pathway/" + wpId + "_r" + revision
						+ "/WP/Interaction/" + interaction.getElementId();
				Resource intRes = model.createResource(url);
				intRes.addProperty(RDF.type, Wp.Interaction);
				intRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);
				addParticipants(intRes, participatingLines);
				if (lt.equals(ArrowHeadType.DIRECTED)) {
				} else if (lt.equals(ArrowHeadType.UNDIRECTED)) {
//					undirected interactions
//					if(target.size() != 0) {
//						System.out.println("Problem - undirected with targets should not be there");
//					} else {
				}
			}
		}
	}

	private void addParticipants(Resource intRes, List<Interaction> participatingLines) {
		List<Resource> source = new ArrayList<Resource>();
		List<Resource> target = new ArrayList<Resource>();
		for(Interaction interaction : participatingLines) {
//			System.out.println("Interaction line: " + interaction.getElementId());
			for (Anchor anchor : interaction.getAnchors()) {
				PathwayObject pwEle = convertor.pathway.getPathwayObject((anchor.getElementId()));
//				System.out.println("Object: " + pwEle.getObjectType() + " with ID " + pwEle.getElementId());
			}
		}
	}

	// check if line is pointing towards another line - will be handled with baseline
	private boolean pointingTowardsLine(Interaction interaction) {
		boolean ignore = false;
		if(interaction.getStartElementRef() != null) {
			PathwayObject elem = this.convertor.pathway.getPathwayObject(interaction.getStartElementRef().getElementId());
			if(elem == null) {
				// TODO: it is an anchor / hopefully? bug?
				// ignore line
				ignore = true;
			}
		}	
		if(interaction.getEndElementRef() != null) {
			PathwayObject elem = this.convertor.pathway.getPathwayObject(interaction.getEndElementRef().getElementId());
			if(elem == null) {
				// TODO: it is an anchor / hopefully? bug?
				// ignore line
				ignore = true;
			}
		}
		return ignore;
	}

	private static ArrowHeadType getInteractionType(List<Interaction> participatingLines) {
		List<ArrowHeadType> lineTypes = new ArrayList<ArrowHeadType>();
		for(Interaction l : participatingLines) {
			if(!l.getStartArrowHeadType().equals(ArrowHeadType.UNDIRECTED)) {
				if(!lineTypes.contains(l.getStartArrowHeadType())) lineTypes.add(l.getStartArrowHeadType());
			}
			if(!l.getEndArrowHeadType().equals(ArrowHeadType.UNDIRECTED)) {
				if(!lineTypes.contains(l.getEndArrowHeadType())) lineTypes.add(l.getEndArrowHeadType());
			}
		}
		if(lineTypes.size() > 1) {
			return null;
		} else if (lineTypes.size() == 1) {
			return lineTypes.get(0);
		} else {
			return ArrowHeadType.UNDIRECTED;
		}
	}

}
