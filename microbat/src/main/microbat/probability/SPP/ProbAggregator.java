package microbat.probability.SPP;

import java.util.Collection;
import java.util.List;

import microbat.model.value.VarValue;

public class ProbAggregator {
	
	public final static double NON_PROB = -1.0;
	
	public double aggregate(Collection<VarValue> vars, ProbAggregateMethods method) {
		if (vars == null) {
			throw new IllegalArgumentException("Input vars is null");
		}
		
		if (vars.size() == 0) {
			return ProbAggregator.NON_PROB;
		}
		
		switch (method) {
		case AVG:
			return this.aggregateAVG(vars);
		case MAX:
			return this.aggregateMAX(vars);
		case MIN:
			return this.aggregateMIN(vars);
		default:
			return ProbAggregator.NON_PROB;
		}
	}
	
	private double aggregateAVG(Collection<VarValue> vars) {
		double prob = 0.0;
		for (VarValue var : vars) {
			prob += var.getProbability();
		}
		return prob / vars.size();
	}
	
	private double aggregateMAX(Collection<VarValue> vars) {
		double prob = 0.0;
		for (VarValue var : vars) {
			if (var.getProbability() > prob) {
				prob = var.getProbability();
			}
		}
		return prob;
	}
	
	private double aggregateMIN(Collection<VarValue> vars) {
		double prob = 1.0;
		for (VarValue var : vars) {
			if (var.getProbability() < prob) {
				prob = var.getProbability();
			}
		}
		return prob;
	}
}
