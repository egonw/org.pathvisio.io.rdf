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
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.pathvisio.io.rdf.ontologies.Gpml;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.PathwayElement.Comment;
import org.pathvisio.libgpml.model.type.LineStyleType;

public class DataNodeConvertor {

	IDMapperStack mapper;
	Convertor convertor;
	CommentConvertor commentConvertor;
	
	protected DataNodeConvertor(Convertor convertor) {
		this(convertor, null);
	}
	
	protected DataNodeConvertor(Convertor convertor, IDMapperStack mapper) {
		this.commentConvertor = new CommentConvertor(convertor);
		this.convertor = convertor;
		this.mapper = mapper;
	}

	public boolean validXref(Xref xref) {
		return xref != null && xref.getId() != null && xref.getDataSource() != null;
	}

	public void convertDataNode(DataNode elem, Model model, String wpId, String revision) {
		String pwResURI = "http://rdf.wikipathways.org/Pathway/" + wpId + "_r" + revision; 
		Resource datanodeRes = model.createResource(pwResURI + "/DataNode/" + 
		    (elem.getElementId() != null ? elem.getElementId() : elem.hashCode()));
		datanodeRes.addProperty(RDF.type, Gpml.DATA_NODE);
		datanodeRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);
		convertor.pwyRes.addProperty(Gpml.HAS_DATA_NODE, datanodeRes);

		datanodeRes.addLiteral(Gpml.FONT_STYLE, elem.getFontStyle() ? "Italic" : "Normal");
		datanodeRes.addLiteral(Gpml.FONT_SIZE, elem.getFontSize());
		datanodeRes.addLiteral(Gpml.FONT_NAME, elem.getFontName());
		datanodeRes.addLiteral(Gpml.FONT_WEIGHT, elem.getFontWeight() ? "Bold" : "Normal");
		datanodeRes.addLiteral(Gpml.FONT_DECORATION, elem.getFontDecoration() ? "Underline" : "Normal");
		datanodeRes.addLiteral(Gpml.FONT_STRIKETHRU, elem.getFontStrikethru() ? "Strikethru" : "Normal");
		datanodeRes.addLiteral(Gpml.GRAPH_ID, elem.getElementId() != null ? elem.getElementId() : "");
		if(elem.getGroupRef() != null) datanodeRes.addLiteral(Gpml.GROUP_REF, elem.getGroupRef());
		datanodeRes.addLiteral(Gpml.FILL_COLOR, Utils.colorToHex(elem.getFillColor()));
		datanodeRes.addLiteral(Gpml.ZORDER, elem.getZOrder());
		datanodeRes.addLiteral(Gpml.CENTER_X, elem.getCenterX());
		datanodeRes.addLiteral(Gpml.CENTER_Y, elem.getCenterY());
		datanodeRes.addLiteral(Gpml.HEIGHT, elem.getHeight());
		datanodeRes.addLiteral(Gpml.WIDTH, elem.getWidth());
		datanodeRes.addLiteral(Gpml.ALIGN, elem.getHAlign().getName());
		datanodeRes.addLiteral(Gpml.VALIGN, elem.getVAlign().getName());
		datanodeRes.addLiteral(Gpml.LINE_STYLE, elem.getBorderStyle() != LineStyleType.DASHED ? "Solid" : "Broken");
		datanodeRes.addLiteral(Gpml.LINE_THICKNESS, elem.getBorderWidth());
		datanodeRes.addLiteral(Gpml.TEXTLABEL, elem.getTextLabel());
		if (elem.getShapeType() != null)
			datanodeRes.addLiteral(Gpml.SHAPE_TYPE, elem.getShapeType().getName());
		datanodeRes.addLiteral(Gpml.TYPE, elem.getType().getName());
		datanodeRes.addLiteral(Gpml.COLOR, Utils.colorToHex(elem.getTextColor()));

		if(elem.getXref() != null && elem.getXref().getId() != null && elem.getXref().getDataSource() != null) {
			datanodeRes.addLiteral(Gpml.XREF_ID, elem.getXref().getId());
			datanodeRes.addLiteral(Gpml.XREF_DATASOURCE, elem.getXref().getDataSource().getFullName());
		}

		for(Comment c : elem.getComments()) {
			commentConvertor.parseCommentGpml(c, model, datanodeRes);
		}
	}

}
