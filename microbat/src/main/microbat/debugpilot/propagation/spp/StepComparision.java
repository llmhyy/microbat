package microbat.debugpilot.propagation.spp;

import microbat.log.Log;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.vectorization.vector.NodeVector;
import microbat.vectorization.vector.VariableVector;
import microbat.vectorization.vector.Vector;

public class StepComparision {

	protected double stepThreshold;
	protected double variableThreshold;

	public StepComparision() {
		this(0.8, 0.85);
	}

	public StepComparision(final double stepThreshold, final double variableThreshold) {
		this.stepThreshold = stepThreshold;
		this.variableThreshold = variableThreshold;
	}

	public double calCosSimilarity(final Vector vector1, final Vector vector2) {
		return this.calCosSimilarity(vector1.getVector(), vector2.getVector());
	}
	
	public double calCosSimilarity(final float[] array1, final float[] array2) {
		if (array1.length != array2.length) {
			throw new IllegalArgumentException(Log.genMsg(getClass(), "Both vector should have the same size"));
		}

		double dotProduct = 0.0d;
		double norm1 = 0.0d;
		double norm2 = 0.0d;

		for (int i = 0; i < array1.length; ++i) {
			dotProduct += array1[i] * array2[i];
			norm1 += array1[i] * array1[i];
			norm2 += array2[i] * array2[i];
		}

		if (norm1 == 0.0 || norm2 == 0.0) {
			return 0.0d;
		}

		return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
	}

	protected int calLevenshteinDistance(String s1, String s2) {
		int[][] dp = new int[s1.length() + 1][s2.length() + 1];

		for (int i = 0; i <= s1.length(); i++) {
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else {
					int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
					dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
				}
			}
		}
		return dp[s1.length()][s2.length()];
	}

	protected double calVarNameSimilarity(final String name1, final String name2) {
		final int lavenshteinDistance = this.calLevenshteinDistance(name1, name2);
		final int maxLength = Math.max(name1.length(), name2.length());
		return 1.0d - (double) lavenshteinDistance / maxLength;
	}

	public double getStepThreshold() {
		return stepThreshold;
	}

	public double getVariableThreshold() {
		return variableThreshold;
	}

	protected VariableVector getVariableVector(final VarValue var) {
		if (var == null)
			return new VariableVector();
		if (var.isConditionResult())
			return new VariableVector();
		return new VariableVector(var);
	}

	public boolean isSimiliar(final TraceNode node1, final TraceNode node2) {
		final NodeVector vector1 = new NodeVector(node1);
		final NodeVector vector2 = new NodeVector(node2);
		final double cosSim = this.calCosSimilarity(vector1.getVector(), vector2.getVector());
		return cosSim >= this.stepThreshold;
	}

	public boolean isSimiliar(final VarValue var1, final VarValue var2) {
		if (var1.isConditionResult() && var2.isConditionResult()) {
			return true;
		}
		final VariableVector vector1 = new VariableVector(var1);
		final VariableVector vector2 = new VariableVector(var2);
		double cosSim;

		cosSim = this.calCosSimilarity(vector1.getVector(), vector2.getVector());

		double nameSim = this.calVarNameSimilarity(var1.getVarName(), var2.getVarName());
		double sim = cosSim * nameSim;
		return sim >= this.variableThreshold;
	}

	public void setStepThreshold(double stepThreshold) {
		this.stepThreshold = stepThreshold;
	}

	public void setVariableThreshold(double variableThreshold) {
		this.variableThreshold = variableThreshold;
	}
}
