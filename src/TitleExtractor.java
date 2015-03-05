import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class TitleExtractor {
	public static void main(String args[]) throws Exception  {
		PrintWriter out = new PrintWriter(Config.TITLE_PATH);
		Map<String, String> docIdMap = Utils.getFileMapping(Config.MAP_PATH);
		int idx=0;
		int last = docIdMap.size();
		
		for(Entry<String, String> entry : docIdMap.entrySet()) {
			if(idx%100 == 0) {
				System.out.println(idx+"/"+last);
			}
			String docId = entry.getKey();
			String url = entry.getValue();
			try {
				Document doc = Jsoup.connect(url).get();
				
				out.println(docId);
				out.println(doc.title());
			} catch (IOException e) {
				out.println(docId);
				out.println("");
			}
			idx++;
		}
		out.close();
	}
}
