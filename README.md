# RDF for MIBiG

Script based on Groovy and Bacting to convert MIBiG content into RDF.

```shell
wget https://dl.secondarymetabolites.org/mibig/mibig_json_2.0.tar.gz
tar zxvf mibig_json_2.0.tar.gz
groovy convert2rdf.groovy | tee mibig_json_2.0.ttl
```

## Use cases

### Match up with Wikidata

```shell
roqet -r csv sparql/allSMILESes.rq -D mibig_json_2.0.ttl > mibig.smi
```
