
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;

public class Search {
	
	/**
   * output a list of docId & search score pair in DESC order
   * @param searchQuery
   * @return
   */
	public static List<Entry<String, Double>> search(String searchQuery, Index index) {
		// ----- Set up token from query -----
		List<String> searchTokens = Token.tokenizeText(searchQuery);
	    
	    // ----- Get relevant documents for search tokens -----
	    Set<String> relevantDocs = Search.getRelevantDocuments(searchTokens, index);
	    
	    // ----- Compute TF-IDF score for query -----
	    Map<String, Double> scoreQueries = Search.computeQueryScore(searchTokens, index);
	    
	    // ----- Compute Cosine Similarity -----
	    Map<String, Double> AllCosineScores = Search.computeCosineScores(relevantDocs, searchTokens, index, scoreQueries);
	
		// ----- Compare and Process Result -----
	    List<Map.Entry<String, Double>> rankedScores = Search.rankScore(AllCosineScores);
	    return rankedScores;
	}
	
  	private static JsonNode getIndexes() throws IOException{	
		//Get indexes from JSON file
		ObjectMapper mapper = new ObjectMapper();
		File jsonFile = new File(Config.INDEX_PATH); 
		JsonNode rootNode = mapper.readTree(jsonFile);
		
		return rootNode;
	}
	
	private static Set<String> getRelevantDocuments(List<String> searchTokens, Index index){
		Set<String> relevantDocs = new HashSet<String>();
		for(String token : searchTokens){
			Iterator<Map.Entry<String,Double>> ite = index.getTfidfMap(token).entrySet().iterator();
			while (ite.hasNext()) {
				Entry<String,Double> temp = ite.next();
				relevantDocs.add(temp.getKey());
			}
		}
		
		return relevantDocs;
	}
	
	private static Map<String, Double> computeQueryScore(List<String> tokens, Index index){
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
	        double idfValue = index.getIdf(entry.getKey());
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
	
	private static Double computeTotalQueryScore(Map<String, Double> scoreQueries){
		Double totalScoreQuery = 0.0;
		for(Map.Entry<String, Double> entry : scoreQueries.entrySet()){
			totalScoreQuery = totalScoreQuery + entry.getValue();
		}
		
		return totalScoreQuery;
	}
	
	private static Map<String, Double> computeCosineScores(Set<String> documents, List<String> tokens,
														  Index index, Map<String, Double> scoreQueries){
		Map<String, Double> cosineScores = new HashMap<String, Double>();
		for(String doc : documents){
			Double sumQD = 0.0;
			Double sumQ2 = 0.0;
			Double sumD2 = 0.0;
			
			for(String token : tokens){
				// Compute cosine(query,doucment) score
				Double q = scoreQueries.get(token);
				Double d = index.getTfidfMap(token).get(doc);
				d = (d==null) ? 0 : d;
				Double qd = q * d;
				Double q2 = Math.pow(q,2);
				Double d2 = Math.pow(d,2);
				
				sumQD = sumQD + qd;
				sumQ2 = sumQ2 + q2;
				sumD2 = sumD2 + d2;
			}
			
			Double queriesDocumentCosineScore = sumQD / ( Math.sqrt(sumQ2) * Math.sqrt(sumD2) );
			
			//if NaN, change score to zero
			if(queriesDocumentCosineScore.isNaN()){
				queriesDocumentCosineScore = 0.00;
			}
			
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
				return (o2.getValue()).compareTo(o1.getValue()); // -> descending order
			}
		});
		
	    return scoreList;
	}
	
}
