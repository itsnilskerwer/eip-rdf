The file EIPS.ttl contains an RDF dataset for a local SPARQL endpoint.

Using curl, a SPARQL query ("query" is the placeholder for your actual query) can be sent to the dataset like this:
```bash
curl -X POST \
  -H "Accept: text/turtle" \
  --data-urlencode "query=query" \
  http://localhost:3030/EIPS/sparql
```

### Example Queries with SPARQL

Return all triples:
```sparql
CONSTRUCT WHERE { ?s ?p ?o . }
```

Return all properties of a Document:

```sparql
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX gist: <https://ontologies.semanticarts.com/o/gistCore/>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
CONSTRUCT {
  ?eip a foaf:Document .
  ?eip ?p ?o .
}
WHERE {
  ?eip a foaf:Document .
  ?eip ?p ?o .
}
```

Retrieve all authors:
```sparql
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX gist: <https://ontologies.semanticarts.com/o/gistCore/>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
CONSTRUCT { ?document gist:hasParticipant ?participant . }
WHERE {
 ?document a foaf:Document .
 ?document gist:hasParticipant ?participant .
}
```

Retrieve the latest commit towards a Document:
```sparql
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX gist: <https://ontologies.semanticarts.com/o/gistCore/>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
CONSTRUCT { ?document gist:latestCommit ?commit . ?commit dc:date ?latestCommitDate . }
WHERE { ?document a foaf:Document . {
  SELECT ?document (MAX(?commitDate) AS ?latestCommitDate)
    WHERE { ?commit a gist:Event ; dcterms:isVersionOf ?document ; dc:date ?commitDate . }
    GROUP BY ?document
} ?commit a gist:Event ; dcterms:isVersionOf ?document ; dc:date ?latestCommitDate . }
```


### More complex queries
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
    }
```

### Used vocabularies:
- RDF standard vocabulary (https://www.w3.org/TR/rdf11-schema/)
- FOAF (Friend of a Friend) an upper-level ontology (http://xmlns.com/foaf/spec/)
- DC (Dublin Core) and
- DCTerms (https://www.dublincore.org/specifications/dublin-core/dcmi-terms/)
- GIST an upper-level ontology (https://github.com/semanticarts/gist)

### Mental Model:

<img width="954" alt="Bildschirmfoto 2024-09-24 um 06 08 29" src="https://github.com/user-attachments/assets/05a7fcda-fe3c-42ef-9989-e1992e2a81b6">

Disclaimer: Under development ...
