package microbat.probability.SPP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import microbat.model.value.VarValue;

public class ProbAggregator {
	
	public final static double NON_PROB = -1.0;
	
	public double aggregateProb(Collection<VarValue> vars, ProbAggregateMethods method) {
		if (!this.isReady(vars)) {
			return NON_PROB;
		}
		
		List<Double> probs = new ArrayList<>();
		for (VarValue var : vars) {
			probs.add(var.getProbability());
		}
		
		return this.aggregate(probs, method);
	}
	
	public double aggregateForwardProb(Collection<VarValue> vars, ProbAggregateMethods method) {
		if (!this.isReady(vars)) {
			return NON_PROB;
		}
		
		List<Double> probs = new ArrayList<>();
		for (VarValue var : vars) {
			probs.add(var.getForwardProb());
		}
		
		return this.aggregate(probs, method);
		
	}
	
	public double aggregateBackwardProb(Collection<VarValue> vars, ProbAggregateMethods method) {
		if (!this.isReady(vars)) {
			return NON_PROB;
		}
		
		List<Double> probs = new ArrayList<>();
		for (VarValue var : vars) {
			probs.add(var.getBackwardProb());
		}
		
		return this.aggregate(probs, method);
		
	}
	
	private boolean isReady(Collection<VarValue> vars) {
		if (vars == null) {
			throw new IllegalArgumentException("Input vars is null");
		}
		
		if (vars.isEmpty()) {
			return false;
		}
		
		return true;
	}
	
	private double aggregate(Collection<Double> probs, ProbAggregateMethods method) {
		switch (method) {
		case AVG:
			return this.aggregateAVG(probs);
		case MAX:
			return this.aggregateMAX(probs);
		case MIN:
			return this.aggregateMIN(probs);
		default:
			return ProbAggregator.NON_PROB;
		}
	}
	
	private double aggregateAVG(Collection<Double> probs) {
		double avgProb = 0.0;
		for (double prob : probs) {
			avgProb += prob;
		}
		return avgProb / probs.size();
	}
	
	private double aggregateMAX(Collection<Double> probs) {
		double maxProb = 0.0;
		for (double prob : probs ) {
			if (prob > maxProb) {
				maxProb = prob;
			}
		}
		return maxProb;
	}
	
	private double aggregateMIN(Collection<Double> probs) {
		double minProb = 1.0;
		for (double prob : probs) {
			if (prob < minProb) {
				minProb = prob;
			}
		}
		return minProb;
	}
}
