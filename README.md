*************************** Project abandoned (for now) ***********************


##### What have i learned (TLDR)

- Triplestores with SPARQL endpoints can be easily set up with Apache Jena Fuseki (either locally via Docker or publicly via a free-tier EC2) even for non-experienced users.
- Converting Github data into RDF is made easy with Java libraries.
- Usefulness requires some skills on how to formulate SPARQL queries, given the mental model and the research question
- Combination of Github with SPARQL nevertheless seems interesting (e.g. A repo with RDF data immediately allows for shared collaboration on writing and querying the dataset).

##### Feedback and Ideas appreciated
- The code is far from optimized (EIP 5 seemed to have some kind of issue)
- The mental model has room to grow richer
- Conversion of further file formats not attempted

****************************************************************************************
----------------------------------------------------------------------------------------

## Dataset 
! Only information about EIP-777 currently !

EIP-777.ttl contains the RDF dataset used by the SPARQL endpoint.

EIPS.ttl contains the configuration (also called assembler file).

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


## Querying the dataset
! This assumes your public instance is up and running !
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


## A website full of resources

https://itsnilskerwer.github.io

Last update October 4th 2024. Disclaimer: Project Under development ...

## Shoutout 

For step-by-step instructions to host on AWS EC2 free-tier, and a bunch of interesting blog posts see https://www.bobdc.com 

