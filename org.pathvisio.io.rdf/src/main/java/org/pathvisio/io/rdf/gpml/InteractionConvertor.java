/* Copyright 2015 Martina Kutmon
 *                Ryan Miller
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
package org.pathvisio.io.rdf.gpml;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.pathvisio.io.rdf.ontologies.Gpml;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.LineElement.LinePoint;
import org.pathvisio.libgpml.model.PathwayElement.Comment;
import org.pathvisio.libgpml.model.type.LineStyleType;

public class InteractionConvertor {

	Convertor convertor;
	CommentConvertor commentConvertor;
	PointConvertor pointConvertor;

	protected InteractionConvertor(Convertor convertor) {
		this.commentConvertor = new CommentConvertor(convertor);
		this.pointConvertor = new PointConvertor(convertor);
		this.convertor = convertor;
	}

	public void convertInteraction(Interaction interaction, Model model, String wpId, String revision) {
		Resource intRes = model.createResource(convertor.pwyRes.getURI() + "/Interaction/" + interaction.getElementId());

		intRes.addProperty(RDF.type, Gpml.INTERACTION);
		convertor.pwyRes.addProperty(Gpml.HAS_INTERACTION, intRes);
		intRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);
		
		intRes.addLiteral(Gpml.LINE_THICKNESS, interaction.getLineWidth());
		intRes.addLiteral(Gpml.GRAPH_ID, interaction.getElementId());
		intRes.addLiteral(Gpml.COLOR, org.pathvisio.io.rdf.utils.Utils.colorToHex(interaction.getLineColor()));
		intRes.addLiteral(Gpml.LINE_STYLE, interaction.getLineStyle() != LineStyleType.DASHED ? "Solid" : "Broken");
		intRes.addLiteral(Gpml.ZORDER, interaction.getZOrder());
		intRes.addLiteral(Gpml.CONNECTOR_TYPE, interaction.getConnectorType().getName());
		
		if(interaction.getXref() != null && interaction.getXref().getId() != null && interaction.getXref().getDataSource() != null) {
			intRes.addLiteral(Gpml.XREF_ID, interaction.getXref().getId());
			intRes.addLiteral(Gpml.XREF_DATASOURCE, interaction.getXref().getDataSource().getFullName());
		}

		for(LinePoint p : interaction.getLinePoints()) {
			if(p.equals(interaction.getStartLinePoint())) {
				pointConvertor.convertPoint(p, model, intRes, wpId, revision, interaction.getStartArrowHeadType().getName());
			} else if (p.equals(interaction.getEndLinePoint())) {
				pointConvertor.convertPoint(p, model, intRes, wpId, revision, interaction.getEndArrowHeadType().getName());
			} else {
				pointConvertor.convertPoint(p, model, intRes, wpId, revision, null);
			}
		}

		for(Comment c : interaction.getComments()) {
			commentConvertor.parseCommentGpml(c, model, intRes);
		}
	}

}
