## Dataset 
The file EIPS.ttl contains an RDF dataset for a local SPARQL endpoint.
! Only information about EIP-777 currently !

## Querying the dataset
Using curl, a SPARQL query ("query" is the placeholder for your actual query) can be sent to the dataset like this:
```bash
curl -X POST \
  -H "Accept: text/turtle" \
  --data-urlencode "query=query" \
  http://localhost:3030/EIPS/sparql
```

Example Queries are found in files ending with .rq

## Source code for triplification of EIP data

Found in MyCustomTriplifier.java

## Used vocabularies:
- RDF standard vocabulary (https://www.w3.org/TR/rdf11-schema/)
- FOAF (Friend of a Friend) an upper-level ontology (http://xmlns.com/foaf/spec/)
- DC (Dublin Core) and
- DCTerms (https://www.dublincore.org/specifications/dublin-core/dcmi-terms/)
- GIST an upper-level ontology (https://github.com/semanticarts/gist)

## Mental Model:

<img width="954" alt="Bildschirmfoto 2024-09-24 um 06 08 29" src="https://github.com/user-attachments/assets/05a7fcda-fe3c-42ef-9989-e1992e2a81b6">


Disclaimer: Under development ...
