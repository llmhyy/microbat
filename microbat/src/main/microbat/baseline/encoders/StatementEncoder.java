package microbat.baseline.encoders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import microbat.baseline.Configs;
import microbat.baseline.constraints.Constraint;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class StatementEncoder {
	private Trace trace;
	private List<TraceNode> slice;
	private HashMap<BreakPoint, Integer> instOccurence = new HashMap<>();
	private HashMap<BreakPoint, Integer> instInSlice = new HashMap<>();
	private HashMap<String, Integer> sliceInFunc = new HashMap<>();
	private HashSet<String> faultyVars = new HashSet<>();
	private boolean populated = false;
	private int sliceSize = 0;
	
	public StatementEncoder(Trace trace, List<TraceNode> slice) {
		this.trace = trace;
		this.slice = slice;
		TraceNode last = slice.get(slice.size() - 1);
		for (VarValue v : last.getReadVariables()) {
			faultyVars.add(v.getVarName());
		}
	}
	
	public boolean encode() {
		boolean hasChange = false;
		for (TraceNode tn : slice) {
			hasChange = encode(tn) || hasChange;
		}
		return hasChange;
	}
	
	private boolean encode(TraceNode tn) {
		boolean hasChange = false;
		double prob = structureConstraint(tn) * varToStatementConstraint(tn) * Math.log10((1+ nameConstraint(tn)) * 10);
		if (Math.abs(tn.getProbability() - prob) > 0.01) {
			hasChange = true;
		}
		tn.setProbability(prob);
		return hasChange;
	}
	
	private void populateCount() {
		for (TraceNode tn : trace.getExecutionList()) {
			BreakPoint bp = tn.getBreakPoint();
			TraceNode next = tn.getStepOverNext();
			// if they return to the same breakpoint, it means they are the same line
			if (next != null && bp.equals(next.getBreakPoint()))
				continue;
			if (!instOccurence.containsKey(bp))
				instOccurence.put(bp, 1);
			else
				instOccurence.put(bp, instOccurence.get(bp) + 1);
		}
		
		for (TraceNode tn : slice) {
			BreakPoint bp = tn.getBreakPoint();
			TraceNode next = tn.getStepOverNext();
			// if they return to the same breakpoint, it means they are the same line
			if (next != null && bp.equals(next.getBreakPoint()) && slice.contains(next))
				continue;
			if (!instInSlice.containsKey(bp))
				instInSlice.put(bp, 1);
			else
				instInSlice.put(bp, instInSlice.get(bp) + 1);
			
			String key = bp.getMethodSign();
			if (!sliceInFunc.containsKey(key))
				sliceInFunc.put(key, 1);
			else
				sliceInFunc.put(key, sliceInFunc.get(key) + 1);
		}
		
		for (int value : instInSlice.values())
			this.sliceSize += value;
		this.populated = true;
	}
	
	private double structureConstraint(TraceNode node) {
		if (!populated)
			populateCount();
		BreakPoint bp = node.getBreakPoint();
		String key = bp.getMethodSign();
		double prop1 = (double) sliceInFunc.getOrDefault(key, 0) / this.sliceSize;
		int allInstOccurence = instOccurence.getOrDefault(bp, 0);
		int instsNotInSlice = allInstOccurence - instInSlice.getOrDefault(bp, 0);
		double prop2 = (double) instsNotInSlice / allInstOccurence;
		return 0.5 * (sigma1(prop1) + sigma2(prop2));
	}
	
	private double nameConstraint(TraceNode node) {
		double score = 0.0;
		for (VarValue v : node.getWrittenVariables()) {
			String varName = v.getVarName();
			for (String fVarName : this.faultyVars) {
				score = Math.max(editDistance(varName, fVarName), score);
			}
		}
		return score;
	}
	
	private double varToStatementConstraint(TraceNode tn) {
		boolean isReadCorrect = true;
		for (VarValue v : tn.getReadVariables()) {
			if (v.getProbability() < 0.3) {
				isReadCorrect = false;
				break;
			}
		}
		
		boolean isWrittenCorrect = true;
		for (VarValue v : tn.getWrittenVariables()) {
			if (v.getProbability() < 0.3) {
				isWrittenCorrect = false;
				break;
			}
		}
		
		if (isReadCorrect && isWrittenCorrect) {
			// all read and written variables are correct
			return Configs.HIGH;
		} else if (isReadCorrect) {
			// all read variables are correct but at least one written var is wrong
			return Configs.LOW;
		} else if (isWrittenCorrect){
			// all written var is correct but one read variable is wrong 
			return Configs.HIGH; // TODO: how to allocate this probability
		} else {
			// at least one written and read var is wrong
			return Configs.HIGH;
		}
	}
	
	private double sigma1(double x) {
		return 0.5 - 0.5 * (2 * Configs.HIGH - 1) * x;
	}
	
	private double sigma2(double x) {
		return 0.5 + 0.5 * (2 * Configs.HIGH - 1) * x;
	}
	
	private static double editDistance(String s1, String s2) {
        /* implements OSA Damerau-Levenshtein distance */
        // add padding in front
        s1 = " " + s1;
        s2 = " " + s2;

        char[] a1 = s1.toCharArray();
        char[] a2 = s2.toCharArray();

        // initialize the array
        int[][] distanceMatrix = new int[a1.length][a2.length];
        for (int i = 0; i < distanceMatrix.length; i++)
            distanceMatrix[i][0] = i;
        for (int i = 0; i < distanceMatrix[0].length; i++)
            distanceMatrix[0][i] = i;

        for (int i = 1; i < distanceMatrix.length; i++) {
            for (int j = 1; j < distanceMatrix[0].length; j++) {
                int cost = a1[i] == a2[j] ? 0 : 1;
                distanceMatrix[i][j] = minimum(distanceMatrix[i-1][j] + 1, 
                                                  distanceMatrix[i][j-1] + 1,
                                                  distanceMatrix[i-1][j-1] + cost);
                if (i > 1 && j > 1 && a1[i] == a2[j - 1] && a1[i -1] == a2[j]) {
                    distanceMatrix[i][j] = minimum(distanceMatrix[i][j],
                                                   distanceMatrix[i-2][j-2] + 1);
                }
            }
        }
        int editDistance = distanceMatrix[s1.length() - 1][s2.length() - 1];
        double lenSum = (double) (s1.length() - 1 + s2.length() - 1);
        double similarity =  lenSum - editDistance;
        return similarity / lenSum;
    }
	
    private static int minimum (int a, int... b) {
        int min = a;
        for (int i : b) {
            min = min < i ? min : i;
        }
        return min;
    }
}
