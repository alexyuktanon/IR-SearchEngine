import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;

public class Search {

	public static void main (String [] args){
		// ----- Set up query for search -----
		String searchQuery = "ant fish";
		List<String> searchTokens = Token.tokenizeText(searchQuery);
		// ------- end ---------
		
		// ----- Get relevant documents for search tokens -----
		JsonNode rootIndexNode = null;
		try {
			rootIndexNode = getIndexes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Set<String> relevantDocs = new HashSet<String>();
		for(String token : searchTokens){
			JsonNode wordNode = rootIndexNode.path(token);
			Iterator<Map.Entry<String,JsonNode>> ite = wordNode.path("tfidfTuples").getFields();
			while (ite.hasNext()) {
				Entry<String,JsonNode> temp = ite.next();
				relevantDocs.add(temp.getKey());
			}
		}
		// ------- end ---------
		
		// ----- Compute TF-IDF score for query -----
		Double scoreQuery = computeQueryScore(searchTokens, rootIndexNode);
	    // ------- end ---------
		
		// ----- Compute TF-IDF score for document given a query -----
		Map<String, Double> scoreTermDocument = computeTermDocumentScore(relevantDocs, searchTokens, rootIndexNode);
	    // ------- end ---------
	    
		// ----- Compute Cosine Similarity -----
	    // ------- end ---------
	    
		// ----- Compare and Process Result -----
	    // ------- end ---------
		
	    // ----- Testing Part -----
		System.out.println("Relevant Docs: " + relevantDocs);
		System.out.println("Score of Query: " + scoreQuery);
		System.out.println("Score of Term-Document: " + scoreTermDocument);
		// ------- end ---------
	}
	
	public static JsonNode getIndexes() throws IOException{	
		//Get indexes from JSON file
		ObjectMapper mapper = new ObjectMapper();
		File jsonFile = new File("./indexes.json.txt"); 
		JsonNode rootNode = mapper.readTree(jsonFile);
		
		return rootNode;
	}
	
	public static Double computeQueryScore(List<String> tokens, JsonNode rootNode){
		Double scoreQuery = 0.0;
	    int numTokens = tokens.size();
	    Map<String, Integer> tokensFrequencies = new HashMap<String, Integer>();
	    for(int i = 0; i < numTokens; i++) {
	    	// Compute TF
			if(tokensFrequencies.containsKey(tokens.get(i))){
				int currentFrequency = (int) tokensFrequencies.get(tokens.get(i));
				currentFrequency++;
				tokensFrequencies.put(tokens.get(i).toString(), currentFrequency);
			}else{
				//If there is no token in the hashmap, add new
				tokensFrequencies.put(tokens.get(i).toString(), 1);
			}
	    }
	    
	    // Compute TF-IDF
	    for(Map.Entry<String, Integer> entry : tokensFrequencies.entrySet()){
	        double tfValue = entry.getValue();
	        double idfValue = rootNode.path(entry.getKey()).path("idf").getDoubleValue();
	        double tfidfValue = tfValue * idfValue;
	        tfidfValue = Math.round( tfidfValue * 10000.0 ) / 10000.0; //Round to 4 decimal

	        scoreQuery = scoreQuery + tfidfValue;
	    }
		return scoreQuery;
	}
	
	public static Map<String, Double> computeTermDocumentScore(Set<String> documents, List<String> tokens, JsonNode rootNode){
		Map<String, Double> scoreTermDocument = new HashMap<String, Double>();
		for(String doc : documents){
			for(String token : tokens){
				JsonNode wordNode = rootNode.path(token);
				Iterator<Map.Entry<String,JsonNode>> ite = wordNode.path("tfidfTuples").getFields();
				while (ite.hasNext()) {
					Entry<String,JsonNode> temp = ite.next();
					// Check for relevant documents only
					if(doc == temp.getKey().toString()){
						if(scoreTermDocument.containsKey(doc)){
							Double currentScore = (Double) scoreTermDocument.get(doc);
							currentScore = currentScore + temp.getValue().asDouble();
							scoreTermDocument.put(doc, currentScore);
						}else{
							//If there is no doc in the hashmap, add new
							scoreTermDocument.put(doc, temp.getValue().asDouble());
						}
					}
				}
			}
		}
		return scoreTermDocument;
	}
}
