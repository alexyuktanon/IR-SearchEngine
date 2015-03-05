import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main2 {
	
	static final int MAX_DISPLAY = 20;
	
	public static int getTitlePageScore(String titleString, List<String> query) throws IOException {
		int num = 0;
		for(String q : query) {
			if(titleString.toLowerCase().contains(q.toLowerCase())) num++;
		}
		return num;
	}
	
	public static List<Entry<String, Double>> updateScore(List<Entry<String, Double>> input, Map<String, String> docIdMap, Map<String, String> titleMap, List<String> query) throws Exception {
		Map<String, Double> scoreMap = new HashMap<String, Double>();
		for(int i=0; i<input.size(); i++) {
			Entry<String, Double> entry = input.get(i);
			String docId = entry.getKey();
			int score = getTitlePageScore(titleMap.get(docId), query);
			scoreMap.put(docId, entry.getValue()+score);
		}
		return Search.rankScore(scoreMap);
	}

	public static void main(String args[]) throws Exception {
		Map<String, String> docIdMap = Utils.getFileMapping(Config.MAP_PATH);
		Map<String, String> titleMap = Utils.getFileMapping(Config.TITLE_PATH);
		Index index = readIndex(Config.INDEX_PATH);
		Snippet s = new Snippet();
		
		while(true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter query: ");
		    String q = br.readLine();
		    
		    long startSearcTimeTime = System.nanoTime();
			List<Entry<String, Double>> docOut = Search.search(q, index);
			docOut = updateScore(docOut, docIdMap, titleMap, Token.tokenizeText(q));
		    long endSearchTime = System.nanoTime();
		    long durationInSecond = (endSearchTime - startSearcTimeTime)/100000;
		    
		    System.out.println("Found "+docOut.size()+" results in "+durationInSecond+" milliseconds.");
			for(int i=0; i<Math.min(docOut.size(), MAX_DISPLAY); i++) {
					Entry<String, Double> entry = docOut.get(i);
				String docId = entry.getKey();
				String doc = new String(Files.readAllBytes(Paths.get(Config.ROOT_FOLDER+docId)), StandardCharsets.UTF_8);

				String url = docIdMap.get(docId);
				String snippet = s.getSnippet(doc, docId, q, index);
				System.out.println("Rank "+i+" : doc id : "+docId+" score : "+entry.getValue());
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
