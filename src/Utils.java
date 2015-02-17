

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/**
 * utitlity class
 * @author Oak
 *
 */
public class Utils {
	
	private static final String STOPWORDS_FILE = "./stopwords.txt";

	/**
	 * http://stackoverflow.com/questions/8429516/stop-words-removal-in-java
	 * @return a set of stopWords -> to find if a string is in the list, use stopWords.contain(word)
	 * @throws IOException 
	 */
	public static Set<String> getStopWords() throws IOException {
		Set<String> stopWords = new LinkedHashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(STOPWORDS_FILE));
		for(String line; (line = br.readLine()) != null; )
		   stopWords.add(line.trim());
		br.close();
		return stopWords;
	}
	
	public static Map<String, String> getFileMapping(String mapPath) throws Exception {
		File mapFile = new File(mapPath);
		List<String> lines = FileUtils.readLines(mapFile);
		Map<String, String> output = new HashMap<String, String>();
		for(int i=0; i<lines.size(); i+=2) {
			// file ID
			String key = lines.get(i);
			String val = lines.get(i+1);
			if(output.containsKey(key)) {
				System.out.println(key);
				throw new Exception("Duplicate file ID");
			}
			
			output.put(key, val);
		}
		return output;
	}
}
