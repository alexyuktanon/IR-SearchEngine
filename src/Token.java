
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;


public class Token {
	
	public static List<String> tokenizeText(String text) {
		String[] tokens = text.trim().toLowerCase().split("[^A-Za-z0-9]");
		List<String> tokensList = new ArrayList<String>();
		for (int i = 0; i < tokens.length; i++){
			if( tokens[i].isEmpty()) continue;
			tokensList.add( tokens[i] );
		}
		return tokensList;
	}
	public static List<String> tokenizeFile(String fileName) throws IOException {
		String text = FileUtils.readFileToString(new File(fileName));
		return tokenizeText(text);
	}
}
