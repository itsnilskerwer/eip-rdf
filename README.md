The file EIPS.ttl contains an RDF dataset for a local SPARQL endpoint.

### Example Queries with SPARQL

Return all triples:
```sparql
CONSTRUCT WHERE { ?s ?p ?o . }
```
Return all EIPs and their creation date:

```sparql
CONSTRUCT {
      ?eip dcterms:identifier ?id ;
           dcterms:created ?date .
    }
    WHERE {
      ?eip a dcterms:Document ;
           dcterms:identifier ?id ;
           dcterms:created ?date .
    }"
```

Retrieve all authors:
```sparql
 CONSTRUCT {
      ?eip dcterms:identifier ?id ;
           foaf:maker ?author .
      ?author foaf:name ?authorName .
    }
    WHERE {
      ?eip a dcterms:Document ;
           dcterms:identifier ?id ;
           foaf:maker ?author .
           
      ?author foaf:name ?authorName .
    }"
```

Retrieve the latest EIP created:
 ```sparql
  PREFIX dcterms: <http://purl.org/dc/terms/>

    SELECT ?eip ?id ?date
    WHERE {
      ?eip a dcterms:Document ;
           dcterms:identifier ?id ;
           dcterms:created ?date .
    }
    ORDER BY DESC(?date)
    LIMIT 1"
```
Retrieve EIPs authored by a specific person:

```sparql
PREFIX dcterms: <http://purl.org/dc/terms/>
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>

    CONSTRUCT {
      ?eip dcterms:identifier ?id ;
           foaf:maker ?author .
      ?author foaf:name ?authorName .
    }
    WHERE {
      ?eip a dcterms:Document ;
           dcterms:identifier ?id ;
           foaf:maker ?author .
           
      ?author foaf:name ?authorName .
      FILTER (?authorName = 'Specific Author Name')
    }"
```

### Used vocabularies:
- RDF standard vocabulary (https://www.w3.org/TR/rdf11-schema/)
- FOAF (Friend of a Friend) an upper-level ontology (http://xmlns.com/foaf/spec/)
- DC (Dublin Core) and DCTerms (https://www.dublincore.org/specifications/dublin-core/dcmi-terms/)
- GIST an upper-level ontology (https://github.com/semanticarts/gist)

### Mental Model:

<img width="954" alt="Bildschirmfoto 2024-09-24 um 06 08 29" src="https://github.com/user-attachments/assets/05a7fcda-fe3c-42ef-9989-e1992e2a81b6">

! Under development ...
