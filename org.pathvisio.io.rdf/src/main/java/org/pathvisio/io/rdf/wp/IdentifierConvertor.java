// Copyright 2015-2021 BiGCaT Bioinformatics authors
//                2022 Egon Willighagen <egon.willighagen@gmail.com>
// 
// Conversion from GPML pathways to RDF
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.pathvisio.io.rdf.ontologies.Wp;

public class IdentifierConvertor {

	public static IDMapperStack createBridgeDbMapper(Properties prop) throws Exception {
		Class.forName("org.apache.derby.jdbc.ClientDriver");
		Class.forName("org.bridgedb.rdb.IDMapperRdb");
		if (!DataSource.fullNameExists("Ensembl")) DataSourceTxt.init();
		if (prop.getProperty("bridgefiles") == null) {
			throw new Exception("Expected a bridgefiles property, but did not find one.");
		}
		File dir = new File(prop.getProperty("bridgefiles")); //TODO Get Refactor to get them directly from bridgedb.org -> could be done with wget? Or from Dockerised version of bridgeDb?
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		    	if (name == null) return false;
		        return name.toLowerCase().endsWith(".bridge");
		    }
		};

		File[] bridgeDbFiles = dir.listFiles(filter);
		IDMapperStack mapper = new IDMapperStack();
		for (File bridgeDbFile : bridgeDbFiles) {
			System.out.println(bridgeDbFile.getAbsolutePath());
			mapper.addIDMapper(
				BridgeDb.connect("idmapper-pgdb:" + bridgeDbFile.getAbsolutePath())
			);
		}
		return mapper;
	}

	public static void getUnifiedGeneIdentifiers(Model model, IDMapper  mapper, Xref idXref, Resource internalWPDataNodeResource) throws IDMapperException, UnsupportedEncodingException {
//		System.out.println("gene xref: " + idXref);
		//ENSEMBL
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"En", "https://identifiers.org/ensembl/", Wp.bdbEnsembl
		);
		//Uniprot
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"S", "https://identifiers.org/uniprot/", Wp.bdbUniprot
		);
		//Entrez Gene
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"L", "https://identifiers.org/ncbigene/", Wp.bdbEntrezGene
		);
		//HGNC Symbols
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"H", "https://identifiers.org/hgnc.symbol/", Wp.bdbHgncSymbol
		);
	}

	public static void getUnifiedMetaboliteIdentifiers(Model model, IDMapper  mapper, Xref idXref, Resource internalWPDataNodeResource) throws IDMapperException, UnsupportedEncodingException {
//		System.out.println(idXref);
		//HMDB
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"Ch", "https://identifiers.org/hmdb/", Wp.bdbHmdb
		);
		//CHEMSPIDER
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"Cs", "https://identifiers.org/chemspider/", Wp.bdbChemspider
		);
		//ChEBI
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"Ce", "https://identifiers.org/chebi/", Wp.bdbChEBI
		);
		// Wikidata
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"Wd", "http://www.wikidata.org/entity/", Wp.bdbWikidata
		);
		// PubChem Compound
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"Cpc", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID", Wp.bdbPubChem
		);
		// Kegg Compound
				outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
					"Ck", "https://identifiers.org/kegg.compound/", Wp.bdbKeggCompound
				); 
		// LipidMaps
				outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
					"Lm", "https://identifiers.org/lipidmaps/", Wp.bdbLipidMaps
				); 
		// InChIKey
		if (mapper != null) {
			try {
				DataSource source = DataSource.getExistingBySystemCode("Ik");
				Set<Xref> unifiedIdXref = mapper.mapID(idXref, source);
				Iterator<Xref> iter = unifiedIdXref.iterator();
				while (iter.hasNext()){
					Xref unifiedId = (Xref) iter.next();
					String inchikey = unifiedId.getId();
					Resource inchiResource = model.createResource("https://identifiers.org/inchikey/" + inchikey);
					internalWPDataNodeResource.addProperty(Wp.bdbInChIKey, inchiResource);
					Resource neutralResource = model.createResource("https://identifiers.org/inchikey/" + inchikey.substring(0,inchikey.length()-1) + "N");
					internalWPDataNodeResource.addProperty(RDFS.seeAlso, neutralResource);
				}
			} catch (Exception exception) {
				System.out.println("InChIKey exception: " + exception.getMessage());
			}
		}
	}

	public static void getUnifiedInteractionIdentifiers(Model model, IDMapper  mapper, Xref idXref, Resource internalWPDataNodeResource) throws IDMapperException, UnsupportedEncodingException {
//		System.out.println(idXref);
		// Reactome
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"Re", "https://identifiers.org/reactome/", Wp.bdbReactome
		);
		// Rhea
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"Rh", "https://identifiers.org/rhea/", Wp.bdbRhea
		);
	}

	public static void getUnifiedComplexIdentifiers(Model model, IDMapper  mapper, Xref idXref, Resource internalWPDataNodeResource) throws IDMapperException, UnsupportedEncodingException {
//		System.out.println(idXref);
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"Cpx", "https://identifiers.org/complexportal/", Wp.bdbComplexPortal
		);
	}

	public static void getUnifiedLiteratureIdentifiers(Model model, IDMapper  mapper, Xref idXref, Resource internalWPDataNodeResource) throws IDMapperException, UnsupportedEncodingException {
//		System.out.println(idXref);
		// DOI
		outputBridgeDbMapping(model, mapper, idXref, internalWPDataNodeResource,
			"Pbd", "https://doi.org/", OWL.sameAs
		);
	}

	private static void outputBridgeDbMapping(Model model, IDMapper mapper, Xref idXref,
			Resource internalWPDataNodeResource, String sourceCode, String uriPrefix, Property predicate)
	throws IDMapperException, UnsupportedEncodingException {
		if (mapper == null) return; // OK, not BridgeDb mapping files; just return
		DataSource source = null;
		try {
			source = DataSource.getExistingBySystemCode(sourceCode);
		} catch (Exception exception) {
			System.out.println("Unknown system code (mapping files not loaded?): " + sourceCode);
			return;
		}
		// first, if we already have a $foo identifier, also output is as mapped identifier
		if (sourceCode.equals(idXref.getDataSource().getSystemCode())) {
			// here too, we need to make sure to output proper identifiers.org IRIs for ChEBI IDs.
			if ("Ce".equals(sourceCode)) {
				String nodeIdentifier = idXref.getId();
				if (nodeIdentifier.startsWith("CHEBI:")) {
					Resource unifiedlIdResource = model.createResource(uriPrefix+nodeIdentifier);
					internalWPDataNodeResource.addProperty(predicate, unifiedlIdResource);
				} else { // just digits
					nodeIdentifier = "CHEBI:" + nodeIdentifier;
					Resource unifiedlIdResource = model.createResource(uriPrefix+nodeIdentifier);
					internalWPDataNodeResource.addProperty(predicate, unifiedlIdResource);
				}
			} else {
				Resource unifiedlIdResource = model.createResource(uriPrefix + idXref.getId().replaceAll(" ", "_"));
				internalWPDataNodeResource.addProperty(predicate, unifiedlIdResource);
			}
		}
		// now, use BridgeDb to find additional identifiers
		Set<Xref> unifiedIdXref = mapper.mapID(idXref, source);
		Iterator<Xref> iter = unifiedIdXref.iterator();
		// the next if clause is to handle new HMDB00xxxxx-style identifiers and a BridgeDb
		// mapping file that does not recognize them yet (causing an empty iter)
		if (!iter.hasNext() && "Ch".equals(idXref.getDataSource().getSystemCode())) {
			String origIdentifier = idXref.getId();
			if (origIdentifier.length() == 11) { // HMDB00xxxxx -> HMDBxxxxx
				idXref = new Xref(origIdentifier.replace("HMDB00", "HMDB"), idXref.getDataSource());
				unifiedIdXref = mapper.mapID(idXref, source);
				iter = unifiedIdXref.iterator();
			}
		}
		while (iter.hasNext()){
			Xref unifiedId = (Xref) iter.next();
			if ("Ce".equals(sourceCode)) {
				String unifiedDataNodeIdentifier = unifiedId.getId();
				if (unifiedDataNodeIdentifier.startsWith("CHEBI:")) {
					Resource unifiedlIdResource = model.createResource(uriPrefix+unifiedDataNodeIdentifier);
					internalWPDataNodeResource.addProperty(predicate, unifiedlIdResource);
				} else { // just digits
					unifiedDataNodeIdentifier = "CHEBI:" + unifiedDataNodeIdentifier;
					Resource unifiedlIdResource = model.createResource(uriPrefix+unifiedDataNodeIdentifier);
					internalWPDataNodeResource.addProperty(predicate, unifiedlIdResource);
				}
			} else if ("Ch".equals(sourceCode)) {
				String unifiedDataNodeIdentifier = unifiedId.getId();
				if (unifiedDataNodeIdentifier.length() == 9) {
					unifiedDataNodeIdentifier = unifiedDataNodeIdentifier.replace("HMDB", "HMDB00");
				}
				Resource unifiedlIdResource = model.createResource(uriPrefix+unifiedDataNodeIdentifier);
				internalWPDataNodeResource.addProperty(predicate, unifiedlIdResource);
			} else if ("Pbd".equals(sourceCode)) {
				String dataNodeIdentifier = unifiedId.getId().toUpperCase();
				String unifiedDataNodeIdentifier = URLEncoder.encode(dataNodeIdentifier, "UTF-8");
  			    Resource unifiedlIdResource = model.createResource(uriPrefix+unifiedDataNodeIdentifier);
			    internalWPDataNodeResource.addProperty(predicate, unifiedlIdResource);
			    unifiedlIdResource.addProperty(DCTerms.identifier, model.createLiteral(dataNodeIdentifier));
			    unifiedlIdResource.addProperty(DC_11.source, model.createLiteral("DOI"));
			} else {
				String unifiedDataNodeIdentifier = URLEncoder.encode(unifiedId.getId(), "UTF-8");
  			    Resource unifiedlIdResource = model.createResource(uriPrefix+unifiedDataNodeIdentifier);
			    internalWPDataNodeResource.addProperty(predicate, unifiedlIdResource);
			}
			//createCHEMINFBits(model,
			//		internalWPDataNodeResource, CHEMINF.CHEMINF_000405, unifiedChemspiderDataNodeIdentifier
			//);
		}
	}

}
