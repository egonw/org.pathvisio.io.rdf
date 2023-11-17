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
import org.pathvisio.libgpml.model.LineElement.Anchor;

public class AnchorConvertor {

	Convertor convertor;
	CommentConvertor commentConvertor;

	protected AnchorConvertor(Convertor convertor) {
		this.commentConvertor = new CommentConvertor(convertor);
		this.convertor = convertor;
	}

	public void convertAnchor(Anchor anchor, Model model, Resource intRes, String wpId, String revision) {
		Resource anchorRes = model.createResource(intRes.getURI() + "/Anchor/" + anchor.getElementId());
		anchorRes.addProperty(RDF.type, Gpml.ANCHOR);
		anchorRes.addProperty(DCTerms.isPartOf, intRes);
		anchorRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);

		anchorRes.addLiteral(Gpml.GRAPH_ID, anchor.getElementId());
		anchorRes.addLiteral(Gpml.POSITION, anchor.getPosition());
		anchorRes.addLiteral(Gpml.SHAPE, anchor.getShapeType().getName());

		intRes.addProperty(Gpml.HAS_ANCHOR, anchorRes);
	}

}
