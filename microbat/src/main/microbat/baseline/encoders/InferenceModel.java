package microbat.baseline.encoders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import microbat.baseline.BitRepresentation;
import microbat.baseline.constraints.Constraint;

/**
 * Probability inference model is used to calculate the probability correctness based on different cases
 * @author Siang Hwee, David
 *
 */
public class InferenceModel {
	
	private List<Constraint> constraints;
	private int varsCount;
	
	public InferenceModel(List<Constraint> constraints) {
		this.constraints = constraints;
		
		if (!this.constraints.isEmpty()) {
			this.varsCount = this.constraints.get(0).getVarsCount();
		}
	}
	
	/**
	 * Get the probability of correctness of target variable or statement
	 * @param conclusionIdx Index of target variable or statement
	 * @return Probability of correctness of target. -1 if constraints are not valid or empty
	 */
	public double getProbability(final int targetIdx) {
		
		if (!this.verifyConstraints()) {
			return -1;
		}
		
		final int maxInt = 1 << this.varsCount;
		
		// Store the calculate result in memory table to avoid redundant calculation
		HashMap<Integer, Double> memoTable = new HashMap<>();
		double denominator = 0;
		for (int caseNo=0; caseNo<maxInt; caseNo++) {
			double product = 1;
			for (Constraint constraint : this.constraints) {
				product *= constraint.getProbability(caseNo);
			}
			memoTable.put(caseNo, product);
			denominator += product;
		}
		
		double sum = 0;
		for (int caseNo: this.getCorrespondenceCaseNo(targetIdx)) {
			sum += memoTable.get(caseNo);
		}
		
		return sum / denominator;
	}
	
	/**
	 * Print the probability inference table for debug purposes
	 */
	public void printTable() {
		final int maxInt = 1 << this.varsCount;
		for (Constraint constraint : this.constraints) {
			System.out.print(constraint.getName() + " ");
		}
		System.out.println();
		for (int caseNo = 0; caseNo < maxInt; caseNo++) {
			BitRepresentation bitRep = BitRepresentation.parse(caseNo, this.varsCount);
			System.out.print(bitRep + "\t");
			for (Constraint constraint : this.constraints) {
				System.out.print(String.format("%.4f", constraint.getProbability(caseNo)) + "\t");
			}
			System.out.println();
		}
	}
	
	/**
	 * Check is the given list of variables are valid.
	 * Those constraints should have the same number of variables involved
	 * @return True if the list of constraints are valid. False otherwise.
	 */
	private boolean verifyConstraints() {
		if (this.constraints.isEmpty()) {
			return false;
		}
		
		final int varsCount = this.constraints.get(0).getVarsCount();
		for (Constraint constraint : this.constraints) {
			if (constraint.getVarsCount() != varsCount) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Get the list of case number that the target index is true
	 * @param targetIdx Index of target variable or statement
	 * @return List of case number that the target is true
	 */
	private List<Integer> getCorrespondenceCaseNo(final int targetIdx) {
		List<Integer> result = new ArrayList<>(1 << this.varsCount);
		int numBitLeft = targetIdx;
		int numBitRight = this.varsCount - (numBitLeft + 1);
		int value = 1 << numBitRight;
		List<Integer> tempResult = new ArrayList<>(value);
		for (int i = 0; i < value; i++) {
			tempResult.add(value + i);
		}
		
		int maxLeft = 1 << numBitLeft;
		for (int i = 0; i < maxLeft; i++) {
			int temp = i << (numBitRight + 1);
			for (int j : tempResult)
				result.add(j + temp);
		}
		return result;
	}
}
