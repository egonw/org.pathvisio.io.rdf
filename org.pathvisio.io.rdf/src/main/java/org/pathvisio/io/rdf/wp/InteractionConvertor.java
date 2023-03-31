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
import org.bridgedb.IDMapperStack;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.LineElement.Anchor;
import org.pathvisio.libgpml.model.PathwayObject;

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
	public void convertInteraction(Interaction interaction, Model model) {
		boolean ignore = pointingTowardsLine(interaction);
		if(!ignore) {
			List<Interaction> participatingLines = new ArrayList<Interaction>();
			participatingLines.add(interaction);
			List<Interaction> regLines = new ArrayList<Interaction>();
			
			for (Anchor a : interaction.getAnchors()) {
				for (Interaction currLine : this.convertor.pathway.getInteractions()) {
					
				}
			}

//			for(MAnchor a : e.getMAnchors()) {
//				for(PathwayElement currLine : data.getPathway().getDataObjects()) {
//					if(currLine.getObjectType().equals(ObjectType.LINE)) {
//						if(currLine.getStartGraphRef() != null) {
//							if(currLine.getStartGraphRef().equals(a.getGraphId())) {
//								if(currLine.getStartLineType().equals(LineType.LINE)) {
//									if(!participatingLines.contains(currLine)) participatingLines.add((MLine)currLine);
//								} else {
//									if(!regLines.contains(currLine)) regLines.add((MLine)currLine);
//								}
//							} 
//						}		
//						if(currLine.getEndGraphRef() != null) {
//							if(currLine.getEndGraphRef().equals(a.getGraphId())) {
//								if(currLine.getEndLineType().equals(LineType.LINE)) {
//									if(!participatingLines.contains(currLine)) participatingLines.add((MLine)currLine);
//								} else {
//									if(!regLines.contains(currLine)) regLines.add((MLine)currLine);
//								}
//							}
//						}
//					}
//				}
//			}
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
}
