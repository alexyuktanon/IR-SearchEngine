import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;


public class Index {
	
	private class IndexComponents {
		private Map<String, Double> tfidfTuples;
		private MultiMap<String, Integer> positionTuples;
		
		public IndexComponents() {
			tfidfTuples = new HashMap<String, Double>();
			positionTuples = new MultiValueMap<String, Integer>();
		}
		
		public void putTfidfTuple(String word, double frequency) {
			tfidfTuples.put(word, frequency);
		}
		
		public void putPositionTuple(String word, int position) {
			positionTuples.put(word, position);
		}
		
		public Map<String, Double> getTfidfTuples() {
			return tfidfTuples;
		}
		
		public MultiMap<String, Integer> getPositionTuples() {
			return positionTuples;
		}
		
		@Override
		public String toString(){
			return "TFIDF: " + tfidfTuples.toString() + "\n" + "position index: " + positionTuples.toString() + "\n";
		}
	}

	private Map<String, IndexComponents> index = new HashMap<String, IndexComponents>(); // map word to indexes
	
	public void putTfidfIndex(String word, String docId, double frequency) {
		IndexComponents comp;
		if(!index.containsKey(word)) {
			comp = new IndexComponents();
		} else {
			comp = index.get(word);
		}
		comp.putTfidfTuple(docId, frequency);
		index.put(word, comp);
	}
	
	public void putPositionIndex(String word, String docId, int position) {
		IndexComponents comp;
		if(!index.containsKey(word)) {
			comp = new IndexComponents();
		} else {
			comp = index.get(word);
		}
		comp.putPositionTuple(docId, position);
		index.put(word, comp);
	}
	
	public Map<String, Double> getTfidfMap(String word) {
		return index.get(word).getTfidfTuples();
	}
	
	public MultiMap<String, Integer> getPositionMap(String word) {
		return index.get(word).getPositionTuples();
	}
	
	@Override
	public String toString(){
		StringBuilder sBuilder = new StringBuilder();
		for(String w : index.keySet()) {
			sBuilder.append("Word: " + w + "\n" + index.get(w).toString());
		}
		return sBuilder.toString();
	}
	
	private Index(Map<String, IndexComponents> index) {
		this.index = index;
	}
	
	public Index() {}
	
	public String toJson() throws JsonGenerationException, JsonMappingException, IOException {
		return toJson(false);
	}

	public String toJson(boolean prettyPrinter) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectWriter ow = new ObjectMapper().writer();
		if(prettyPrinter) {
			ow = ow.withDefaultPrettyPrinter();
		}
		return ow.writeValueAsString(index);
	}
	
	@SuppressWarnings("unchecked")
	public static Index fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return new Index(new ObjectMapper().readValue(json, HashMap.class));
	}
	
	
	public void toFile() throws FileNotFoundException{
		PrintWriter out = new PrintWriter("result.txt");
		int totalNumWords = 0;
		for(String w : index.keySet()) {
			out.print("Word: " + w + "\n" + index.get(w).toString());
			totalNumWords++;
		}
		out.print("Total number of unique words: " + totalNumWords);
		out.close();
	}
	
	public Integer size(){
		return index.size();
	}
	
	// For testing: should have put it into unittests folder but not sure how to create
	public static void main(String args[]) {
		Index index = new Index();
		index.putTfidfIndex("word", "d1", 0.5);
		index.putTfidfIndex("word", "d2", 1);
		index.putTfidfIndex("w2", "d1", 2);
		index.putPositionIndex("word", "d1", 1);
		index.putPositionIndex("word", "d1", 5);
		index.putPositionIndex("word", "d2", 1);
		index.putPositionIndex("w2", "d1", 2);
		System.out.println(index.toString());
		
		System.out.println("+++ TFDIF TEST +++");
		Map<String, Double> tfidfMap = index.getTfidfMap("word");
		for(Object key : tfidfMap.keySet()) {
			System.out.println(key + " : " + tfidfMap.get(key));
		}
		
		System.out.println("\n+++ POSITION TEST +++");
		MultiMap<String, Integer> positionMap = index.getPositionMap("word");
		for(Object key : positionMap.keySet()) {
			System.out.println(key + " : " + positionMap.get(key));
			@SuppressWarnings("unchecked")
			List<Integer> l = (List<Integer>) positionMap.get(key);
			for(Integer i : l) System.out.println(i);
		}
		
		try {
			String json = index.toJson();
			System.out.println(json);
			Index index2 = Index.fromJson(json);
			json = index2.toJson();
			System.out.println(json);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
