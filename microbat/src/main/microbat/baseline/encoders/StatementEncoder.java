package microbat.baseline.encoders;

import java.util.HashMap;
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
	private boolean populated = false;
	private int sliceSize = 0;
	
	public StatementEncoder(Trace trace, List<TraceNode> slice) {
		this.trace = trace;
		this.slice = slice;
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
		double prob = structureConstraint(tn) * varToStatementConstraint(tn);
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
		/*
		 * in the intial project, they trained three different classification
		 * model to calculate the probability. in our case, we will be using
		 * a simple max edit distance to calculate the probability
		 * 
		 * since we are implementing Java, it might be wiser to look at the return
		 * type to determine
		 */
		return 0.0;
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
}
