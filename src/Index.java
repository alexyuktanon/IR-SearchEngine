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
		
	}
	
	private class IndexComponents {
		private List<Tuple> tfidfTuples;
		private List<Tuple> positionTuples;
		
//		private Map<String, <double, List<double> >
		
		public IndexComponents() {
			tfidfTuples = new ArrayList<Index.Tuple>();
			positionTuples = new ArrayList<Index.Tuple>();
		}
	}

	private Map<String, IndexComponents> index = new HashMap<String, IndexComponents>();
	
	
	
}
