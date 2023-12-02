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
import org.pathvisio.libgpml.model.Group;

public class GroupConvertor {

	Convertor convertor;
	CommentConvertor commentConvertor;

	protected GroupConvertor(Convertor convertor) {
		this.commentConvertor = new CommentConvertor(convertor);
		this.convertor = convertor;
	}

	public void convertGroup(Group group, Model model, String wpId, String revision) {
		if (group.getGroupRef() != null) System.out.println("  " + group.getGroupRef().getElementId());
		String graphId = group.getElementId();
		Resource groupRes = model.createResource(convertor.pwyRes.getURI() + "/Group/" + graphId);

		groupRes.addProperty(RDF.type, Gpml.GROUP);
		convertor.pwyRes.addProperty(Gpml.HAS_GROUP, groupRes);
		groupRes.addProperty(DCTerms.isPartOf, convertor.pwyRes);

		if(group.getGroupRef() != null) groupRes.addLiteral(Gpml.GROUP_REF, group.getGroupRef());
		groupRes.addLiteral(Gpml.GROUP_ID, group.getElementId());
		if(group.getElementId() != null) groupRes.addLiteral(Gpml.GRAPH_ID, group.getElementId());
		if(group.getTextLabel() != null && !group.getTextLabel().equals("")) groupRes.addLiteral(Gpml.TEXTLABEL, group.getTextLabel());
		groupRes.addLiteral(Gpml.STYLE, group.getShapeType().getName());
	}

}
