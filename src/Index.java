import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;


public class Index {
	
	private class Tuple {
		private String docId;
		private double val;
		
		public Tuple(String docId, double val) {
			this.docId = docId;
			this.val = val;
		}
		public double getVal() {
			return val;
		}
		public String getDocId() {
			return docId;
		}
		@Override
		public String toString() {
			return "("+docId+","+val+")";
		}
		
	}
	
	private class IndexComponents {
//		private List<Tuple> tfidfTuples;
		private Map<String, Double> tfidfTuples;
		private MultiMap<String, Integer> positionTuples;
		
//		private Map<String, <double, List<double> >
		
		public IndexComponents() {
//			tfidfTuples = new ArrayList<Index.Tuple>();
			tfidfTuples = new HashMap<String, Double>();
			positionTuples = new MultiValueMap<String, Integer>();
		}
		
		public void putTfidfTuple(String word, double frequency) {
			tfidfTuples.put(word, frequency);
		}
		
		public void putPositionTuple(String word, int position) {
			positionTuples.put(word, position);
		}
		
//		public List<Tuple> getTfidfTuples() {
//			return tfidfTuples;
//		}
		
		public MultiMap<String, Integer> getPositionTuples() {
			return positionTuples;
		}
		
		@Override
		public String toString(){
			return "TFIDF: " + tfidfTuples.toString() + "\n" + "position index: " + positionTuples.toString() + "\n";
		}
		
		private String listToString(List<Tuple> l) {
			if(l.isEmpty()) return "empty";

			String s = "";
			for(Tuple t : l) {
				s += t.toString() + " ";
			}
			return s;
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
	
	public void toFile() throws FileNotFoundException{
		PrintWriter out = new PrintWriter("result.txt");
		for(String w : index.keySet()) {
			out.print("Word: " + w + "\n" + index.get(w).toString());
		}
		out.close();
	}
	
	// For testing: should have put it into unittests folder but not sure how to create
	public static void main(String args[]) {
		Index index = new Index();
		index.putPositionIndex("word", "d1", 1);
		index.putPositionIndex("word", "d1", 5);
		index.putPositionIndex("word", "d2", 1);
		index.putPositionIndex("w2", "d1", 2);
		System.out.println(index.toString());
		MultiMap<String, Integer> positionMap = index.getPositionMap("word");
		for(Object key : positionMap.keySet()) {
			System.out.println(key + " : " + positionMap.get(key));
			List<Integer> l = (List<Integer>) positionMap.get(key);
			for(Integer i : l) System.out.println(i);
		}
	}
}
