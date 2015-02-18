
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Token {
	
	@SuppressWarnings("resource")
	public static List<String> tokenizeFile(String fileName) {
		File textFile = new File(fileName);
		List<String> out = new ArrayList<String>();
		Scanner fileScanner;
		try {
			fileScanner = new Scanner(textFile);
		} catch (FileNotFoundException e) {
			return out;
		}
		while (fileScanner.hasNextLine()) {
	         String line = fileScanner.nextLine();
	         Scanner wordScanner = new Scanner(line);
	         
	         while(wordScanner.hasNext()) {
	        	 String word;
        		 word = wordScanner.next();
	        	 String[] words = word.split("[^a-zA-Z0-9]");
	        	 for(int i=0; i<words.length; i++) {
	        		 if(words[i].isEmpty()) continue;
	        		 out.add(tokenize(words[i]));
	        	 }
	         }
		}
		return out;
		
	}
	
	private static String tokenize(String w) {
		return w.toLowerCase().replaceAll("[^A-Za-z0-9]", "");
	}
}