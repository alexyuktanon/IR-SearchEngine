import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;


public class Main2 {

	public static void main(String args[]) throws Exception {
		Map<String, String> docIdMap = Utils.getFileMapping(Config.MAP_PATH);
		Index index = readIndex(Config.INDEX_PATH);
		String[] queries = {"machine learning", "information retrieval"};
		Snippet s = new Snippet();
		
		for(String q : queries) {
			System.out.println("Query: "+q+"\n");
			List<Entry<String, Double>> docOut = search(q);
			for(Entry<String, Double> entry : docOut) {
				String docId = entry.getKey();
				String doc = new String(Files.readAllBytes(Paths.get(Config.ROOT_FOLDER+docId)), StandardCharsets.UTF_8);

				String url = docIdMap.get(docId);
				String snippet = s.getSnippet(doc, docId, q, index);
				System.out.println(url);
				System.out.println(snippet+"\n\n");
			}
		}
		
	}
	
	public static Index readIndex(String indexPath) throws IOException {
		String indexJson = new String(Files.readAllBytes(Paths.get(indexPath)), StandardCharsets.UTF_8);
		return Index.fromJson(indexJson);
	}
	/**
	 * output a list of docId & search score pair in DESC order
	 * @param searchQuery
	 * @return
	 */
	public static List<Entry<String, Double>> search(String searchQuery) {
		List<String> searchTokens = Token.tokenizeText(searchQuery);
		// ------- end ---------
		
		// ----- Get relevant documents for search tokens -----
		JsonNode rootIndexNode = null;
		try {
			rootIndexNode = Search.getIndexes();
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
		Map<String, Double> scoreQueries = Search.computeQueryScore(searchTokens, rootIndexNode);
	    // ------- end ---------
		
		// ----- Compute Cosine Similarity -----
		Map<String, Double> AllCosineScores = Search.computeCosineScores(relevantDocs, searchTokens, rootIndexNode, scoreQueries);
		

		List<Map.Entry<String, Double>> rankedScores = Search.rankScore(AllCosineScores);
		return rankedScores;
	}
}
