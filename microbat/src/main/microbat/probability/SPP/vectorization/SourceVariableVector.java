package microbat.probability.SPP.vectorization;

import java.util.Collection;

import microbat.model.value.VarValue;

public class SourceVariableVector extends TargetVariableVector {

	private final double probability;
	
	private SourceVariableVector(double probability) {
		super();
		this.probability = probability;
	}
	
	public SourceVariableVector(VarValue var) {
		super(var);
		this.probability = var.getProbability();
	}
	
	public static SourceVariableVector summarize(Collection<SourceVariableVector> vectors) {
		double avgProb = 0.0;
		for(SourceVariableVector vector : vectors) {
			avgProb += vector.getProbability();
		}
		avgProb /= vectors.size();
		
		SourceVariableVector result = new SourceVariableVector(avgProb);
		for(SourceVariableVector vector : vectors) {
			boolean[] featureVector = vector.getFeatureArray();
			for(int idx=0; idx<featureVector.length; idx++) {
				boolean feature = featureVector[idx];
				if (feature) {
					result.setFeature(idx, feature);
				}
			}
		}
		
		return result;
	}
	
	public double getProbability() {
		return this.probability;
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if (!super.equals(otherObj)) {
			return false;
		}
		SourceVariableVector otherVec = (SourceVariableVector) otherObj;
		return this.probability == otherVec.probability;
	}

	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(super.toString());
		strBuilder.append(",");
		strBuilder.append(String.format("%.2f", this.probability));
		return strBuilder.toString();
	}
}
