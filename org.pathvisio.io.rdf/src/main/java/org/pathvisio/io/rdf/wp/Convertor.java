// Copyright (c) 2015 BiGCaT Bioinformatics
//               2022-2025 Egon Willighagen <egon.willighagen@gmail.com>
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.pathvisio.io.rdf.ontologies.CITO;
import org.pathvisio.io.rdf.ontologies.Pav;
import org.pathvisio.io.rdf.ontologies.Wp;
import org.pathvisio.io.rdf.utils.Utils;
import org.pathvisio.libgpml.model.Annotation;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.Group;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.PathwayElement.CitationRef;
import org.pathvisio.libgpml.model.PathwayModel;

/**
 * Tool to convert a {@link Pathway} model into a Jena RDF model.
 */
public class Convertor {

	PathwayModel pathway;
	DataNodeConvertor dataNodeConvertor;
	InteractionConvertor interactionConvertor;
	GroupConvertor groupConvertor;

	// cached things
	String domainName;
	Resource pwyRes;
	Map<String, Resource> datanodes;

	public Convertor(PathwayModel pathway) throws Exception {
		this(pathway, Utils.WP_RDF_URL, null);
	}
	public Convertor(PathwayModel pathway, String domainName, IDMapperStack mapper) throws Exception {
		this.pathway = pathway;
		this.domainName = domainName;
		this.datanodes = new HashMap<>();
		dataNodeConvertor = new DataNodeConvertor(this, domainName, mapper);
		interactionConvertor = new InteractionConvertor(this, domainName, mapper);
		groupConvertor = new GroupConvertor(this, domainName);
	}

	public Model asRDF() {
		Model model = ModelFactory.createDefaultModel();

		// pathway
		pwyRes = generatePathwayResource(model);
		generateDataNodeResources(pathway.getDataNodes(), model);
		generateInteractionResources(pathway.getInteractions(), model);
		generateGroupResources(pathway.getGroups(), model);
		
		return model;
	}

	private void generateGroupResources(List<Group> groups, Model model) {
		String wpId = this.pathway.getPathway().getXref().getId();
		String revision = Utils.getRevisionFromVersion(wpId, pathway.getPathway().getVersion());

		for (Group group : groups) {
			groupConvertor.convertGroup(group, model, wpId, revision);
		}
	}

	private void generateInteractionResources(List<Interaction> interactions, Model model) {
		String wpId = this.pathway.getPathway().getXref().getId();
		String revision = Utils.getRevisionFromVersion(wpId, pathway.getPathway().getVersion());

		for (Interaction interaction : interactions) {
			interactionConvertor.convertInteraction(interaction, model, wpId, revision);
		}
	}

	private void generateDataNodeResources(List<DataNode> dataNodes, Model model) {
		String wpId = this.pathway.getPathway().getXref().getId();
		String revision = Utils.getRevisionFromVersion(wpId, pathway.getPathway().getVersion());

		for (DataNode node : dataNodes) {
			dataNodeConvertor.convertDataNode(node, model, wpId, revision);
		}
	}

	private Resource generatePathwayResource(Model model) {
		Pathway pathway = this.pathway.getPathway();
		String wpId = pathway.getXref().getId();
		String revision = Utils.getRevisionFromVersion(wpId, pathway.getVersion());

		Resource pwyRes = model.createResource(
			Utils.WP_RDF_URL.equals(this.domainName) ? Utils.IDENTIFIERS_ORG_URL + "/wikipathways/" + wpId + "_r" + revision
				: this.domainName + "/pathways/" + wpId + "_r" + revision
		);
		Resource pwyConceptRes = model.createResource(
			Utils.WP_RDF_URL.equals(this.domainName) ? Utils.IDENTIFIERS_ORG_URL + "/wikipathways/" + wpId
				: this.domainName + "/pathways/" + wpId
		);
		pwyConceptRes.addProperty(Pav.hasVersion, pwyRes);
		pwyRes.addProperty(RDF.type, Wp.Pathway);
		pwyRes.addProperty(RDF.type, SKOS.Collection);
		pwyRes.addProperty(DC_11.identifier, pwyConceptRes);
		pwyRes.addLiteral(DC_11.source, "WikiPathways");
		pwyRes.addLiteral(DCTerms.identifier, wpId);
		pwyRes.addLiteral(DC_11.title, model.createLiteral(pathway.getTitle(), "en"));
		if (pathway.getDescription() != null)
			pwyRes.addLiteral(DCTerms.description, pathway.getDescription());
		pwyRes.addProperty(Wp.isAbout, pwyRes);
		if (Utils.WP_RDF_URL.equals(this.domainName)) {
			pwyRes.addProperty(FOAF.page, model.createResource("http://www.wikipathways.org/instance/" + wpId + "_r" + revision));
		}

		Map<String,String> others = new HashMap<>();
		others.put("Abies grandis","46611");
		others.put("Acer pseudoplatanus","4026");
		others.put("Acetobacter subgen. Acetobacter","151157");
		others.put("Acinetobacter baylyi","202950");
		others.put("Actinidia chinensis","3625");
		others.put("Actinidia deliciosa","3627");
		others.put("Actinidia eriantha","165200");
		others.put("Adiantum capillus-veneris","13818");
		others.put("Adonis aestivalis","113211");
		others.put("Adonis annua","212759");
		others.put("Aegilops tauschii","37682");
		others.put("Agapanthus africanus", "51501");
		others.put("Albizia julibrissin","3813");
		others.put("Allium aflatunense","70752");
		others.put("Allium altaicum","48666");
		others.put("Allium altyncolicum","165602");
		others.put("Allium ampeloprasum","4681");
		others.put("Allium ascalonicum","1476995");
		others.put("Allium cepa","4679");
		others.put("Allium chinense","130426");
		others.put("Allium fistulosum","35875");
		others.put("Allium nutans","138328");
		others.put("Allium ochotense","669879");
		others.put("Allium sativum","4682");
		others.put("Allium schoenoprasum","74900");
		others.put("Allium tuberosum","4683");
		others.put("Allium ursinum","4684");
		others.put("Allium victorialis","88845");
		others.put("Aloe arborescens","45385");
		others.put("Aloe ferox","117798");
		others.put("Amaranthus cruentus","117272");
		others.put("Amaranthus hypochondriacus","28502");
		others.put("Amorpha fruticosa","48131");
		others.put("Anaerotignum propionicum","28446");
		others.put("Anchusa officinalis","89630");
		others.put("Anethum foeniculum","2849586");
		others.put("Anigozanthos preissii","95948");
		others.put("Anisodus acutangulus","402998");
		others.put("Anthriscus sylvestris","48027");
		others.put("Antirrhinum majus","4151");
		others.put("Aphelandra squarrosa","103766");
		others.put("Apium graveolens","4045");
		others.put("Aquifex aeolicus","63363");
		others.put("Aquilaria crassna","223751");
		others.put("Aquilegia vulgaris","3451");
		others.put("Arabidopsis lyrata","59689");
		others.put("Arachis hypogaea","3818");
		others.put("Archaeoglobus fulgidus","2234");
		others.put("Artemisia annua", "35608");
		others.put("Asclepias syriaca","48545");
		others.put("Asparagus officinalis","4686");
		others.put("Aspergillus terricola","36642");
		others.put("Astragalus bisulcatus","20406");
		others.put("Atractylodes lancea","41486");
		others.put("Atropa belladonna","33113");
		others.put("Auxenochlorella pyrenoidosa","3078");
		others.put("Avena clauda","83523");
		others.put("Avena longiglumis","4500");
		others.put("Avena prostrata","279683");
		others.put("Avena sativa","4498");
		others.put("Avena strigosa","38783");
		others.put("Avena ventricosa","146535");
		others.put("Bacillus anthracis","1392");
		others.put("Barnadesia spinosa","171760");
		others.put("Batis maritima","4436");
		others.put("Berberis stolonifera","33814");
		others.put("Betula pubescens","38787");
		others.put("Bifidobacterium longum","206672");
		others.put("Bixa orellana","66672");
		others.put("Botryococcus braunii","1202541");
		others.put("Brassica juncea","3707");
		others.put("Brassica nigra","3710");
		others.put("Brassica oleracea","3712");
		others.put("Brassica rapa","3711");
		others.put("Bromus inermis","15371");
		others.put("Bruguiera gymnorhiza","39984");
		others.put("Camellia irrawadiensis","153142");
		others.put("Camellia ptilophylla","319931");
		others.put("Camellia sinensis","4442");
		others.put("Camellia taliensis","182317");
		others.put("Camptotheca acuminata","16922");
		others.put("Canavalia ensiformis","3823");
		others.put("Canavalia lineata","28957");
		others.put("Cannabis sativa","3483");
		others.put("Capsicum annuum","4072");
		others.put("Capsicum baccatum","33114");
		others.put("Capsicum chinense","80379");
		others.put("Capsicum frutescens","4073");
		others.put("Carapichea ipecacuanha","77880");
		others.put("Carica papaya","3649");
		others.put("Carpobrotus acinaciformis","1053334");
		others.put("Carthamus tinctorius","4222");
		others.put("Carum carvi","48032");
		others.put("Catharanthus roseus","4058");
		others.put("Celosia cristata","124768");
		others.put("Centaurium erythraea","172057");
		others.put("Cephalocereus senilis","223054");
		others.put("Cerastium arvense","271558");
		others.put("Ceratodon purpureus","3225");
		others.put("Cereibacter sphaeroides","1063");
		others.put("Cestrum elegans","103475");
		others.put("Chelidonium majus","71251");
		others.put("Chlamydia trachomatis","759363");
		others.put("Chlamydomonas reinhardtii","3055");
		others.put("Chrysanthemum × morifolium","41568");
		others.put("Chrysosplenium americanum","36749");
		others.put("Cicer arietinum","3827");
		others.put("Cichorium intybus","13427");
		others.put("Cinchona calisaya","153742");
		others.put("Cinchona macrocalyx","273779");
		others.put("Cinchona mutisii","273780");
		others.put("Cinchona officinalis","273781");
		others.put("Cinchona pitayensis","128294");
		others.put("Cinchona pubescens","50278");
		others.put("Cinnamomum tenuipile","192326");
		others.put("Citrullus lanatus","3654");
		others.put("Citrus hanaju","481547");
		others.put("Citrus japonica","76966");
		others.put("Citrus junos","135197");
		others.put("Citrus maxima","37334");
		others.put("Citrus trifoliata","37690");
		others.put("Citrus unshiu","55188");
		others.put("Citrus × aurantium","43166");
		others.put("Citrus × clementina","85681");
		others.put("Citrus × microcarpa","164113");
		others.put("Citrus × paradisi","37656");
		others.put("Clarkia breweri","36903");
		others.put("Cleretum bellidiforme","90527");
		others.put("Clitoria ternatea","43366");
		others.put("Clostridium acetobutylicum","1488");
		others.put("Clostridium botulinum","1491");
		others.put("Clostridium kluyveri","1534");
		others.put("Coffea abeokutae","213304");
		others.put("Coffea canephora","49390");
		others.put("Coffea eugenioides","49369");
		others.put("Coffea liberica","49373");
		others.put("Coix lacryma-jobi","4505");
		others.put("Coleus scutellarioides","4142");
		others.put("Consolida orientalis","565971");
		others.put("Coptis chinensis","261450");
		others.put("Coptis japonica","3442");
		others.put("Coptis teeta","261448");
		others.put("Corallococcus coralloides","184914");
		others.put("Coreopsis grandiflora","13449");
		others.put("Corydalis vaginans","3044017");
		others.put("Crambe hispanica","70124");
		others.put("Crepis palaestina","72611");
		others.put("Crocus sativus","82528");
		others.put("Croton stellatopilosus","431156");
		others.put("Croton sublyratus","107238");
		others.put("Cryptomeria japonica","3369");
		others.put("Cucumis melo","3656");
		others.put("Cucumis sativus","3659");
		others.put("Cucurbita maxima","3661");
		others.put("Cucurbita pepo","3663");
		others.put("Curcuma longa","136217");
		others.put("Cyanidioschyzon merolae","45157");
		others.put("Cystobacter fuscus","43");
		others.put("Cytophaga hutchinsonii","985");
		others.put("Dahlia pinnata","101596");
		others.put("Daphne odora","329675");
		others.put("Datura inoxia","4075");
		others.put("Datura stramonium","4076");
		others.put("Daucus carota","4039");
		others.put("Davallia trichomanoides","328206");
		others.put("Delftia acidovorans","80866");
		others.put("Delphinium grandiflorum","85439");
		others.put("Derris elliptica","56063");
		others.put("Desmodium uncinatum","225101");
		others.put("Dianthus caryophyllus","3570");
		others.put("Dicranum scoparium","3222");
		others.put("Dictyostelium discoideum","44689");
		others.put("Digitalis lanata","49450");
		others.put("Digitalis purpurea","4164");
		others.put("Diospyros kaki","35925");
		others.put("Dolichandra unguis-cati","73871");
		others.put("Enterococcus faecalis","1351");
		others.put("Equisetum arvense","3258");
		others.put("Erwinia amylovora","552");
		others.put("Erythroxylum coca", "289672");
		others.put("Eschscholzia californica","3467");
		others.put("Eucalyptus piperita","87677");
		others.put("Euglena gracilis","3039");
		others.put("Euonymus alatus","4307");
		others.put("Euphorbia lagascae","54672");
		others.put("Eustoma grandiflorum","52518");
		others.put("Fagopyrum esculentum","3617");
		others.put("Fagopyrum tataricum","62330");
		others.put("Fagus crenata","28929");
		others.put("Felis catus","9685");
		others.put("Flaveria bidentis","4224");
		others.put("Flaveria chlorifolia","4228");
		others.put("Fluviicola taffensis","191579");
		others.put("Forsythia koreana","205692");
		others.put("Forsythia × intermedia","55183");
		others.put("Fragaria × ananassa","3747");
		others.put("Galanthus elwesii","82232");
		others.put("Galium mollugo","254777");
		others.put("Gardenia jasminoides","114476");
		others.put("Gemmata obscuriglobus","114");
		others.put("Gentiana straminea","50768");
		others.put("Gentiana triflora","55190");
		others.put("Gerbera hybrid","18101");
		others.put("Ginkgo biloba","3311");
		others.put("Glandularia × hybrida","76714");
		others.put("Glebionis segetum","118509");
		others.put("Glycyrrhiza echinata","46348");
		others.put("Glycyrrhiza glabra","49827");
		others.put("Gossypium arboreum","29729");
		others.put("Gossypium barbadense","3634");
		others.put("Gossypium hirsutum","3635");
		others.put("Guatteria blepharophylla","402568");
		others.put("Guatteria friesiana","402569");
		others.put("Guatteria hispida","402570");
		others.put("Haematococcus lacustris","44745");
		others.put("Halobacterium salinarum","2242");
		others.put("Haloferax volcanii","2246");
		others.put("Helianthus annuus","4232");
		others.put("Helianthus tuberosus","4233");
		others.put("Helicobacter pylori","210");
		others.put("Hevea brasiliensis","3981");
		others.put("Hordeum lechleri","38856");
		others.put("Hydrangea macrophylla","23110");
		others.put("Hyoscyamus albus","310458");
		others.put("Hyoscyamus muticus","35626");
		others.put("Hyoscyamus niger","4079");
		others.put("Hypericum androsaemum","140968");
		others.put("Hypericum calycinum","55963");
		others.put("Hypericum perforatum","65561");
		others.put("Hyphomicrobium zavarzinii","48292");
		others.put("Impatiens balsamina","63779");
		others.put("Ipomoea batatas","4120");
		others.put("Ipomoea nil","35883");
		others.put("Ipomoea purpurea","4121");
		others.put("Juglans regia","51240");
		others.put("Kandelia candel","61147");
		others.put("Klebsiella oxytoca","571");
		others.put("Klebsiella pneumoniae","1284798");
		others.put("Lacticaseibacillus casei","1312920");
		others.put("Lactococcus lactis","1358");
		others.put("Lactuca sativa","4236");
		others.put("Lamium galeobdolon","53161");
		others.put("Lathyrus odoratus","3859");
		others.put("Lavandula angustifolia","39329");
		others.put("Lawsonia inermis","141191");
		others.put("Lemna aequinoctialis","89585");
		others.put("Lemna minor","4472");
		others.put("Lens culinaris","3864");
		others.put("Leucaena leucocephala","3866");
		others.put("Lilium longiflorum","4690");
		others.put("Limnanthes alba","42439");
		others.put("Limnanthes douglasii","28973");
		others.put("Limonium latifolium","227291");
		others.put("Linum flavum","407263");
		others.put("Linum nodiflorum","407264");
		others.put("Linum perenne","35941");
		others.put("Linum usitatissimum","4006");
		others.put("Lithospermum erythrorhizon","34254");
		others.put("Lotus corniculatus","47247");
		others.put("Lotus japonicus","34305");
		others.put("Loxodonta africana","9785");
		others.put("Lunaria annua","153659");
		others.put("Lupinus albus","3870");
		others.put("Lupinus angustifolius","3871");
		others.put("Lupinus luteus","3873");
		others.put("Lupinus polyphyllus","3874");
		others.put("Lygodium circinatum","84615");
		others.put("Magnolia grandiflora","3406");
		others.put("Magnolia obovata","349509");
		others.put("Malus domestica","3750");
		others.put("Malus hupehensis","106556");
		others.put("Malus pumila","283210");
		others.put("Manihot esculenta","3983");
		others.put("Matthiola incana","3724");
		others.put("Medicago sativa","3879");
		others.put("Megathyrsus maximus","59788");
		others.put("Melilotus albus","47082");
		others.put("Mentha aquatica","190902");
		others.put("Mentha spicata","29719");
		others.put("Mentha × gracilis","241069");
		others.put("Mentha × piperita","34256");
		others.put("Methanocaldococcus jannaschii","2190");
		others.put("Methanosarcina mazei","1434114");
		others.put("Methanosarcina thermophila","2210");
		others.put("Methanothermobacter marburgensis","145263");
		others.put("Methyloceanibacter caenitepidi","1384459");
		others.put("Methylococcus capsulatus","414");
		others.put("Methylorubrum extorquens","408");
		others.put("Methylosphaera hansonii","51353");
		others.put("Micrococcus luteus","1270");
		others.put("Mirabilis jalapa","3538");
		others.put("Momordica charantia","3673");
		others.put("Musa acuminata","4641");
		others.put("Mycobacterium avium","1764");
		others.put("Mycobacterium kansasii","1768");
		others.put("Mycolicibacterium fortuitum","1766");
		others.put("Mycolicibacterium phlei","1771");
		others.put("Mycoplasmoides pneumoniae","1263835");
		others.put("Nannocystis exedens","54");
		others.put("Narcissus pseudonarcissus","39639");
		others.put("Nepenthes alata","4376");
		others.put("Nepenthes gracilis","150966");
		others.put("Nepenthes mirabilis","150983");
		others.put("Nepenthes rafflesiana","150990");
		others.put("Nerine bowdenii","59042");
		others.put("Neurospora crassa","5141");
		others.put("Nicotiana attenuata","49451");
		others.put("Nicotiana benthamiana","4100");
		others.put("Nicotiana glutinosa","35889");
		others.put("Nicotiana plumbaginifolia","4092");
		others.put("Nicotiana rustica","4093");
		others.put("Nicotiana sylvestris","4096");
		others.put("Nicotiana tabacum","4097");
		others.put("Nitrosopumilus maritimus","338192");
		others.put("Nothapodytes nimmoniana","159386");
		others.put("Ocimum basilicum", "39350");
		others.put("Olea europaea","4146");
		others.put("Ophiorrhiza japonica","367363");
		others.put("Ophiorrhiza pumila","157934");
		others.put("Oxybasis rubra","3560");
		others.put("Panax ginseng","4054");
		others.put("Panax notoginseng","44586");
		others.put("Panicum miliaceum","4540");
		others.put("Panicum virgatum","38727");
		others.put("Pantoea agglomerans","549");
		others.put("Pantoea ananatis","553");
		others.put("Papaver somniferum", "3469");
		others.put("Passiflora edulis","78168");
		others.put("Pelargonium crispum","1417776");
		others.put("Pericallis cruenta","98709");
		others.put("Persea americana","3435");
		others.put("Persicaria tinctoria","96455");
		others.put("Petiveria alliacea","46142");
		others.put("Petroselinum crispum","4043");
		others.put("Petunia x hybrida", "4102");
		others.put("Phaseolus coccineus","3886");
		others.put("Phaseolus lunatus","3884");
		others.put("Phaseolus vulgaris", "3885");
		others.put("Phlebodium aureum","218620");
		others.put("Phleum pratense","15957");
		others.put("Phragmites australis","29695");
		others.put("Physaria fendleri","63442");
		others.put("Physaria lindheimeri","439687");
		others.put("Physcomitrium patens","3218");
		others.put("Picea abies","3329");
		others.put("Picea glauca","3330");
		others.put("Pimpinella anisum","271192");
		others.put("Pinus banksiana","3353");
		others.put("Pinus contorta","3339");
		others.put("Pinus densiflora","77912");
		others.put("Pinus ponderosa","55062");
		others.put("Pinus sabiniana","268869");
		others.put("Pinus strobus","3348");
		others.put("Pinus sylvestris","3349");
		others.put("Pinus taeda","");
		others.put("Pisum sativum","3888");
		others.put("Plantago major","29818");
		others.put("Plectranthus barbatus","41228");
		others.put("Plumbago europaea","114226");
		others.put("Plumbago indica","122308");
		others.put("Podophyllum peltatum","35933");
		others.put("Pogostemon cablin","28511");
		others.put("Polaribacter filamentus","53483");
		others.put("Populus alba","43335");
		others.put("Populus deltoides","3696");
		others.put("Populus nigra","3691");
		others.put("Portulaca grandiflora","3583");
		others.put("Prunus dulcis","3755");
		others.put("Prunus mume","102107");
		others.put("Prymnesium parvum", "97485");
		others.put("Pseudomonas aeruginosa","1009714");
		others.put("Pseudomonas fluorescens","294");
		others.put("Psilotum nudum","3240");
		others.put("Pteris vittata","13821");
		others.put("Pueraria montana","132459");
		others.put("Punica granatum","22663");
		others.put("Pyrus pyrifolia","3767");
		others.put("Quercus robur","38942");
		others.put("Quercus rubra","3512");
		others.put("Raphanus sativus","3726");
		others.put("Rheum palmatum","137221");
		others.put("Rheum tataricum","205071");
		others.put("Rhizophora apiculata","106626");
		others.put("Rhizophora mangle","40031");
		others.put("Rhizophora stylosa","98588");
		others.put("Rhodiola rosea","203015");
		others.put("Rhodiola sachalinensis","265354");
		others.put("Rhodobacter capsulatus","1061");
		others.put("Rhodotorula glutinis","5535");
		others.put("Rhus typhina","255348");
		others.put("Ricinus communis","3988");
		others.put("Robinia pseudoacacia","35938");
		others.put("Rosa chinensis","74649");
		others.put("Rosa hybrid","128735");
		others.put("Rubia tinctorum","29802");
		others.put("Rubus idaeus","32247");
		others.put("Rudbeckia hirta","52299");
		others.put("Ruta graveolens","37565");
		others.put("Saccharopolyspora spinosa","60894");
		others.put("Saccharum officinarum","4547");
		others.put("Salmonella enterica","28901");
		others.put("Salvia fruticosa","268906");
		others.put("Salvia officinalis","38868");
		others.put("Salvia rosmarinus","39367");
		others.put("Salvia splendens","180675");
		others.put("Sanguinaria canadensis","3472");
		others.put("Santalum album","35974");
		others.put("Santalum austrocaledonicum","293154");
		others.put("Santalum spicatum","453088");
		others.put("Sarcina ventriculi","1267");
		others.put("Saussurea medusa","137893");
		others.put("Saxifraga stolonifera","182070");
		others.put("Schizonepeta tenuifolia","2849020");
		others.put("Schizosaccharomyces pombe","4896");
		others.put("Scutellaria baicalensis","65409");
		others.put("Scutellaria viscidula","512023");
		others.put("Secale cereale","4550");
		others.put("Selaginella lepidophylla","59777");
		others.put("Senecio vernalis","93496");
		others.put("Senecio vulgaris","76276");
		others.put("Serratia marcescens","1401254");
		others.put("Sesamum alatum","300844");
		others.put("Sesamum indicum","4182");
		others.put("Sesamum radiatum","300843");
		others.put("Sesbania rostrata","3895");
		others.put("Setaria italica","4555");
		others.put("Silene dioica","39879");
		others.put("Silene latifolia","37657");
		others.put("Simmondsia chinensis","3999");
		others.put("Sinningia cardinalis","189007");
		others.put("Sinopodophyllum hexandrum","93608");
		others.put("Solanum aculeatissimum","267265");
		others.put("Solanum habrochaites","62890");
		others.put("Solanum melongena","4111");
		others.put("Solanum pennellii","28526");
		others.put("Solanum tuberosum","4113");
		others.put("Solidago canadensis","59297");
		others.put("Sorbus aucuparia","36599");
		others.put("Spinacia oleracea","3562");
		others.put("Spirodela polyrhiza","29656");
		others.put("Sporobolus alterniflorus","29706");
		others.put("Stellaria media","13274");
		others.put("Stigmatella aurantiaca","41");
		others.put("Streptococcus mutans","1309");
		others.put("Streptococcus pneumoniae","1001746");
		others.put("Streptomyces antibioticus","1890");
		others.put("Streptomyces griseus","1911");
		others.put("Strobilanthes cusia","222567");
		others.put("Syntrophotalea acetylenica","29542");
		others.put("Syzygium aromaticum","219868");
		others.put("Tagetes erecta","13708");
		others.put("Tagetes patula","55843");
		others.put("Tanacetum balsamita","301877");
		others.put("Tanacetum vulgare","128002");
		others.put("Taxus baccata","25629");
		others.put("Taxus brevifolia","46220");
		others.put("Taxus chinensis","29808");
		others.put("Taxus cuspidata","99806");
		others.put("Tellima grandiflora","29775");
		others.put("Tetradesmus obliquus","3088");
		others.put("Thalassiosira pseudonana","35128");
		others.put("Thalictrum flavum","150094");
		others.put("Thalictrum tuberosum","79802");
		others.put("Thermococcus kodakarensis","311400");
		others.put("Thermotoga maritima","2336");
		others.put("Thuja plicata","3316");
		others.put("Trichosanthes kirilowii","3677");
		others.put("Trifolium pratense","57577");
		others.put("Trifolium repens","3899");
		others.put("Triglochin maritima", "55501");
		others.put("Triticum spelta","58933");
		others.put("Triticum urartu","4572");
		others.put("Ulva curvata", "135247");
		others.put("Ulva intestinalis","3116");
		others.put("Ulva lactuca","63410");
		others.put("Urochloa panicoides","37563");
		others.put("Vaccinium myrtillus","180763");
		others.put("Vanilla planifolia","51239");
		others.put("Vernicia fordii","73154");
		others.put("Vibrio cholerae","1225783");
		others.put("Vibrio cholerae","1225783");
		others.put("Vibrio cholerae","1225783");
		others.put("Vibrio furnissii","29494");
		others.put("Vicia faba","3906");
		others.put("Vicia sativa","3908");
		others.put("Vigna aconitifolia","3918");
		others.put("Vigna angularis","3914");
		others.put("Vigna radiata","157791");
		others.put("Vigna unguiculata","3917");
		others.put("Wachendorfia thyrsiflora","95970");
		others.put("Xanthomonas arboricola","56448");
		others.put("Xanthomonas axonopodis","53413");
		others.put("Zea luxurians","15945");
		others.put("Zingiber officinale","94328");
		others.put("Zingiber zerumbet","311405");
		others.put("Zymomonas mobilis","542");

		// organism info
		String organism = pathway.getOrganism();
		if (organism.contains(",")) {
			String[] organismStrings = organism.split(",");
			for (String singleOrganism : organismStrings) {
				singleOrganism = singleOrganism.trim();
				String taxonID = Organism.fromLatinName(singleOrganism) != null ?
						Organism.fromLatinName(singleOrganism).taxonomyID().getId() :
							others.get(singleOrganism);
				if (taxonID == null) {
					System.out.println("Unknown taxon: " + singleOrganism);
					taxonID = "131567"; // cellular organisms
				}
				pwyRes.addLiteral(Wp.organismName, singleOrganism);
				Resource organismRes = model.createResource("http://purl.obolibrary.org/obo/NCBITaxon_" + taxonID);
				pwyRes.addProperty(Wp.organism, organismRes);
				organismRes.addProperty(model.createProperty("http://purl.obolibrary.org/obo/NCIT_C179773"), taxonID);
			}
		} else {
			String taxonID = Organism.fromLatinName(organism) != null ?
					Organism.fromLatinName(organism).taxonomyID().getId() :
						others.get(organism);
			if (taxonID == null) taxonID = "131567"; // cellular organisms
			pwyRes.addLiteral(Wp.organismName, pathway.getOrganism());
			Resource organismRes = model.createResource("http://purl.obolibrary.org/obo/NCBITaxon_" + taxonID);
			pwyRes.addProperty(Wp.organism, organismRes);
			organismRes.addProperty(model.createProperty("http://purl.obolibrary.org/obo/NCIT_C179773"), taxonID);
		}

		// ontology tags
		for (Annotation annot : this.pathway.getAnnotations()) {
			if (annot.getXref() != null) {
				String ontoTag = annot.getXref().getDataSource().getSystemCode() + "_" + annot.getXref().getId();
				ontoTag = ontoTag.replace("Do_", "DOID_");
				ontoTag = ontoTag.replace("cl_", "CL_");
				pwyRes.addProperty(Wp.ontologyTag, model.createResource(Utils.PURL_OBO_LIB + ontoTag));
				if (ontoTag.contains("PW_")) {
					pwyRes.addProperty(Wp.pathwayOntologyTag, model.createResource(Utils.PURL_OBO_LIB + ontoTag));
				} else if (ontoTag.contains("DOID_")) {
					pwyRes.addProperty(Wp.diseaseOntologyTag, model.createResource(Utils.PURL_OBO_LIB + ontoTag));
				} else if (ontoTag.contains("CL_")) {
					pwyRes.addProperty(Wp.cellTypeOntologyTag, model.createResource(Utils.PURL_OBO_LIB + ontoTag));
				}
			}
		}

		// references
		for (CitationRef ref : pathway.getCitationRefs()) {
			Xref citationXref = ref.getCitation().getXref();
			String fullName = citationXref.getDataSource().getFullName();
			if ("PubMed".equals(fullName) || "DOI".equals(fullName)) {
				addCitation(model, pwyRes, citationXref);
			}
		}

		// image
		if (Utils.WP_RDF_URL.equals(this.domainName)) {
			Resource pngRes = model.createResource("https://www.wikipathways.org//wpi/wpi.php?action=downloadFile&type=png&pwTitle=Pathway:" + wpId + "&oldid=r" + revision);
			pwyRes.addProperty(FOAF.img, pngRes);
			pngRes.addProperty(RDF.type, FOAF.Image);
			pngRes.addLiteral(DCTerms.format, "image/png");
			Resource svgRes = model.createResource("https://www.wikipathways.org//wpi/wpi.php?action=downloadFile&type=svg&pwTitle=Pathway:" + wpId + "&oldid=r" + revision);
			pwyRes.addProperty(FOAF.img, svgRes);
			svgRes.addProperty(RDF.type, FOAF.Image);
			svgRes.addLiteral(DCTerms.format, "image/svg+xml");
		}
 
		return pwyRes;
	}

	protected void addCitation(Model model, Resource resource, Xref citationXref) {
		String fullName = citationXref.getDataSource().getFullName();
		if ("PubMed".equals(fullName)) {
			String pmid = citationXref.getId().trim();
			try {
				Integer.parseInt(pmid);
				Resource pmResource = model.createResource(Utils.IDENTIFIERS_ORG_URL + "/pubmed/" + pmid);
				pmResource.addProperty(RDF.type, Wp.PublicationReference);
				pmResource.addProperty(DC.source, fullName);
				pmResource.addLiteral(DCTerms.identifier, pmid);
				pmResource.addProperty(FOAF.page, model.createResource("http://www.ncbi.nlm.nih.gov/pubmed/" + pmid));
				pmResource.addProperty(DCTerms.isPartOf, resource);
				resource.addProperty(DCTerms.references, pmResource);
				resource.addProperty(CITO.cites, pmResource);
				this.pwyRes.addProperty(CITO.cites, pmResource);
			} catch (Exception e) {} // not an integer
		}
	}

}
