package microbat.probability.SPP.vectorization;

import java.util.Collection;

import microbat.model.value.VarValue;

public class SourceVariableVector extends TargetVariableVector {

	private final double probability;
	
	public SourceVariableVector(VarValue var) {
		super(var);
		this.probability = var.getProbability();
	}
	
	public static SourceVariableVector summarize(Collection<SourceVariableVector> vectors) {
		return null;
	}
	
	public double getProbability() {
		return this.probability;
	}
	
	@Override
	public boolean equals(Object otherObj) {
		return false;
	}

	@Override
	public String toString() {
		return null;
	}
}
