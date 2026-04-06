/* Copyright 2015-2026 BiGCaT Bioinformatics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pathvisio.io.rdf.wp.plugin.bridgedb;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.DataNode;

public class DataNodeConvertor {

	IDMapperStack mapper;
	boolean isEnabled;

	public DataNodeConvertor(IDMapperStack mapper) {
		this.mapper = mapper;
		this.isEnabled = Utils.isPluginEnabled("bridgedb");
	}

	public boolean isEnabled() {
		return this.isEnabled;
	}
	
	public void convertDataNode(DataNode elem, Model model, Resource datanodeRes) {
		if (!this.isEnabled) return;

		Xref xref = elem.getXref();
		switch (elem.getType().getName()) {
		case "GeneProduct":
			break;

		case "Protein":
			break;

		case "Metabolite":
			try { BridgeDbIDMapper.getUnifiedIdentifiers(model, mapper, xref, datanodeRes);
			} catch(Exception exception) {} // ignore
			break;

		case "Rna":
			break;

		case "Pathway":
			break;

		case "Complex":
			break;

		default:
			break;
		}
	}

}
