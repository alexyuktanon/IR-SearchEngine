import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;

public class Search {

	public static void main (String [] args){
		// ----- Set up query for search -----
		String searchQuery = "";
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
		Map<String, Double> scoreQueries = computeQueryScore(searchTokens, rootIndexNode);
	    // ------- end ---------
		
		// ----- Compute Cosine Similarity -----
		Map<String, Double> AllCosineScores = computeCosineScores(relevantDocs, searchTokens, rootIndexNode, scoreQueries);
	    // ------- end ---------
	    
		// ----- Compare and Process Result -----
		List<Map.Entry<String, Double>> rankedScores = rankScore(AllCosineScores);
	    // ------- end ---------
		
	    // ----- Testing Part -----
		System.out.println("Relevant Docs: " + relevantDocs);
		System.out.println("TF-IDF Score of each query: " + scoreQueries);
		System.out.println("Total TF-IDF Score of query: " + computeTotalQueryScore(scoreQueries));
		System.out.println("Cosine score for each document from every query: " + AllCosineScores);
		System.out.println("Ranked score: " + rankedScores);
		// ------- end ---------
	}
	
	public static JsonNode getIndexes() throws IOException{	
		//Get indexes from JSON file
		ObjectMapper mapper = new ObjectMapper();
		File jsonFile = new File("./indexes.json.txt"); 
		JsonNode rootNode = mapper.readTree(jsonFile);
		
		return rootNode;
	}
	
	public static Map<String, Double> computeQueryScore(List<String> tokens, JsonNode rootNode){
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
	    
	    // Compute TF-IDF for each query
	    Map<String, Double> scoreQueries = new HashMap<String, Double>();
	    for(Map.Entry<String, Integer> entry : tokensFrequencies.entrySet()){
	        double tfValue = entry.getValue();
	        double idfValue = rootNode.path(entry.getKey()).path("idf").getDoubleValue();
	        double tfidfValue = tfValue * idfValue;
	        tfidfValue = Math.round( tfidfValue * 10000.0 ) / 10000.0; //Round to 4 decimal

			if(scoreQueries.containsKey(entry.getKey())){
				Double currentTfidfScore = (Double) scoreQueries.get(entry.getValue());
				currentTfidfScore = currentTfidfScore + tfidfValue;
				scoreQueries.put(entry.getKey().toString(), currentTfidfScore);
			}else{
				//If there is no token in the hashmap, add new
				scoreQueries.put(entry.getKey().toString(), tfidfValue);
			}
	    }
	    
		return scoreQueries;
	}
	
	public static Double computeTotalQueryScore(Map<String, Double> scoreQueries){
		Double totalScoreQuery = 0.0;
		for(Map.Entry<String, Double> entry : scoreQueries.entrySet()){
			totalScoreQuery = totalScoreQuery + entry.getValue();
		}
		
		return totalScoreQuery;
	}
	
	public static Map<String, Double> computeCosineScores(Set<String> documents, List<String> tokens,
														  JsonNode rootNode, Map<String, Double> scoreQueries){
		Map<String, Double> cosineScores = new HashMap<String, Double>();
		for(String doc : documents){
			Double sumQD = 0.0;
			Double sumQ2 = 0.0;
			Double sumD2 = 0.0;
			
			for(String token : tokens){
				JsonNode wordNode = rootNode.path(token);
				// Compute cosine(query,doucment) score
				Double q = scoreQueries.get(token);
				Double d = wordNode.path("tfidfTuples").path(doc).getDoubleValue();
				Double qd = q * d;
				Double q2 = Math.pow(q,2);
				Double d2 = Math.pow(d,2);
				
				sumQD = sumQD + qd;
				sumQ2 = sumQ2 + q2;
				sumD2 = sumD2 + d2;
			}
			
			Double queriesDocumentCosineScore = sumQD / ( Math.sqrt(sumQ2) * Math.sqrt(sumD2) );
			cosineScores.put(doc, queriesDocumentCosineScore);
		}
		
		return cosineScores;
	}

	private static List<Map.Entry<String, Double>> rankScore(Map<String, Double> scoreMap) { 
	    // Sort map in increasing order
	    List<Map.Entry<String, Double>> scoreList = new LinkedList<Map.Entry<String, Double> >(scoreMap.entrySet());
		Collections.sort(scoreList, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue()); // -> descending order
			}
		});
		
	    return scoreList;
	}
	
}
