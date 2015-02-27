import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;


public class IndexComponents {
		private Double idf;
		private Map<String, Double> tfidfTuples;
		private MultiMap<String, Integer> positionTuples;
		
		protected IndexComponents(Double idf, Map<String, Double> tfidfTuples, MultiMap<String, Integer> positionTuples) {
			this.idf = idf;
			this.tfidfTuples = tfidfTuples;
			this.positionTuples = positionTuples;
		}
		
		public IndexComponents() {
			tfidfTuples = new HashMap<String, Double>();
			positionTuples = new MultiValueMap<String, Integer>();
		}
		
		public void putIdf(double frequency) {
			idf = frequency;
		}
		
		public void putTfidfTuple(String word, double frequency) {
			tfidfTuples.put(word, frequency);
		}
		
		public void putPositionTuple(String word, int position) {
			positionTuples.put(word, position);
		}
		
		public Double getIdf() {
			return idf;
		}
		
		public Map<String, Double> getTfidfTuples() {
			return tfidfTuples;
		}
		
		public MultiMap<String, Integer> getPositionTuples() {
			return positionTuples;
		}
		
		@Override
		public String toString(){
			return "IDF: " + idf.toString() + "\n" +
				   "TFIDF: " + tfidfTuples.toString() + "\n" +
				   "position index: " + positionTuples.toString() + "\n";
		}
	}