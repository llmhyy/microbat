package microbat.evaluation.util;

import java.util.ArrayList;
import java.util.List;

import microbat.evaluation.model.PairList;
import microbat.evaluation.model.TraceNodePair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class DiffUtil {
	/**
	 * compare traces directly seems not working, which may report may false positive for the matching.
	 * 
	 * @param multisetList
	 * @param commonTokenList
	 * @param tokenList2
	 */
	@Deprecated
	public static PairList generateMatchedTraceNodeList(Trace mutatedTrace, Trace correctTrace) {
		
		TraceNode[] mutatedTraceArray = mutatedTrace.getExecutionList().toArray(new TraceNode[0]);
		TraceNode[] correctTraceArray = correctTrace.getExecutionList().toArray(new TraceNode[0]);
		
		return generateMatchedTraceNodeList(mutatedTraceArray, correctTraceArray, new TraceNodeComprehensiveSimilarityComparator());
	}
	
	public static PairList generateMatchedTraceNodeList(TraceNode[] mutatedTraceArray, TraceNode[] correctTraceArray,
			TraceNodeSimilarityComparator sc){
		
		List<TraceNodePair> pairList = new ArrayList<>();
		double[][] scoreTable = buildScoreTable(mutatedTraceArray, correctTraceArray, sc);

		for (int i = mutatedTraceArray.length, j = correctTraceArray.length; (i > 0 && j > 0);) {
			if (mutatedTraceArray[i - 1].hasSameLocation(correctTraceArray[j - 1])) {
				
				double sim = sc.compute(mutatedTraceArray[i - 1], correctTraceArray[j - 1]);
				double increase = scoreTable[i][j]-scoreTable[i-1][j-1];
				
				if(Math.abs(sim - increase) < 0.01){
					TraceNodePair pair = new TraceNodePair(mutatedTraceArray[i - 1], correctTraceArray[j - 1]);
					pairList.add(pair);
					
					pair.setExactSame(sim > 0.99);
					
					i--;
					j--;
				}
				else{
					if (scoreTable[i - 1][j] >= scoreTable[i][j - 1]){
						i--;					
					}
					else{
						j--;					
					}
				}
				
			} else {
				if (scoreTable[i - 1][j] >= scoreTable[i][j - 1]){
					i--;					
				}
				else{
					j--;					
				}
			}
		}

		reverseOrder(pairList);
		PairList list = new PairList(pairList);
		
		return list;
	}
	
	public static void reverseOrder(List<TraceNodePair> pairList){
		
		int midIndex = pairList.size()/2;
		for(int i=0; i<midIndex; i++){
			TraceNodePair tmp = pairList.get(i);
			pairList.set(i, pairList.get(pairList.size()-1-i));
			pairList.set(pairList.size()-1-i, tmp);
		}
		
	}
	
	private static double[][] buildScoreTable(TraceNode[] nodeList1, TraceNode[] nodeList2, TraceNodeSimilarityComparator comparator){
		double[][] similarityTable = new double[nodeList1.length + 1][nodeList2.length + 1];
		for (int i = 0; i < nodeList1.length + 1; i++)
			similarityTable[i][0] = 0;
		for (int j = 0; j < nodeList2.length + 1; j++)
			similarityTable[0][j] = 0;

		for (int i = 1; i < nodeList1.length + 1; i++){
			for (int j = 1; j < nodeList2.length + 1; j++) {
				if (nodeList1[i - 1].hasSameLocation(nodeList2[j - 1])){
					if(nodeList1[i - 1].getOrder() == 103 && nodeList2[j-1].getOrder()== 95){
						System.currentTimeMillis();
					}
					if(nodeList1[i - 1].getOrder() == 103 && nodeList2[j-1].getOrder()==194){
						System.currentTimeMillis();
					}
					
					double value = similarityTable[i - 1][j - 1] + comparator.compute(nodeList1[i - 1], nodeList2[j - 1]);
					similarityTable[i][j] = getLargestValue(value, similarityTable[i-1][j], similarityTable[i][j-1]);
				}
				else {
					similarityTable[i][j] = (similarityTable[i - 1][j] >= similarityTable[i][j - 1]) ? 
							similarityTable[i - 1][j] : similarityTable[i][j - 1];
				}
			}
		}
		
		return similarityTable;
	}
	
	
	public static double getLargestValue(double entry1, double entry2, double entry3){
		double value = (entry1 > entry2)? entry1 : entry2;
		return (value > entry3)? value : entry3;
	}
}
