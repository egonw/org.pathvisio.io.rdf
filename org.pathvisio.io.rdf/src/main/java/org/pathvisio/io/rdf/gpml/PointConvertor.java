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
import org.pathvisio.libgpml.model.LineElement.LinePoint;

public class PointConvertor {

	Convertor convertor;
	CommentConvertor commentConvertor;

	protected PointConvertor(Convertor convertor) {
		this.commentConvertor = new CommentConvertor(convertor);
		this.convertor = convertor;
	}

	public void convertPoint(LinePoint point, Model model, Resource lineRes, String wpId, String revision, String arrowHead) {
		String graphId = point.getElementId();
		if (graphId == null) {
			graphId = Utils.md5sum(""+point.hashCode());
		}
		Resource pointRes = model.createResource(lineRes.getURI() + "/Point/" + graphId);

		pointRes.addProperty(RDF.type, Gpml.POINT);
		pointRes.addProperty(DCTerms.isPartOf, lineRes);
		pointRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);

		// TODO: make sure that every point has a graph id!!!
		if(point.getElementId() != null) pointRes.addLiteral(Gpml.GRAPH_ID, point.getElementId());
		if(point.getElementRef() != null) pointRes.addLiteral(Gpml.GRAPH_REF, point.getElementRef().getElementId());
		pointRes.addLiteral(Gpml.REL_X, point.getRelX());
		pointRes.addLiteral(Gpml.REL_Y, point.getRelY());
		pointRes.addLiteral(Gpml.X, point.getX());
		pointRes.addLiteral(Gpml.Y, point.getY());
		
		if(arrowHead != null) pointRes.addLiteral(Gpml.ARROW_HEAD, arrowHead);

		lineRes.addProperty(Gpml.HAS_POINT, pointRes);
	}

}
