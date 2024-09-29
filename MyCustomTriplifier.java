package rfc.eth.java;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
// import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.DCTerms;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// todos:
// - iterate over all files test
// - rdfs inference test
// - Handle body content of markdown files (rdfs:comment or dcterms:description?) and implement text search
// - Edge cases for author names and social media links and emails (see ex: Sam Wilson, eip-5)

// For reference, the base URI for the GitHub repository of Ethereum inside the EIPS folder
// https://github.com/ethereum/EIPs/blob/master/EIPS/
// and the ERC repo
// https://github.com/ethereum/ercs/blob/master/ERCS/

public class MyCustomTriplifier {
    private static final String EIP_REPO = "https://github.com/ethereum/EIPs/blob/master/EIPS/";
    private static final String ERC_REPO = "https://github.com/ethereum/ercs/blob/master/ERCS/";
    private static final String EIP_API_URL = "https://api.github.com/repos/ethereum/EIPs/contents/EIPS/";
    private static final String ERC_API_URL = "https://api.github.com/repos/ethereum/ercs/contents/ERCS/";
    private static final String FOAF_URI = "http://xmlns.com/foaf/0.1/";
    private static final String GIST_URI = "https://ontologies.semanticarts.com/o/gistCore/";

    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize RDF model
        Model model = ModelFactory.createDefaultModel();
        // Add the some prefixes manually because it is not in apache.jena.vocabulary package
        model.setNsPrefix("foaf", FOAF_URI);
        model.setNsPrefix("gist", GIST_URI);
        model.setNsPrefix("dcterms", DCTerms.NS);
        model.setNsPrefix("dc", DC.NS);
        // model.setNsPrefix("rdfs", RDFS.getURI());

        // Some basic inference with RDFS tested :
        // gist:Delta is subclass of gist:Event
        // model.createResource(GIST_URI + "Delta")
        //     .addProperty(RDFS.subClassOf, model.createResource(GIST_URI + "Event"));

            
        String[] markdownFiles = { "eip-777.md" }; // 778 , 7749  "eip-100.md", "eip-8.md"

        for (String fileName : markdownFiles) {
            System.out.println("Processing: " + fileName);
            String apiUrl = EIP_API_URL + fileName;
            String fileUrl = EIP_REPO + fileName;
            
            // ---------------- EXTRACT DATA FOR MD DOC and related commits ---------------------
            // Fetch the file metadata and content from GitHub API using custom URL (see Github Rest API docs: https://docs.github.com/en/rest/repos/contents?apiVersion=2022-11-28)
            processMarkdownFile(fileName, model, apiUrl, fileUrl);
        }
        
        // ---------------- LOAD DATA (?)---------------------
        // Save the RDF model to a Turtle file
        try (FileWriter out = new FileWriter("EIPS.ttl")) {
            model.write(out, "TURTLE");
            out.close();
        }
    }

        // ----------------- RESOURCE CREATION -----------------
                     
        // ---------------- TRANSFORM DATA ---------------------

                // ----------------- Author resource -----------------------
    private static Resource createAuthorResource(Model model, String authorURI, String authorName, String authorEmail, Resource resource) {
        Resource authorResource = model.createResource(authorURI)
        .addProperty(RDF.type, model.createResource(GIST_URI + "Agent"));
            
            // add name and email
                authorResource.addProperty(model.createProperty(FOAF_URI, "name"), authorName);
                authorResource.addProperty(model.createProperty(FOAF_URI, "mbox"), "mailto:" + authorEmail);
            
            // link back to markdown file
            authorResource.addProperty(model.createProperty(GIST_URI, "editorOf"), resource);
            
            return authorResource;
        }

                // --------------------- Commit Resource ------------------

    private static Resource createCommitResource(Model model, JSONObject commit, Resource markdownFile) {
        JSONObject commitAuthor = commit.getJSONObject("commit").getJSONObject("author");
        JSONObject author = commit.optJSONObject("author"); // Use optJSONObject to handle possible null values
        String authorURI = author != null ? author.getString("html_url") : "https://github.com/FurtherUnspecifiedAuthors";
        String commitURI = commit.getString("html_url");
        String commitDate = commitAuthor.getString("date");
        String authorName = commitAuthor.getString("name");
        String authorEmail = commitAuthor.getString("email");
        String commitMessage = commit.getJSONObject("commit").getString("message");

        Resource commitResource = model.createResource(commitURI)
            .addProperty(RDF.type, model. createResource(GIST_URI + "Event"))
            // .addProperty(DCTerms.identifier, commit.getString("sha"))
            .addProperty(DC.date, commitDate)
            .addProperty(DC.description, commitMessage)
            .addProperty(DCTerms.isVersionOf, markdownFile);
        
        // commit author Resource
        Resource authorResource = createAuthorResource(model, authorURI, authorName, authorEmail, commitResource);
        commitResource.addProperty(model.createProperty(GIST_URI, "hasParticipant"), authorResource);

        return commitResource;
    }

                // ------------------ Markdown Resource ---------------------
    private static Resource createMarkdownResource(Model model, String UrlString, Map<String, String> frontMatter) {
        Resource markdownResource = model.createResource(UrlString) // We create the resource for the markdown file, described with vocabularies in ()
            .addProperty(RDF.type, model.createResource(FOAF_URI + "Document"))  // a (foaf) Document
            .addProperty(DC.title, frontMatter.getOrDefault("title", "")) // a (DC) title
            .addProperty(DCTerms.type, frontMatter.getOrDefault("type", "")) // a (DCTerms) type
            .addProperty(DCTerms.identifier, frontMatter.getOrDefault("eip", "")) // a a (DCTerms) identifier // todo integrate with custom domain vocab for eips? 
            .addProperty(DC.date, frontMatter.getOrDefault("created", "")); // a (DC) date
            // .addProperty(DCTerms.description, markdownBody);  // a (DCTerms) description containing the content // todo not very useful at the moment as it simply stringifys markdown
            
            // Process authors seperately due to edge cases (et al)
            if (frontMatter.containsKey("author")) {
                boolean etAlPresent = false;
                for (String author : frontMatter.get("author").split(",")) {
                    author = author.trim();
                    
                    if (author.equalsIgnoreCase("et al.")) {
                        etAlPresent = true;
                        continue;
                    }

                    // Process individual authors (without GitHub URI)
                    Matcher m = Pattern.compile("(.+?)\\s*<(.+?)>").matcher(author);
                    if (m.find()) {
                        String authorName = m.group(1).trim();
                        String authorEmail = m.group(2).trim();
                        Resource authorResource = createAuthorResource(model, "https://github.com/" + "eipEditor/" + authorName , authorName, authorEmail, markdownResource);
                        markdownResource.addProperty(model.createProperty(GIST_URI, "hasParticipant"), authorResource);
                    } else {

                   /*  blank node implementation for authors - commented out
                   Resource authorResource = model.createResource()
                        .addProperty(RDF.type, model.createResource(GIST_URI + "Agent"))
                        .addProperty(model.createProperty(FOAF_URI, "name"), author);
                        markdownResource.addProperty(model.createProperty(GIST_URI, "hasParticipant"), authorResource);
                    */  

                    } 
                }

                // If "et al." was present, add placeholder for additional authors
                if (etAlPresent) {
                    Resource additionalAuthorResource = createAuthorResource(model, "https://github.com/FurtherUnspecifiedEipAuthors", "Unspecified", "Unspecified", markdownResource);
                    markdownResource.addProperty(model.createProperty(GIST_URI, "hasParticipant"), additionalAuthorResource);
                }
            }

            // Adding editors from frontmatter
           /* weg
            for (String editorURI : eipEditors.keySet()) {
                markdownResource.addProperty(model.createProperty(GIST_URI, "hasEditor"), eipEditors.get(editorURI));
            } */

            return markdownResource;
        }  

                    // ---------------- HELPER METHODS ---------------------
    private static Resource processMarkdownFile(String fileName, Model model, String apiUrl, String fileUrl) throws IOException, InterruptedException {
        JSONObject fileData = fetchFileFromGitHub(apiUrl); // see helper method for JSON Object below
       
        // Fetch and decode Base64 content
        String fileContent = fetchFileContent(fileData); // see helper method for below
       
        Map<String, String> frontMatter = extractFrontMatter(fileContent); // see helper method below
        
        // If status is moved , change fileName to erc- and re-fetch from ERC_REPO + fileName
        String status = frontMatter.get("status");
        System.out.println(status);

        // If status is moved , change fileName to erc- and re-fetch from ERC_REPO + fileName
        if (status.equalsIgnoreCase("moved") ) {
            System.out.println("status moved detected");
            fileName = fileName.replace("eip", "erc");
            System.out.println(fileName);
            apiUrl = ERC_API_URL + fileName;
            fileUrl = ERC_REPO + fileName;
            fileData = fetchFileFromGitHub(apiUrl); // see helper method for JSON Object below
            fileContent = fetchFileContent(fileData);
            frontMatter = extractFrontMatter(fileContent);
        } 
        
        // Create the markdown resource for the file (EIP or ERC)
        Resource markdownResource = createMarkdownResource(model, fileUrl, frontMatter);

        // Now fetch the commit history for the file (both EIP and ERC, if applicable)
        processCommitsForFile(fileName, markdownResource, model);

        return markdownResource;

        // Extract the markdown body (As comment because not very useful at the moment)
        // String markdownBody = extractMarkdownBody(fileContent);  // see helper method below
        
    }

    private static void processCommitsForFile(String fileName, Resource markdownFile, Model model) throws IOException, InterruptedException {
        System.out.println("Fetching commit history for file: " + fileName);
        JSONArray commits = fetchCommitsForFile(fileName);

            for (int i = 0; i < commits.length(); i++) {
                JSONObject commit = commits.getJSONObject(i);
                createCommitResource(model, commit, markdownFile);
                // Diffs commented out - not useful still. TODO 
                // processCommitDiff(commit.getString("sha"), fileName, commitResource, model);
            }
             // If the file was moved, also fetch the commits for the ERC version
            if (fileName.contains("erc")) {
                String originalFileName = fileName.replace("erc", "eip");
                System.out.println("Fetching commit history for the original EIP file: " + originalFileName);
                JSONArray eipCommits = fetchCommitsForFile(originalFileName);

                for (int i = 0; i < eipCommits.length(); i++) {
                    JSONObject eipCommit = eipCommits.getJSONObject(i);
                    createCommitResource(model, eipCommit, markdownFile);
                }
            }
    }

    // Helper method to fetch the commits related to a specific file
    private static JSONArray fetchCommitsForFile(String fileName) throws IOException, InterruptedException {
        String repoPath;

        // Choose the correct repository based on the file type (EIP or ERC)
        if (fileName.contains("erc")) {
            repoPath = "https://api.github.com/repos/ethereum/ERCs/commits?path=ERCS/";
        } else {
            repoPath = "https://api.github.com/repos/ethereum/EIPs/commits?path=EIPS/";
        }
    
        String apiUrl = repoPath + fileName;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
            .header("User-Agent", "Mozilla/5.0")
            .header("Accept", "application/json")
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            System.out.println("Commits fetched successfully for file: " + fileName);
            System.out.println(response.body());
            return new JSONArray(response.body());
        } else {
            System.out.println("Error fetching commits for file: " + fileName + " - Status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
            return null;
        }
    }

    // Helper method that fetches the file content from the GitHub API (Base64 decoded or raw content)
    private static String fetchFileContent(JSONObject fileData) throws IOException, InterruptedException {
        if (fileData.has("download_url")) {
            String downloadUrl = fileData.getString("download_url");
            System.out.println("file data has download_url");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(downloadUrl))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("file data had been found in download_url");
            return response.body();  // Direct file content from the download URL
         } 
        else {
            throw new IllegalArgumentException("No download_url found in the file data.");
        }
    }

    // Helper method to fetch a file's metadata and content from GitHub API
    private static JSONObject fetchFileFromGitHub(String apiUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
            .header("User-Agent", "Mozilla/5.0")
            .header("Accept", "application/json")
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 ? new JSONObject(response.body()) : new JSONObject();
    }

    // Helper method to extract the frontmatter variables from Markdown top
    private static Map<String, String> extractFrontMatter(String content) {
        Map<String, String> frontMatter = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String line;
            boolean inFrontMatter = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("---")) {
                    if (inFrontMatter) break; // End of front matter
                    else inFrontMatter = true; // Start of front matter
                } else if (inFrontMatter && line.contains(":")) {
                    String[] keyValue = line.split(":", 2);
                    frontMatter.put(keyValue[0].trim().toLowerCase(), keyValue[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return frontMatter;
    }

    /*    // Helper methods to extract author details from front matter
    private static String extractAuthorName(String author) {
        // Regex or simple split logic to extract name
        String[] parts = author.split("<");
        return parts[0].trim();
    }

    private static String extractAuthorEmail(String author) {
        // Regex to extract email if it exists
        if (author.contains("<") && author.contains(">")) {
            return author.substring(author.indexOf('<') + 1, author.indexOf('>'));
        }
        return "";
    } */
     
    // Diff files are commented out - not useful yet. TODO: fix timeout error for Github API
    /*  private static void processCommitDiff(String sha, String fileName, Resource commitResource, Model model) throws IOException, InterruptedException {
        String diffContent = fetchCommitDiff(fileName, sha);
        Resource diffResource = model.createResource(commitResource.getURI() + "#diff")
            .addProperty(RDF.type, model.createResource(GIST_URI + "Delta"))
            .addProperty(DC.description, diffContent);

        commitResource.addProperty(model.createProperty(GIST_URI, "hasPart"), diffResource);
    } */

    // Helper method to extract markdown body content after front matter
    // As comment because not very useful at the moment
       /*   private static String extractMarkdownBody(String fileContent) {
        int bodyStart = fileContent.indexOf("---", fileContent.indexOf("---") + 3);
        return bodyStart != -1 ? fileContent.substring(bodyStart + 3).trim() : fileContent;
    } */

        // Diffs commented out - not useful at the moment
    // Helper method fetches the commit diff for a specific file
  /*   private static String fetchCommitDiff(String fileName, String sha) throws IOException, InterruptedException {
        String apiUrl = "https://api.github.com/repos/ethereum/EIPs/commits/" + sha;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
            .header("User-Agent", "Mozilla/5.0")
            .header("Accept", "application/json")
            .build();
        
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject commitData = new JSONObject(response.body());
                JSONArray commitFiles = commitData.getJSONArray("files");
                // Debugging: Print the number of files in the commit
                System.out.println("Files in the commit: " + commitFiles.length());

                for (int i = 0; i < commitFiles.length(); i++) {
                    JSONObject fileData = commitFiles.getJSONObject(i);
                    String filename = fileData.getString("filename");
    
                    // Debugging: Print the filename to check which files are being processed
                     System.out.println("Processing file: " + filename);

                    // if (filename.equals(fileName) || filename.equals("EIPS/" + fileName)) { // todo: only look at diffs related to the file, not all diffs of all related files
                        if (!fileData.isEmpty()) {
                    // Debugging: File match found
                        System.out.println("File match found: " + filename);

                        // Return the diff/patch if it exists
                        if (fileData.has("patch")) {
                            // Debugging: Patch found for the file
                            System.out.println("Patch found for file: " + filename);
                            return fileData.getString("patch");
                        } else {
                            // Debugging: No diff available for this file
                            System.out.println("No diff available for file: " + filename);
                            return "No diff available for this file.";
                        }
                    }
    
                }
                // If the file is not found in the commit
                System.out.println("File not found in the commit: " + fileName);
                return "File not found in the commit.";
        } else {
            System.out.println("Failed to fetch commit diff: " + response.statusCode());
            System.out.println("Response: " + response.body());
            throw new IOException("Failed to fetch commit diff: " + response.statusCode());
        }
    
    } */
}
