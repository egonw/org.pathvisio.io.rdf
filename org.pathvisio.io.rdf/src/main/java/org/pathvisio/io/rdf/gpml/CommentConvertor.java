// Copyright 2015 Martina Kutmon
//                Ryan Miller
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
package org.pathvisio.io.rdf.gpml;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.pathvisio.io.rdf.ontologies.Gpml;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.PathwayElement.Comment;

/**
 * 
 * @author mkutmon
 * @author ryanmiller
 *
 */
public class CommentConvertor {

	Convertor convertor;
	
	protected CommentConvertor(Convertor convertor) {
		this.convertor = convertor;
	}

	/**
	 * conversion only GPML vocabulary
	 */
	public void parseCommentGpml(Comment comment, Model model, Resource parent) {
		String commentStr = comment.getCommentText();
		String commentid = (commentStr != null)
			? Utils.md5sum(commentStr) : Utils.md5sum(""+comment.hashCode());
		Resource commentRes = model.createResource(convertor.pwyRes.getURI() + "/Comment/" + commentid);

		commentRes.addProperty(RDF.type, Gpml.COMMENT);

		if(comment.getSource() != null) commentRes.addLiteral(Gpml.SOURCE, comment.getSource());
		if (commentStr != null) commentRes.addLiteral(Gpml.COMMENT_TEXT, commentStr);
		
		parent.addProperty(Gpml.HAS_COMMENT, commentRes);
		commentRes.addProperty(DCTerms.isPartOf, parent);
	}
	
}
