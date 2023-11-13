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
import org.apache.jena.vocabulary.RDF;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.pathvisio.io.rdf.ontologies.Gpml;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.DataNode;

public class DataNodeConvertor {

	IDMapperStack mapper;
	Convertor convertor;
	
	protected DataNodeConvertor(Convertor convertor) {
		this(convertor, null);
	}
	
	protected DataNodeConvertor(Convertor convertor, IDMapperStack mapper) {
		this.convertor = convertor;
		this.mapper = mapper;
	}

	public boolean validXref(Xref xref) {
		return xref != null && xref.getId() != null && xref.getDataSource() != null;
	}

	public void convertDataNode(DataNode elem, Model model, String wpId, String revision) {
		String pwResURI = "http://rdf.wikipathways.org/Pathway/" + wpId + "_rr" + revision; 
		Resource datanodeRes = model.createResource(pwResURI + "/DataNode/" + 
		    (elem.getElementId() != null ? elem.getElementId() : elem.hashCode()));
		datanodeRes.addProperty(RDF.type, Gpml.DATA_NODE);

		datanodeRes.addLiteral(Gpml.FONT_STYLE, elem.getFontStyle() ? "Italic" : "Normal");
		datanodeRes.addLiteral(Gpml.FONT_SIZE, elem.getFontSize());
		datanodeRes.addLiteral(Gpml.FONT_NAME, elem.getFontName());
		datanodeRes.addLiteral(Gpml.GRAPH_ID, elem.getElementId() != null ? elem.getElementId() : "");
		if(elem.getGroupRef() != null) datanodeRes.addLiteral(Gpml.GROUP_REF, elem.getGroupRef());
		datanodeRes.addLiteral(Gpml.FONT_WEIGHT, elem.getFontWeight() ? "Bold" : "Normal");
		datanodeRes.addLiteral(Gpml.FILL_COLOR, Utils.colorToHex(elem.getFillColor()));
		datanodeRes.addLiteral(Gpml.ZORDER, elem.getZOrder());

	}

}
