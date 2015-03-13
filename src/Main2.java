import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sun.xml.internal.ws.util.StringUtils;


public class Main2 {
	
	static final int MAX_DISPLAY = 5;
	
	// using jaccard scoring - perhaps should use tfidf
	public static double getScore(String string, List<String> query) throws IOException {
		if(string == null) return 0;	
		Set<String> tokenString = new HashSet<String>(Token.tokenizeText(string));
		Set<String> queryToken = new HashSet<String>(query);
		
		Set<String> union = new HashSet<String>(tokenString);
		union.addAll(queryToken);
		
		Set<String> intersection = new HashSet<String>(tokenString);
		intersection.retainAll(queryToken);
		
		return ((double) intersection.size())/((double) queryToken.size());
	}
	
	public static double computeScoreFromUrl(String url, List<String> query) {
    double penalizeScore = url.indexOf("?"); // penalize query url
    int numSlashes = url.length() - url.replace("/", "").length();
    double score = (penalizeScore != -1) ? -0.5 : 0;
    
    if(numSlashes<=3) score += 1;
    else if(numSlashes==4) score += 0.5;
    else if(numSlashes==5) score += 0.4;
    else if(numSlashes==6) score += 0.35;
    
    return score;
	}
	
	/**
	 * update score using heuristics
	 * for now, extract score from title and url
	 * @param input
	 * @param urlMap
	 * @param titleMap
	 * @param query
	 * @return
	 * @throws Exception
	 */
	double COSIM_THRESHOLD = 0.2;
	public static List<Entry<String, Double>> updateScore(List<Entry<String, Double>> input, Map<String, String> urlMap, Map<String, String> titleMap, List<String> query, Index index) throws Exception {
		Map<String, Double> scoreMap = new HashMap<String, Double>();
		for(int i=0; i<input.size(); i++) {
			Entry<String, Double> entry = input.get(i);
			String docId = entry.getKey();
			
			// TODO: apply linear combination or other heuristics

      double cosimScore = entry.getValue();
      if(cosimScore < 0.2) continue; // reduce workload
			double titleScore = getScore(titleMap.get(docId), query)+0.1*getScore(titleMap.get(docId), new ArrayList<String>(){{add("home");}});
			double urlQueryScore = getScore(urlMap.get(docId), query);
			double urlStructureScore = computeScoreFromUrl(urlMap.get(docId), query);
			
			// pure heuristics here...
			// 1) title is the most important one
			// 2) url structure (num of slashes and query request) and cosim are equally important i think
			// 3) url-query is prolly not much - for tie breaking
			// the way to design linear combination weight (since every score has a max value of 1):
			// 1) > 2) >>> 3)
			scoreMap.put(docId, titleScore*.4 + cosimScore*.2 + urlStructureScore*.3 + urlQueryScore*0.001);
		}
		return Search.rankScore(scoreMap);
	}

	public static void main(String args[]) throws Exception {
		Map<String, String> docIdMap = Utils.getFileMapping(Config.MAP_PATH);
		Map<String, String> titleMap = Utils.getFileMapping(Config.TITLE_PATH);
		Index index = readIndex(Config.INDEX_PATH);
		Snippet s = new Snippet();
		
		//Web UI
		while(true) {		  
			String status = new String(Files.readAllBytes(Paths.get("./web/share/status-q.txt")));
			if(status.matches("1")){
				//Change query status
				PrintWriter out = new PrintWriter("./web/share/status-q.txt");
				out.print("0");
				out.close();
				
				//Read query
			    String q = new String(Files.readAllBytes(Paths.get("./web/share/query.txt")));
			    System.out.println("Processing " + q);
			    
			    //Process query
				List<Entry<String, Double>> docOut = Search.search(q, index);
				System.out.println("Done getting cosim score. Start updating score");
				docOut = updateScore(docOut, docIdMap, titleMap, Token.tokenizeText(q), index);
        System.out.println("Done updating score");
				String resultData = "<h1>Search for " + q + "</h1>";
				for(int i=0; i<Math.min(docOut.size(), MAX_DISPLAY); i++) {
					Entry<String, Double> entry = docOut.get(i);
					String docId = entry.getKey();
					String doc = new String(Files.readAllBytes(Paths.get(Config.ROOT_FOLDER+docId)), StandardCharsets.UTF_8);
		
					String title = titleMap.get(docId);
					String url = docIdMap.get(docId);
					String snippet = s.getSnippet(doc, docId, q, index);
					resultData += "<div>";
					resultData += "<p>" + "Rank " + i + " | Doc ID: " + docId + " | Score: " + String.format("%.2f", entry.getValue()) + "</p>";
					resultData += "<p><a href=\"" + url + "\" target=\"_blank\">" + url + "</a></p>";
					resultData += "<p>" + snippet + "</p>";
					resultData += "</div>";
					resultData += "<br/>";
				}
				
				if(docOut.size() == 0){
					resultData += "<div>";
					resultData += "There are no results for your search!";
					resultData += "</div>";
				}
	
				//Print search result
				out = new PrintWriter("./web/share/result.txt");
				out.print(resultData);
				out.close();
			    
				//Change result status
				out = new PrintWriter("./web/share/status-r.txt");
				out.print("1");
				out.close();
			}else{
				System.out.println("Idle");
				Thread.sleep(2000); //Delay retrieve query if status code = 0
			}
		}

		//Console UI
		/*
		while(true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter query: ");
		    String q = br.readLine();
		    
		    long startSearcTimeTime = System.nanoTime();
			List<Entry<String, Double>> docOut = Search.search(q, index);
			docOut = updateScore(docOut, docIdMap, titleMap, Token.tokenizeText(q), index);
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
		*/
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
