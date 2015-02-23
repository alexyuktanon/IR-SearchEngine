
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Token {
	
	public static List<String> tokenizeFile(String fileName) throws FileNotFoundException {
		File textFile = new File(fileName);
		List<String> out = new ArrayList<String>();
		Scanner fileScanner;
		fileScanner = new Scanner(textFile);
   	 	String word;
		while (fileScanner.hasNext()) {
    		 word = fileScanner.next();
        	 String[] words = word.split("[^a-zA-Z0-9]");
        	 for(int i=0; i<words.length; i++) {
        		 if(words[i].isEmpty()) continue;
        		 out.add(tokenize(words[i]));
        	 }
		}
		fileScanner.close();
		return out;
		
	}
	
	private static String tokenize(String w) {
		return w.toLowerCase().replaceAll("[^A-Za-z0-9]", "");
	}
}
