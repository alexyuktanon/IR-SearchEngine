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


public class Main2 {
	
	static final int MAX_DISPLAY = 5;

	public static void main(String args[]) throws Exception {
		Map<String, String> docIdMap = Utils.getFileMapping(Config.MAP_PATH);
		String[] queries = {"machine learning", "machine"};
		Snippet s = new Snippet();
		
		for(String q : queries) {
			Index index = readIndex(Config.INDEX_PATH, new HashSet<String>(Token.tokenizeText(q)));
			System.out.println("=================================");
			System.out.println("Query: "+q+"\n");
			List<Entry<String, Double>> docOut = search(q, index);
			for(int i=0; i<MAX_DISPLAY; i++) {
				Entry<String, Double> entry = docOut.get(i);
				String docId = entry.getKey();
				String doc = new String(Files.readAllBytes(Paths.get(Config.ROOT_FOLDER+docId)), StandardCharsets.UTF_8);

				String url = docIdMap.get(docId);
				String snippet = s.getSnippet(doc, docId, q, index);
				System.out.println("Rank "+i+" : doc id : "+docId+" score : "+entry.getValue());
				System.out.println(url);
				System.out.println(snippet+"\n");
			}
			System.out.println("=================================");
		}
		
	}

	public static Index readIndex(String indexPath, Set<String> queryTokens) throws IOException {
		String indexJson = new String(Files.readAllBytes(Paths.get(indexPath)), StandardCharsets.UTF_8);
		return Index.fromJson(indexJson, queryTokens);
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
	public static List<Entry<String, Double>> search(String searchQuery, Index index) {
		List<String> searchTokens = Token.tokenizeText(searchQuery);
		// ------- end ---------
		
		// ----- Get relevant documents for search tokens -----
//		JsonNode rootIndexNode = null;
//		try {
//			rootIndexNode = Search.getIndexes();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		Set<String> relevantDocs = new HashSet<String>();
		for(String token : searchTokens){
			Iterator<Map.Entry<String,Double>> ite = index.getTfidfMap(token).entrySet().iterator();
			while (ite.hasNext()) {
				Entry<String,Double> temp = ite.next();
				relevantDocs.add(temp.getKey());
			}
		}
		
		// ------- end ---------
		
		// ----- Compute TF-IDF score for query -----
		Map<String, Double> scoreQueries = Search.computeQueryScore(searchTokens, index);
	    // ------- end ---------
		
		// ----- Compute Cosine Similarity -----
		Map<String, Double> AllCosineScores = Search.computeCosineScores(relevantDocs, searchTokens, index, scoreQueries);
		

		List<Map.Entry<String, Double>> rankedScores = Search.rankScore(AllCosineScores);
		return rankedScores;
	}
}
