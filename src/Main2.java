import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class Main2 {
	
	static final int MAX_DISPLAY = 5;

	public static void main(String args[]) throws Exception {
		Map<String, String> docIdMap = Utils.getFileMapping(Config.MAP_PATH);
		Index index = readIndex(Config.INDEX_PATH);
		Snippet s = new Snippet();
		
		while(true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter query: ");
		    String q = br.readLine();
		    
		    long startSearcTimeTime = System.nanoTime();
			List<Entry<String, Double>> docOut = Search.search(q, index);
		    long endSearchTime = System.nanoTime();
		    long durationInSecond = (endSearchTime - startSearcTimeTime)/100000;
		    
		    System.out.println("Found "+docOut.size()+" results in "+durationInSecond+" milliseconds.");
			for(int i=0; i<Math.min(docOut.size(), MAX_DISPLAY); i++) {
					Entry<String, Double> entry = docOut.get(i);
				String docId = entry.getKey();
				String doc = new String(Files.readAllBytes(Paths.get(Config.ROOT_FOLDER+docId)), StandardCharsets.UTF_8);

				String url = docIdMap.get(docId);
				String snippet = s.getSnippet(doc, docId, q, index);
				System.out.println("Rank " + i + " | Doc ID: " + docId + " | Cosine Score: "+entry.getValue());
				System.out.println(url);
				System.out.println(snippet+"\n");
			}
			System.out.println();
		}
		
	}
	
	public static Index readIndex(String indexPath, Set<String> q) throws IOException {
		String indexJson = new String(Files.readAllBytes(Paths.get(indexPath)), StandardCharsets.UTF_8);
		return Index.fromJson(indexJson, q);
	}
	
	public static Index readIndex(String indexPath) throws IOException {
		String indexJson = new String(Files.readAllBytes(Paths.get(indexPath)), StandardCharsets.UTF_8);
		return Index.fromJson(indexJson);
	}
}
