@Grab(group='io.github.egonw.bacting', module='managers-cdk', version='0.0.24')
@Grab(group='io.github.egonw.bacting', module='managers-inchi', version='0.0.24')

import groovy.io.FileType
import groovy.json.JsonSlurper

workspaceRoot = "."
def cdk = new net.bioclipse.managers.CDKManager(workspaceRoot);
def inchi = new net.bioclipse.managers.InChIManager(workspaceRoot);

File folder = new File('mibig_json_2.0/')

println "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
println "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
println "PREFIX cito: <http://purl.org/spar/cito/>"

println "PREFIX sso: <http://semanticscience.org/resource/>"
println "PREFIX smiles: <http://semanticscience.org/resource/CHEMINF_000018>"
println "PREFIX inchikey: <http://semanticscience.org/resource/CHEMINF_000059>"

println "PREFIX bigcat: <http://www.bigcat.unimaas.nl/mibig-rdf/onto/>"
println "PREFIX cluster: <http://www.bigcat.unimaas.nl/mibig-rdf/cluster/>"
println "PREFIX cmpd: <http://www.bigcat.unimaas.nl/mibig-rdf/compound/>"
println "PREFIX gene: <http://www.bigcat.unimaas.nl/mibig-rdf/gene/>"

println "PREFIX ncbitaxon: <http://www.identifiers.org/ncbitaxon/>"
println "PREFIX inchikeyuri: <http://www.identifiers.org/inchikey/>"
println "PREFIX pubmed: <http://www.identifiers.org/pubmed/>"
println ""

folder.eachFileRecurse FileType.FILES,  { file ->
  if (file.name.endsWith(".json")) {
    clusterID = file.name.replace(".json","")
    println "# Processing file ${clusterID}"
    def jsonSlurper = new JsonSlurper()
    def data = jsonSlurper.parseText(file.text)
    println "cluster:${clusterID} a bigcat:GeneCluster ;"

    // compounds
    cmpdData = "";
    data.cluster.compounds.each { compound ->
      smiles = compound.chem_struct
      // println "#  compound SMILES: ${smiles}"
      if (smiles) {
        try {
          mol = cdk.fromSMILES(smiles)
          molInChI = inchi.generate(mol)
          molURI = "inchikeyuri:${molInChI.key}"
          println "  bigcat:compound ${molURI} ;"
          smiles = smiles.replace("\\","\\\\")
          cmpdData += "${molURI} rdfs:label \"${compound.compound}\" ;\n" +
                      "  smiles: \"${smiles}\" ;\n" +
                      "  inchikey: \"${molInChI.key}\" .\n\n"
        } catch (Exception e) {} //ignore for now
      }
    }

    // genes
    genesData = "";
    if (data.cluster.genes) {
      data.cluster.genes.annotations.each { gene ->
        // println "#  gene: ${gene.id}"
        geneURI = "gene:${gene.id}"
        println "  bigcat:gene ${geneURI} ;"
        if (gene.name) {
          genesData += "${geneURI} rdfs:label \"${gene.name}\" .\n"
        } else {
            genesData += "${geneURI} rdfs:label \"${gene.id}\" .\n"
        }
      }
    }

    // publicatoins
    pubsData = "";
    if (data.cluster.publications) {
      data.cluster.publications.each { pub ->
        if (pub.startsWith("doi:")) {
          pub = "<https://doi.org/" + pub.substring(4) + ">"
        }
        println "  cito:cites ${pub} ;"
      }
    }

    println "  bigcat:taxon ncbitaxon:${data.cluster.ncbi_tax_id} .\n"
    println "ncbitaxon:${data.cluster.ncbi_tax_id} rdfs:label \"${data.cluster.organism_name}\" .\n"
    println cmpdData; println "";
    println genesData;
  }
}
