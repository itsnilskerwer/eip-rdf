## Dataset 
! Only information about EIP-777 currently !

The file EIP-777.ttl contains the full RDF dataset used by the SPARQL endpoint.

The file EIPS.ttl contains the configuration (usually called assembler file).

## Querying the dataset
! query is the placeholder for your actual SPARQL query !

From a terminal, a query can be sent to the dataset like this:
```bash
curl -X POST \
  -H "Accept: text/turtle" \
  --data-urlencode "query=query" \
   http://13.49.44.222:3030/EIP-777/sparql
```
Alternatively, queries can be pasted into a browser search bar in this format
```bash
http://13.49.44.222:3030/EIP-777/sparql?query=query&format=text/turtle
```
Example Queries are found in files ending with .rq

## Source code for triplification of EIP data (MyCustomTriplifier.java)

Data is fetched from Github via http requests. This is not automated to update the dataset at this time. Feedback is highly appreciated.

## Used vocabularies:
- RDF standard vocabulary (https://www.w3.org/TR/rdf11-schema/)
- FOAF (Friend of a Friend) an upper-level ontology (http://xmlns.com/foaf/spec/)
- DC (Dublin Core) and
- DCTerms (https://www.dublincore.org/specifications/dublin-core/dcmi-terms/)
- GIST an upper-level ontology (https://github.com/semanticarts/gist)

## Mental Model:
<img width="950" alt="mental_model_rfc" src="https://github.com/user-attachments/assets/7d502ccf-2f7f-46bd-8f7e-f97b8348ca2c">


Disclaimer: Under development ...
