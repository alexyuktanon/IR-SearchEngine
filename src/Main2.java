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
			Index index = Index.fromJsonFile(Config.INDEX_PATH, new HashSet<String>(Token.tokenizeText(q)));
			System.out.println("=================================");
			System.out.println("Query: "+q+"\n");
			List<Entry<String, Double>> docOut = Search.search(q, index);
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
	
	public static Index readIndex(String indexPath) throws IOException {
		String indexJson = new String(Files.readAllBytes(Paths.get(indexPath)), StandardCharsets.UTF_8);
		return Index.fromJson(indexJson);
	}
}
