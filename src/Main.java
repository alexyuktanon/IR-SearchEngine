import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class Main {
	
	public static final String ROOT_FOLDER = "./data/crawl/root/";
	public static final String MAP_PATH = "./data/crawl/root/map.txt";
	
	public static void main(String args[]) throws IOException {
		File[] listOfFiles = new File(ROOT_FOLDER).listFiles();
		
		Index index = new Index();
		
		for (File file : listOfFiles) {
			// all index processing should be in here
			String docId = file.getName();
			
			// check if its valid file
			if (file.isFile() && !docId.equals(MAP_PATH)) {
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
	}
}
