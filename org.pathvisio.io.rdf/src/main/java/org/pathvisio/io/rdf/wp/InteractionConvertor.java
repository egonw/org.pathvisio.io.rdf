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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.bridgedb.IDMapperStack;
import org.pathvisio.io.rdf.ontologies.Wp;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.GraphLink.LinkableTo;
import org.pathvisio.libgpml.model.Group;
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

	enum types {
		SOURCE,
		TARGET,
		OTHER
	}
	
	/**
	 * conversion only WP vocabulary
	 * semantic information about interactions
	 */
	public void convertInteraction(Interaction interaction, Model model, String wpId, String revision) {
		// System.out.println("Interaction: " + interaction.getElementId());
		if(pointingTowardsLine(interaction)) {
			System.out.println("pointing towards line. ignoring this interaction");
		} else {
			List<Interaction> participatingLines = new ArrayList<Interaction>();
			participatingLines.add(interaction);
			List<Interaction> regLines = new ArrayList<Interaction>();
			
			for (Anchor a : interaction.getAnchors()) {
				// System.out.println("  anchor: " + a.getElementId());
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
			// System.out.println("  line type: " + lt);
			if (lt == null) {
				System.out.println("WARNING - different line types in one interaction");
			} else {
				Resource intRes = createResource(model, wpId, revision, interaction);
				String gpmlURL = Utils.WP_RDF_URL + "/Pathway/" + wpId + "_r" + revision
						+ "/Interaction/" + interaction.getElementId();
				Resource gpmlRes = model.createResource(gpmlURL);
				Map<types, List<PathwayObject>> participants = getParticipants(intRes, participatingLines, lt);
				int datanodeCount = getDataNodeCount(participants, ObjectType.DATANODE);
				// System.out.println("  node count: " + datanodeCount);
				int groupCount = getDataNodeCount(participants, ObjectType.GROUP);
				// System.out.println("  group count: " + groupCount);
				if (groupCount > 0) {
					// totally unsupported at this moment
				} else if (lt.equals(ArrowHeadType.CATALYSIS)) {
					if (datanodeCount > 0) {
						intRes.addProperty(RDF.type, Wp.DirectedInteraction);
						intRes.addProperty(RDF.type, Wp.Catalysis);
						intRes.addProperty(RDF.type, Wp.Interaction);
						intRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);
						intRes.addProperty(Wp.isAbout, gpmlRes);
						for (PathwayObject node : participants.get(types.SOURCE)) {
							Resource nodeRes = getResourceForID(model, wpId, revision, node.getElementId());
							if (nodeRes != null) {
								intRes.addProperty(Wp.source, nodeRes);
								intRes.addProperty(Wp.participants, nodeRes);
								nodeRes.addProperty(DCTerms.isPartOf, intRes);
							}
						}
						for (PathwayObject node : participants.get(types.TARGET)) {
							Resource nodeRes = getResourceForID(model, wpId, revision, node.getElementId());
							if (nodeRes != null) {
								intRes.addProperty(Wp.target, nodeRes);
								intRes.addProperty(Wp.participants, nodeRes);
								nodeRes.addProperty(DCTerms.isPartOf, intRes);
							}
						}
						for (PathwayObject node : participants.get(types.OTHER)) {
							Resource nodeRes = getResourceForID(model, wpId, revision, node.getElementId());
							if (nodeRes != null) {
								intRes.addProperty(Wp.participants, nodeRes);
								nodeRes.addProperty(DCTerms.isPartOf, intRes);
							}
						}
					}
				} else if (lt.equals(ArrowHeadType.DIRECTED)) {
					if (datanodeCount > 0) {
						intRes.addProperty(RDF.type, Wp.Interaction);
						intRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);
						intRes.addProperty(RDF.type, Wp.DirectedInteraction);
						intRes.addProperty(Wp.isAbout, gpmlRes);
						for (PathwayObject node : participants.get(types.SOURCE)) {
							Resource nodeRes = getResourceForID(model, wpId, revision, node.getElementId());
							if (nodeRes != null) {
								intRes.addProperty(Wp.source, nodeRes);
								intRes.addProperty(Wp.participants, nodeRes);
								nodeRes.addProperty(DCTerms.isPartOf, intRes);
							}
						}
						for (PathwayObject node : participants.get(types.TARGET)) {
							Resource nodeRes = getResourceForID(model, wpId, revision, node.getElementId());
							if (nodeRes != null) {
								intRes.addProperty(Wp.target, nodeRes);
								intRes.addProperty(Wp.participants, nodeRes);
								nodeRes.addProperty(DCTerms.isPartOf, intRes);
							}
						}
						for (PathwayObject node : participants.get(types.OTHER)) {
							Resource nodeRes = getResourceForID(model, wpId, revision, node.getElementId());
							if (nodeRes != null) {
								intRes.addProperty(Wp.participants, nodeRes);
								nodeRes.addProperty(DCTerms.isPartOf, intRes);
							}
						}
					}
				} else if (lt.equals(ArrowHeadType.UNDIRECTED)) {
					if (datanodeCount > 0) {
						intRes.addProperty(RDF.type, Wp.Interaction);
						intRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);
						intRes.addProperty(Wp.isAbout, gpmlRes);
						for (PathwayObject node : participants.get(types.SOURCE)) {
							Resource nodeRes = getResourceForID(model, wpId, revision, node.getElementId());
							intRes.addProperty(Wp.participants, nodeRes);
							nodeRes.addProperty(DCTerms.isPartOf, intRes);
						}
						for (PathwayObject node : participants.get(types.TARGET)) {
							Resource nodeRes = getResourceForID(model, wpId, revision, node.getElementId());
							intRes.addProperty(Wp.participants, nodeRes);
							nodeRes.addProperty(DCTerms.isPartOf, intRes);
						}
						for (PathwayObject node : participants.get(types.OTHER)) {
							Resource nodeRes = getResourceForID(model, wpId, revision, node.getElementId());
							if (nodeRes != null) {
								intRes.addProperty(Wp.participants, nodeRes);
								nodeRes.addProperty(DCTerms.isPartOf, intRes);
							}
						}
					}
				}
			}
		}
	}

	private int getDataNodeCount(Map<types, List<PathwayObject>> participants, ObjectType objectType) {
		int count = 0;
		for (PathwayObject node : participants.get(types.SOURCE))
			if (node.getObjectType() == objectType) count++; 
		for (PathwayObject node : participants.get(types.TARGET))
			if (node.getObjectType() == objectType) count++;
		for (PathwayObject node : participants.get(types.OTHER))
			if (node.getObjectType() == objectType) count++;
		return count;
	}

	private Resource createResource(Model model, String wpId, String revision, Interaction interaction) {
		String url = Utils.WP_RDF_URL + "/Pathway/" + wpId + "_r" + revision
				+ "/WP/Interaction/" + interaction.getElementId();
		return model.createResource(url);
	}

	private Resource getResourceForID(Model model, String wpId, String revision, String elementId) {
		Resource res = this.convertor.datanodes.get(elementId);
		if (res != null) return res;
		// maybe an interaction?
		for (Interaction interaction : convertor.pathway.getInteractions()) {
			if (interaction.getElementId().equals(elementId)) {
				return createResource(model, wpId, revision, interaction);
			}
		}
		return null;
	}

	private Map<types, List<PathwayObject>> getParticipants(Resource intRes, List<Interaction> participatingLines, ArrowHeadType overallType) {
		List<PathwayObject> sources = new ArrayList<>();
		List<PathwayObject> targets = new ArrayList<>();
		List<PathwayObject> others = new ArrayList<>();
		// System.out.println("  overall type: " + overallType);
		for(Interaction interaction : participatingLines) {
			// System.out.println("  line: " + interaction.getElementId());
			LinkableTo start = interaction.getStartElementRef();
			if (start != null) {
				// System.out.println("    start: " + start.getElementId());
				PathwayObject pwObj = convertor.pathway.getPathwayObject(start.getElementId());
				// System.out.println("      type: " + pwObj.getObjectType());
				if (pwObj instanceof Group || pwObj instanceof DataNode || pwObj instanceof Interaction) {
					// System.out.println("      node: " + pwObj);
					if (overallType == ArrowHeadType.UNDIRECTED) {
						// System.out.println("      other: " + start.getElementId());
						// everything is just other
						others.add(pwObj);
					} else {
						if (interaction.getStartArrowHeadType() == ArrowHeadType.UNDIRECTED) {
							// System.out.println("      source: " + start.getElementId());
							// the node is source
							sources.add(pwObj);
						} else {
							// System.out.println("      target: " + start.getElementId());
							// the node is target
							targets.add(pwObj);
						}
					}
				}
			}
			LinkableTo end = interaction.getEndElementRef();
			if (end != null) {
				// System.out.println("    end: " + end.getElementId());
				PathwayObject pwObj = convertor.pathway.getPathwayObject(end.getElementId());
				// System.out.println("      type: " + pwObj.getObjectType());
				if (pwObj instanceof Anchor) {
					Interaction targetInternation = getInteractionWithAnchor((Anchor)pwObj);
					if (targetInternation != null) {
						// System.out.println("      node: " + targetInternation);
						if (overallType == ArrowHeadType.UNDIRECTED) {
							// System.out.println("      other: " + end.getElementId());
							// everything is just other
							others.add(targetInternation);
						} else {
							if (interaction.getStartArrowHeadType() == ArrowHeadType.UNDIRECTED) {
								// System.out.println("      target: " + end.getElementId());
								// the node is target
								targets.add(targetInternation);
							} else {
								// System.out.println("      source: " + end.getElementId());
								// the node is source
								sources.add(targetInternation);
							}
						}
					}
				} else if (pwObj instanceof Group || pwObj instanceof DataNode || pwObj instanceof Interaction) {
					// System.out.println("      node: " + pwObj);
					if (overallType == ArrowHeadType.UNDIRECTED) {
						// System.out.println("      other: " + end.getElementId());
						// everything is just other
						others.add(pwObj);
					} else {
						if (interaction.getStartArrowHeadType() == ArrowHeadType.UNDIRECTED) {
							// System.out.println("      target: " + end.getElementId());
							// the node is target
							targets.add(pwObj);
						} else {
							// System.out.println("      source: " + end.getElementId());
							// the node is source
							sources.add(pwObj);
						}
					}
				}
			}
		}
		Map<types, List<PathwayObject>> participants = new HashMap<>();
		participants.put(types.SOURCE, sources);
		participants.put(types.TARGET, targets);
		participants.put(types.OTHER, others);
		return participants;
	}

	// Returns the Interaction of which the given anchor is part.
	private Interaction getInteractionWithAnchor(Anchor anchor) {
		for (Interaction interaction : convertor.pathway.getInteractions()) {
			if (interaction.hasAnchor(anchor)) return interaction;
		}
		return null;
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
