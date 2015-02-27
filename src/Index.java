import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;


public class Index {
	
	

	private Map<String, IndexComponents> index = new HashMap<String, IndexComponents>(); // map word to indexes
	
	public void putIdfIndex(String word, double frequency) {
		IndexComponents comp;
		if(!index.containsKey(word)) {
			comp = new IndexComponents();
		} else {
			comp = index.get(word);
		}
		comp.putIdf(frequency);
		index.put(word, comp);
	}
	
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
	
	public Double getIdf(String word) {
		return index.get(word).getIdf();
	}
	
	public Map<String, Double> getTfidfMap(String word) {
		return index.get(word).getTfidfTuples();
	}
	
	public List<Integer> getPosition(String word, String docId) {
		if(!index.containsKey(word)) return new ArrayList<Integer>();
		Object positions = index.get(word).getPositionTuples().get(docId);
		if(positions == null) return new ArrayList<Integer>();
		return (List<Integer>) positions;
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
	
	public static Index fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		Map<String, IndexComponents> outIndex = new HashMap<String, IndexComponents>();
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> mapObject = mapper.readValue(json,
				new TypeReference<Map<String, Object>>() {
				});
		for(String key : mapObject.keySet()) {
			Map<String, Object> val = (HashMap<String, Object>) mapObject.get(key);
			Double idf = (Double) val.get("idf");
			Map<String, Double> tfidf = (Map<String, Double>) val.get("tfidfTuples");
			Map<String, List<Integer>> positions = (Map<String, List<Integer>>) val.get("positionTuples");
			MultiValueMap<String, Integer> pos = new MultiValueMap<String, Integer>();
			for(Entry<String, List<Integer>> entry : positions.entrySet()) {
				pos.putAll(entry.getKey(), entry.getValue());
			}
			IndexComponents comp = new IndexComponents(idf, tfidf, pos);
			outIndex.put(key, comp);
		}
		return new Index(outIndex);
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
		index.putIdfIndex("word", 0.0);
		index.putIdfIndex("w2", 1.0);
		index.putTfidfIndex("word", "d1", 0.5);
		index.putTfidfIndex("word", "d2", 1);
		index.putTfidfIndex("w2", "d1", 2);
		index.putPositionIndex("word", "d1", 1);
		index.putPositionIndex("word", "d1", 5);
		index.putPositionIndex("word", "d2", 1);
		index.putPositionIndex("w2", "d1", 2);
		System.out.println(index.toString());
		
		System.out.println("+++ IDF TEST +++");
		String token = "word";
		Double idf = index.getIdf(token);
		System.out.println(token + " : " + idf);
		
		System.out.println("\n+++ TFIDF TEST +++");
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
			
			System.out.println(index2.getTfidfMap("word"));
			System.out.println(index2.getPositionMap("word"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
