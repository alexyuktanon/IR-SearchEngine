import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


public class Main {
	
	public static final String ROOT_FOLDER = "./data/crawl/root/";
	public static final String MAP_FILENAME = "map.txt";
	public static final String MAP_PATH = ROOT_FOLDER+MAP_FILENAME;
	
	public static void main(String args[]) throws IOException {
		File[] listOfFiles = new File(ROOT_FOLDER).listFiles();
		
		Index index = new Index();
		final long startTime = System.currentTimeMillis();
		
		//----- DF (Document Frequency) processing starts ------
		int corpus = 0;
		Map<String, Integer> documentsFrequencies = new HashMap<String, Integer>();
		Set<String> uniqueTotalTokensSet = new HashSet<String>(); //For compute unique words
		for (File file : listOfFiles) {
			String docId = file.getName();
			
			if (file.isFile() && !docId.equals(MAP_FILENAME) && !docId.equals(".DS_Store")) {
				System.out.println("DF Processing file: "+docId);
				List<String> tokens = Token.tokenizeFile(file.getAbsolutePath());

				//Compute unique tokens
		    	Set<String> uniqueTokensSet = new HashSet<String>();
		    	for(int i = 0; i < tokens.size(); i++){
		    		uniqueTokensSet.add((String) tokens.get(i));
		    		uniqueTotalTokensSet.add((String) tokens.get(i));
		    	}
		    	List<String> uniqueTokens = new ArrayList<String>(uniqueTokensSet);

		    	//Compute document frequency
		    	int numTokens = uniqueTokens.size();
			    for(int i = 0; i < numTokens; i++) {
					if(documentsFrequencies.containsKey(uniqueTokens.get(i))){
						int currentFrequency = (int) documentsFrequencies.get(uniqueTokens.get(i));
						currentFrequency++;
						documentsFrequencies.put(uniqueTokens.get(i).toString(), currentFrequency);
					}else{
						//If there is no token in the hashmap, add new
						documentsFrequencies.put(uniqueTokens.get(i).toString(), 1);
					}
			    }
			    
			    //Update corpus frequency
			    corpus++;
			}
		}
		List<String> uniqueTotalTokens = new ArrayList<String>(uniqueTotalTokensSet);
		
		//System.out.println("DF " + documentsFrequencies);
		//System.out.println("Corpus " + corpus);
		//System.out.println("Total number of unique words: " + uniqueTotalTokens.size());
		// ------- end ---------
		
		for (File file : listOfFiles) {
			// all index processing should be in here
			String docId = file.getName();
			
			// check if its valid file
			if (file.isFile() && !docId.equals(MAP_FILENAME) && !docId.equals(".DS_Store")) {
				System.out.println("Processing file: "+docId);
			    List<String> tokens = Token.tokenizeFile(file.getAbsolutePath());
			    int numTokens = tokens.size();
			    
			    // ----- TFDIF processing starts ------
			    //Compute TF (Term Frequency)
			    Map<String, Integer> tokensFrequencies = new HashMap<String, Integer>();
			    for(int i = 0; i < numTokens; i++) {
					if(tokensFrequencies.containsKey(tokens.get(i))){
						int currentFrequency = (int) tokensFrequencies.get(tokens.get(i));
						currentFrequency++;
						tokensFrequencies.put(tokens.get(i).toString(), currentFrequency);
					}else{
						//If there is no token in the hashmap, add new
						tokensFrequencies.put(tokens.get(i).toString(), 1);
					}
			    }
			    
			    //Compute TDIDF
				Object[] frequenciesArray = tokensFrequencies.entrySet().toArray();
			    for (Object e : frequenciesArray) {
			        Entry<String, Integer> entry = (Map.Entry<String, Integer>) e;

			        double tfValue = 1 + ( Math.log( entry.getValue() ) / Math.log(2) );
			        double dfValue = documentsFrequencies.get(entry.getKey());
			        double idfValue = Math.log( ( Math.abs( corpus ) / dfValue ) ) / Math.log(2);
			        double tfidfValue = tfValue * idfValue;

			        index.putTfidfIndex(entry.getKey(), docId, tfidfValue);
			        //System.out.println(entry.getKey() + ", " + entry.getValue() + ", " + tfValue + ", " + dfValue + ", " + idfValue + ", " + tfidfValue);
					//System.out.println(entry.getKey() + " - " + tfidfValue);	
			    }
			    // ------- end ---------
			    
			    // ----- position index processing starts ------
			    for(int i=0; i<numTokens; i++) {
			    	int position = i;
			    	index.putPositionIndex(tokens.get(i), docId, position);
			    }
			    // ------- endddd ---------
			} 
			
		}
		
		//Print index to file
		index.toFile();
		
		final long endTime = System.currentTimeMillis();
		
		System.out.println("Total execution time: " + (endTime - startTime) );
		System.out.println("Total number of unique words: " + uniqueTotalTokens.size());
	}
}
