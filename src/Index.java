import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
		private List<Tuple> tfidfTuples;
		private List<Tuple> positionTuples;
		
//		private Map<String, <double, List<double> >
		
		public IndexComponents() {
			tfidfTuples = new ArrayList<Index.Tuple>();
			positionTuples = new ArrayList<Index.Tuple>();
		}
		
		public void putPositionTuple(Tuple t) {
			positionTuples.add(t);
		}
		
		public List<Tuple> getTfidfTuples() {
			return tfidfTuples;
		}
		
		public List<Tuple> getPositionTuples() {
			return positionTuples;
		}
		
		@Override
		public String toString(){
			return "TFIDF: " + listToString(tfidfTuples) + "\n" + "position index: " + listToString(positionTuples) + "\n";
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
	
	public void putPositionIndex(String word, String docId, int position) {
		if(!index.containsKey(word)) {
			IndexComponents comp = new IndexComponents();
			comp.putPositionTuple(new Tuple(docId, (double) position));
			index.put(word, comp);
		} else {
			IndexComponents comp = index.get(word);
			comp.putPositionTuple(new Tuple(docId, (double) position));
			index.put(word, comp);
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sBuilder = new StringBuilder();
		for(String w : index.keySet()) {
			sBuilder.append("Word: " + w + "\n" + index.get(w).toString());
		}
		return sBuilder.toString();
	}
	
	// For testing: should have put it into unittests folder but not sure how to create
	public static void main(String args[]) {
		Index index = new Index();
		index.putPositionIndex("word", "d1", 1);
		index.putPositionIndex("word", "d2", 1);
		index.putPositionIndex("w2", "d1", 2);
		System.out.println(index.toString());
	}
}
