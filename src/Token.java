
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;


public class Token {
	
	public static List<String> tokenizeQuery(String query) {
		List<String> out = new ArrayList<String>();
		Scanner fileScanner;
		fileScanner = new Scanner(query);
   	 	String word;
		while (fileScanner.hasNext()) {
    		 word = fileScanner.next();
        	 String[] words = word.split("[^a-zA-Z0-9]");
        	 for(int i=0; i<words.length; i++) {
        		 if(words[i].isEmpty()) continue;
        		 out.add(words[i].toLowerCase());
        	 }
		}
		fileScanner.close();
		return out;
	}
	public static List<String> tokenizeFile(String fileName) throws IOException {
		String text = FileUtils.readFileToString(new File(fileName));
		String[] tokens = text.trim().split("[^A-Za-z0-9]");
		List<String> tokensList = new ArrayList<String>();
		for (int i = 0; i < tokens.length; i++){
			if( tokens[i].isEmpty()) continue;
			tokensList.add( tokens[i].toLowerCase() );
		}
		return tokensList;
	}
}
