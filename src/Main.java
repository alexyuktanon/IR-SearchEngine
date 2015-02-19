import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Main {
	
	public static final String ROOT_FOLDER = "./data/crawl/root/";
	public static final String MAP_FILENAME = "map.txt";
	public static final String MAP_PATH = ROOT_FOLDER+MAP_FILENAME;
	
	public static void main(String args[]) throws IOException {
		File[] listOfFiles = new File(ROOT_FOLDER).listFiles();
		
		Index index = new Index();
		final long startTime = System.currentTimeMillis();
		
		//----- DF (Document Frequency) processing starts ------
		Map<String, Integer> documentsFrequencies = new HashMap<String, Integer>();
		for (File file : listOfFiles) {
			String docId = file.getName();
			
			if (file.isFile() && !docId.equals(MAP_FILENAME)) {
				System.out.println("DF Processing file: "+docId);
				List<String> tokens = Token.tokenizeFile(file.getAbsolutePath());

				//Compute unique tokens
		    	Set<String> uniqueTokensSet = new HashSet<String>();
		    	for(int i = 0; i < tokens.size(); i++){
		    		uniqueTokensSet.add((String) tokens.get(i));
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
			}
		}
//		System.out.println("DF "+documentsFrequencies);
		// ------- end ---------
		
		for (File file : listOfFiles) {
			// all index processing should be in here
			String docId = file.getName();
			
			// check if its valid file
			if (file.isFile() && !docId.equals(MAP_FILENAME)) {
				System.out.println("Processing file: "+docId);
			    List<String> tokens = Token.tokenizeFile(file.getAbsolutePath());
			    
			    // ----- position index processing starts ------
			    int numTokens = tokens.size();
			    for(int i=0; i<numTokens; i++) {
			    	int position = i;
			    	index.putPositionIndex(tokens.get(i), docId, position);
			    }
			    // ------- endddd ---------
			} 
			
		}
		
		PrintWriter out = new PrintWriter("out.txt");
		out.print(index.toString());
		out.close();
		
		final long endTime = System.currentTimeMillis();
		
		System.out.println("Total execution time: " + (endTime - startTime) );
	}
}
