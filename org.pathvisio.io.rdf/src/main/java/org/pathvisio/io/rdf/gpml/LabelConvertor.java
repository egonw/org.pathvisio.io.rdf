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
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.Label;
import org.pathvisio.libgpml.model.PathwayElement.Comment;
import org.pathvisio.libgpml.model.type.LineStyleType;

public class LabelConvertor {

	Convertor convertor;
	CommentConvertor commentConvertor;

	protected LabelConvertor(Convertor convertor) {
		this.commentConvertor = new CommentConvertor(convertor);
		this.convertor = convertor;
	}

	public void convertLabel(Label label, Model model, String wpId, String revision) {
		Resource labelRes = model.createResource(convertor.pwyRes.getURI() + "/Label/" + label.getElementId());

		labelRes.addProperty(RDF.type, Gpml.LABEL);
		convertor.pwyRes.addProperty(Gpml.HAS_LABEL, labelRes);
		labelRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);

		labelRes.addLiteral(Gpml.FONT_STYLE, label.getFontStyle() ? "Italic" : "Normal");
		labelRes.addLiteral(Gpml.LINE_THICKNESS, label.getBorderWidth());
		labelRes.addLiteral(Gpml.FONT_SIZE, label.getFontSize());
		labelRes.addLiteral(Gpml.FONT_NAME, label.getFontName());
		labelRes.addLiteral(Gpml.ALIGN, label.getHAlign().getName());
		if(label.getElementId() != null) labelRes.addLiteral(Gpml.GRAPH_ID, label.getElementId()); //created if statement, to check for problems in RDF creation
		labelRes.addLiteral(Gpml.COLOR, Utils.colorToHex(label.getTextColor()));
		labelRes.addLiteral(Gpml.CENTER_Y, label.getCenterY());
		labelRes.addLiteral(Gpml.VALIGN, label.getVAlign().getName());
		labelRes.addLiteral(Gpml.FONT_WEIGHT, label.getFontWeight() ? "Bold" : "Normal");
		labelRes.addLiteral(Gpml.FONT_DECORATION, label.getFontDecoration() ? "Underline" : "Normal");
		labelRes.addLiteral(Gpml.FONT_STRIKETHRU, label.getFontStrikethru() ? "Strikethru" : "Normal");
		labelRes.addLiteral(Gpml.HEIGHT, label.getHeight());
		if(label.getHref() != null) labelRes.addLiteral(Gpml.HREF, label.getHref());
		labelRes.addLiteral(Gpml.LINE_STYLE, label.getBorderStyle() != LineStyleType.DASHED ? "Solid" : "Broken");
		labelRes.addLiteral(Gpml.CENTER_X, label.getCenterX());
		labelRes.addLiteral(Gpml.TEXTLABEL, label.getTextLabel());
		labelRes.addLiteral(Gpml.WIDTH, label.getWidth());
		labelRes.addLiteral(Gpml.FILL_COLOR, Utils.colorToHex(label.getFillColor()));
		labelRes.addLiteral(Gpml.ZORDER, label.getZOrder());
		if (label.getShapeType() != null)
			labelRes.addLiteral(Gpml.SHAPE_TYPE, label.getShapeType().getName());
		
		for(Comment c : label.getComments()) {
			commentConvertor.parseCommentGpml(c, model, labelRes);
		}

	}

}
