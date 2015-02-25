import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Snippet {
	
	private static final int MAX_NUM_WORDS = 40;
	private static final int WORDS_FROM_CENTER = 5;
	
	/**
	 * interval class, each containing range: (min, max)
	 * number of words of interest inside the interval
	 * number of tokens in interval
	 * @author norrathep
	 *
	 */
	private class Interval {
		int min;
		int max;
		int num;
		
		// TODO: what if order matters?
		Set<String> words = new HashSet<String>();
		
		public Interval(int center) {
			this(Math.max(0, center - WORDS_FROM_CENTER), center + WORDS_FROM_CENTER);
		}
		
		public Interval(int min, int max) {
			this.min = min;
			this.max = max;
			this.num = max-min+1;
		}
		
		public Interval(Interval in1, Interval in2) {
			this(Math.min(in1.min, in2.min), Math.max(in1.max, in2.max));
			this.words.addAll(in1.words);
			this.words.addAll(in2.words);
		}
		
		public void addWord(String word) {
			words.add(word);
		}
		
		public boolean isOverlap(int start, int end) {
			return min<=end && start<=max;
		}
	}
	
	/**
	 * from stackoverflow...
	 * TODO: dont merge if interval is too large?
	 * @param intervals
	 * @return
	 */
	private List<Interval> merge(List<Interval> intervals) {
		 
		if (intervals == null || intervals.size() <= 1)
			return intervals;
 
		// sort intervals by using self-defined Comparator
		Collections.sort(intervals, new IntervalRangeComparator());
 
		List<Interval> result = new ArrayList<Interval>();
 
		Interval prev = intervals.get(0);
		for (int i = 1; i < intervals.size(); i++) {
			Interval curr = intervals.get(i);
 
			if (prev.max >= curr.min) {
				// merged case
				Interval merged = new Interval(prev, curr);
				prev = merged;
			} else {
				result.add(prev);
				prev = curr;
			}
		}
 
		result.add(prev);
 
		return result;
	}
	
	class IntervalRangeComparator implements Comparator<Interval> {
		public int compare(Interval i1, Interval i2) {
			return i1.min - i2.min;
		}
	}
	
	class IntervalContentComparator implements Comparator<Interval> {
		public int compare(Interval i1, Interval i2) {
			if(i1.words.size() != i2.words.size()) return i2.words.size() - i1.words.size();
			
			return i2.num - i1.num;
		}
	}

	/**
	 * How this works: 
	 * 1) find all intervals containing each query in docId by using position index
	 * 2) merge them based on the range
	 * @param index
	 * @param docId
	 * @param query
	 * @return
	 */
	private List<Interval> getLazySnippetIntervals(Index index, String docId, String query) {
		List<String> queryTokens = Token.tokenizeText(query);
		List<Interval> lazyIntervals = new ArrayList<Interval>();
		for(String token : queryTokens) {
			List<Integer> positions = index.getPosition(token, docId);
			if(positions.isEmpty()) continue;
			for(int j=0; j<positions.size(); j++) {
				Interval in = new Interval(positions.get(j));
				in.addWord(token);
				lazyIntervals.add(in);
			}
		}
		return merge(lazyIntervals);
	}
	
	/**
	 * @param intervals
	 * @param doc
	 * @return
	 */
	private String getSnippet(List<Interval> intervals, String doc) {
		StringBuilder builder = new StringBuilder();
		
		// slow but accurate
		List<String> whiteSpaceSepWords = Arrays.asList(doc.split(" "));
		for(Interval in : intervals) {
			int tokenIndex = 0;
			for(int i=0; i<whiteSpaceSepWords.size(); i++) {
				String word = whiteSpaceSepWords.get(i);
				int numTokensInWord = Token.tokenizeText(word).size();
				if(in.isOverlap(tokenIndex, tokenIndex+numTokensInWord-1)) {
					builder.append(word);
					builder.append(" ");
				}
				tokenIndex += numTokensInWord;
				
				// if outside of interval
				if(tokenIndex > in.max) continue;
			}
			builder.append("... ");
		}
		
		// not 100% of the time is correct but fast
//		for(Interval in : intervals) {
//			List<String> subList = tokens.subList(in.min, in.max);
//			for(String t : subList) {
//				builder.append(t);
//				builder.append(" ");
//			}
//			builder.append("... ");
//		}
		return builder.toString();
	}
	
	/**
	 * 1) get lazy intervals
	 * 2) sort them based on the number of query words in which each interval contains
	 * 3) get top K intervals
	 * 4) call getSnippet(intervals, doc);
	 * @param doc
	 * @param query
	 * @param index
	 * @return
	 */
	public String getSnippet(String doc, String docId, String query, Index index) {
		List<Interval> intervals = getLazySnippetIntervals(index, docId, query);
		Collections.sort(intervals, new IntervalContentComparator());
		List<Interval> outputIntervals = new ArrayList<Interval>();
		int numWords = 0;
		
		// TODO: want |all intervals| = MAX_NUM_WORDS? now its ~=
		for(int i=0; i<intervals.size() && numWords < MAX_NUM_WORDS; i++) {
			outputIntervals.add(intervals.get(i));
			numWords += intervals.get(i).num;
		}
		Collections.sort(outputIntervals, new IntervalRangeComparator());
		return getSnippet(outputIntervals, doc);
	}
	
	// basic testing
	public static void main(String[] arg) {
		String query = "information resources overload";
		String doc = "Information retrieval (IR) is the activity of obtaining information resources relevant to an information need from a collection of information resources. Searches can be based on metadata or on full-text (or other content-based) indexing." +
						" Automated information retrieval systems are used to reduce what has been called \"information overload\". Many universities and public libraries use IR systems to provide access to books, journals and other documents. Web search engines are the most visible IR applications.";
		String docId = "d";
		List<String> docTokens = Token.tokenizeText(doc);
		Index index = new Index();
		for(int i=0; i<docTokens.size(); i++ )
			index.putPositionIndex(docTokens.get(i), docId, i);
		System.out.println(new Snippet().getSnippet(doc, docId, query, index));
	}
}
