package microbat.baseline.encoders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
import microbat.baseline.constraints.StatementConstraintA1;
import microbat.baseline.constraints.StatementConstraintA2;
import microbat.baseline.constraints.StatementConstraintA3;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Statement encoder is used to calculate the probability of correctness of statement instance (trace node)
 * @author David, Siang Hwee
 *
 */
public class StatementEncoder extends Encoder{
	
	/**
	 * Total number umber of constraints involved
	 */
	private int constraintsCount;
	
	/**
	 * Constructor
	 * @param trace Complete trace of testing program
	 * @param executionList Sliced trace
	 */
	public StatementEncoder(Trace trace, List<TraceNode> executionList) {
		super(trace, executionList);
		this.constraintsCount = 0;
	}
	
	/**
	 * Calculate the probability of correctness of all trace node in sliced trace
	 */
	@Override
	public void encode() {
//		boolean hasChange = false;
		for (TraceNode tn : executionList) {
			this.encode(tn);
		}
		System.out.println("Statement Encoder: " + this.constraintsCount + " constraints");
	}
	
	@Override
	protected int countPredicates(TraceNode node) {
		// For statement encoder, we will consider the statement as well
		return super.countPredicates(node)+1;
	}
	
	@Override
	protected boolean isSkippable(TraceNode node) {
		return this.countPredicates(node) >= 30;
	}
	
	/**
	 * Calculate the probability of correctness of given trace node
	 * @param tn Target trace node
	 */
	private void encode(TraceNode tn) {

		if (this.isSkippable(tn)) {
			return;
		}
		
		final int totalLen = this.countPredicates(tn);
		
		List<Constraint> constraints = this.genConstraints(tn);
		this.constraintsCount += constraints.size();
		final int conclusionIdx = totalLen - 1;
		
		InferenceModel model = new InferenceModel(constraints);
		// model.printTable();
		double prob = model.getProbability(conclusionIdx);

		tn.setProbability(prob);
	}
	
	/**
	 * Generate all the constraints of execution list
	 * @return List of constraints
	 */
	protected List<Constraint> genConstraints() {
		List<Constraint> constraints = new ArrayList<>();
		for (TraceNode node : this.executionList) {
			constraints.addAll(this.genVarToStatConstraints(node));
		}
		
		return constraints;
	}
	/**
	 * Generate constraints for given trace node
	 * @param node Target TraceNode
	 * @return List of constraints
	 */
	protected List<Constraint> genConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		constraints.addAll(this.genVarToStatConstraints(node));
//		constraints.addAll(this.genStructureConstraints(node));
//		constraints.addAll(this.genNamingConstraints(node));
		constraints.addAll(this.genPriorConstraints(node));
		return constraints;
	}
	
	/**
	 * Generate variable to statement constraint of given node
	 * @param node Target trace node
	 * @return List of constraints
	 */
	protected List<Constraint> genVarToStatConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		final int readLen = this.countReadVars(node);
		final int writeLen = this.countWriteVars(node);
		final int totalLen = this.countPredicates(node);
		
		// Check control dominator exist or not
		TraceNode controlDominator = node.getControlDominator();
		boolean haveControlDominator = controlDominator != null;

		// Index of statement in bit representation
		final int conclusionIdx = totalLen - 1;
		
		// Constraint A1, A2, A3 include the same variable
		BitRepresentation variableIncluded = new BitRepresentation(totalLen);
		variableIncluded.set(0, readLen + writeLen);	// Include all read and write variables
		if (haveControlDominator) {
			variableIncluded.set(totalLen - 2);	// Include control dominator if exist
		}
		variableIncluded.set(conclusionIdx);	// Include conclusion statement
		
		// Index of starting write variable in the bit representation
		final int writeStartIdx = readLen == 0 ? 0 : readLen;
		
		// Order of current node
		final int statementOrder = node.getOrder();
		
		// Get the ID of control dominator variable if exists
		String controlDomID = "";
		if (haveControlDominator) {
			VarValue controlDomValue = this.getControlDomValue(controlDominator);
			controlDomID = controlDomValue.getVarID();
		}
		
		/*
		 * Consider the following special cases
		 * 1. Trace Node only have written variable
		 * 2. Trace Node only have read variable
		 * 3. Trace Node only have control dominator
		 * 
		 * Then the statement correctness will only
		 * depends on the predicate it has (only have
		 * one constraint)
		 * 
		 * Note that the case that node does not have anything
		 * does not exist, because this kind of node will not
		 * be included during the dynamic slicing
		 */
		if ((readLen == 0 && writeLen == 0) || // 3rd case
			(readLen == 0 && writeLen != 0) || // 1st case
			(readLen != 0 && writeLen == 0)) { // 2nd case
			
			Constraint constraintA1 = new StatementConstraintA1(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA1.setVarsID(node);
			constraints.add(constraintA1);
		} else {
			
			// Variable to statement constraint A1
			Constraint constraintA1 = new StatementConstraintA1(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA1.setVarsID(node);
			constraints.add(constraintA1);
			
			// Variable to statement constraint A2
			Constraint constraintA2 = new StatementConstraintA2(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA2.setVarsID(node);
			constraints.add(constraintA2);
			
			// Variable to statement constraint A3
			Constraint constraintA3 = new StatementConstraintA3(variableIncluded, conclusionIdx, Configs.HIGH, writeStartIdx, statementOrder, controlDomID);
			constraintA3.setVarsID(node);
			constraints.add(constraintA3);
		}

		// Variable to statement constraint A
		return constraints;
	}
	
	/**
	 * Generate prior constraints of given node
	 * @param node Target trace node
	 * @return List of constraints
	 */
	protected List<Constraint> genPriorConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		final int readLen = this.countReadVars(node);
		final int writeLen = this.countWriteVars(node);
		final int totalLen = this.countPredicates(node);
		
		TraceNode controlDominator = node.getControlDominator();
		
		// Index of control dominator in bit representation
		final int predIdx = controlDominator != null ? totalLen - 2 : -1;
		
		// Handle read variables prior constraints
		for (int i=0; i<readLen; i++) {
			VarValue readVar = node.getReadVariables().get(i);
			BitRepresentation br = new BitRepresentation(totalLen);
			br.set(i);
			Constraint constraint = new PriorConstraint(br, i, readVar.getProbability());
			constraints.add(constraint);
		}
		
		// Handle write variables prior constraints
		for (int i = 0; i<writeLen; ++i) {
			VarValue writeVar = node.getWrittenVariables().get(i);
			final int pos = i+readLen;
			BitRepresentation br = new BitRepresentation(totalLen);
			br.set(pos);
			Constraint constraint = new PriorConstraint(br, pos, writeVar.getProbability());
			constraints.add(constraint);
		}
		
		// Handle control dominator constraint
		if (controlDominator != null) {
			BitRepresentation bitRep = new BitRepresentation(totalLen);
			bitRep.set(predIdx);
			VarValue controlDomValue = this.getControlDomValue(controlDominator);
			Constraint constarint = new PriorConstraint(bitRep, predIdx, controlDomValue.getProbability());
			constraints.add(constarint);
		}

		return constraints;
	}
	
//	private void printTable(List<Constraint> constraints, int totalLen) {
//		final int size = 1 << totalLen;
//		for (int i = 0; i<size; ++i) {
//			BitRepresentation bitRep = BitRepresentation.parse(i, totalLen);
//			System.out.print(bitRep + "\t");
//			for (Constraint constraint : constraints) {
//				System.out.print(String.format("%.4f", constraint.getProbability(i)) + "\t");
//			}
//			System.out.println();
//		}
//	}
	
//	private List<Constraint> genStructureConstraints(TraceNode node) {
//		List<Constraint> constraints = new ArrayList<>();
//		
//		int readLen = node.getReadVariables().size();
//		int writeLen = node.getWrittenVariables().size();
//		
//		// Also include structure factor, naming factor and the statement conclusion
//		int totalLen = readLen + writeLen + 3;
//		
//		// Check control dominator exist or not
//		TraceNode controlDominator = node.getControlDominator();
//		boolean haveControlDominator = controlDominator != null;
//		if (haveControlDominator) {
//			totalLen++;
//		}
//		
//		// Define which variable is included
//		BitRepresentation variableIncluded = new BitRepresentation(totalLen);
//		final int structureIdx = totalLen - 3;
//		final int conclusionIdx = totalLen - 1;
//		variableIncluded.set(structureIdx);
//		variableIncluded.set(conclusionIdx);
//		
//		// Create forward and backward constraint
////		Constraint constraint_forward = new StatementConstraint(variableIncluded, conclusionIdx, Configs.HIGH, ConstraintType.PROG_STRUCTURE, readLen - 1);
////		Constraint constraint_backward = new StatementConstraint(variableIncluded, structureIdx, Configs.HIGH, ConstraintType.PROG_STRUCTURE, readLen - 1);
////		constraints.add(constraint_forward);
////		constraints.add(constraint_backward);
//		
//		// Create prior constraint
//		final double prob = this.calStructureProb(node);
//		BitRepresentation varBit = new BitRepresentation(totalLen);
//		varBit.set(structureIdx);
////		Constraint priorConstraint = new StatementConstraint(varBit, structureIdx, prob, ConstraintType.PRIOR, readLen - 1);
////		constraints.add(priorConstraint);
//		
//		return constraints;
//	}
	
//	private List<Constraint> genNamingConstraints(TraceNode node) {
//		List<Constraint> constraints = new ArrayList<>();
//		return constraints;
//	}
	

	
//	private void populateCount() {
//		for (TraceNode tn : trace.getExecutionList()) {
//			BreakPoint bp = tn.getBreakPoint();
//			TraceNode next = tn.getStepOverNext();
//			// if they return to the same breakpoint, it means they are the same line
//			if (next != null && bp.equals(next.getBreakPoint()))
//				continue;
//			if (!instOccurence.containsKey(bp))
//				instOccurence.put(bp, 1);
//			else
//				instOccurence.put(bp, instOccurence.get(bp) + 1);
//		}
//		
//		for (TraceNode tn : slice) {
//			BreakPoint bp = tn.getBreakPoint();
//			TraceNode next = tn.getStepOverNext();
//			// if they return to the same breakpoint, it means they are the same line
//			if (next != null && bp.equals(next.getBreakPoint()) && slice.contains(next))
//				continue;
//			if (!instInSlice.containsKey(bp))
//				instInSlice.put(bp, 1);
//			else
//				instInSlice.put(bp, instInSlice.get(bp) + 1);
//			
//			String key = bp.getMethodSign();
//			if (!sliceInFunc.containsKey(key))
//				sliceInFunc.put(key, 1);
//			else
//				sliceInFunc.put(key, sliceInFunc.get(key) + 1);
//		}
//		
//		for (int value : instInSlice.values())
//			this.sliceSize += value;
//		this.populated = true;
//	}
//	
//	private double calStructureProb(TraceNode node) {
//		if (!populated)
//			populateCount();
//		BreakPoint bp = node.getBreakPoint();
//		String key = bp.getMethodSign();
//		double prop1 = (double) sliceInFunc.getOrDefault(key, 0) / this.sliceSize;
//		int allInstOccurence = instOccurence.getOrDefault(bp, 0);
//		int instsNotInSlice = allInstOccurence - instInSlice.getOrDefault(bp, 0);
//		double prop2 = (double) instsNotInSlice / allInstOccurence;
//		return 0.5 * (sigma1(prop1) + sigma2(prop2));
//	}
//	
//	private double nameConstraint(TraceNode node) {
//		double score = 0.0;
//		for (VarValue v : node.getWrittenVariables()) {
//			String varName = v.getVarName();
//			for (String fVarName : this.faultyVars) {
//				score = Math.max(editDistance(varName, fVarName), score);
//			}
//		}
//		return score;
//	}
//	
//	private double varToStatementConstraint(TraceNode tn) {
//		boolean isReadCorrect = true;
//		for (VarValue v : tn.getReadVariables()) {
//			if (v.getProbability() < 0.3) {
//				isReadCorrect = false;
//				break;
//			}
//		}
//		
//		boolean isWrittenCorrect = true;
//		for (VarValue v : tn.getWrittenVariables()) {
//			if (v.getProbability() < 0.3) {
//				isWrittenCorrect = false;
//				break;
//			}
//		}
//		
//		TraceNode controlDominator = tn.getControlDominator();
//		if (controlDominator != null) {
//			if (controlDominator.getPredProb() < 0.3) {
//				return Configs.LOW;
//			}
//		}
//		
//		
//		if (isReadCorrect && isWrittenCorrect) {
//			// all read and written variables are correct
//			return Configs.HIGH;
//		} else if (isReadCorrect && !isWrittenCorrect) {
//			// all read variables are correct but at least one written var is wrong
//			return Configs.LOW;
//		} else if (isWrittenCorrect && !isReadCorrect){
//			// all written var is correct but one read variable is wrong 
//			return Configs.HIGH; // TODO: how to allocate this probability
//		} else {
//			// at least one written and read var is wrong
//			return Configs.HIGH;
//		}
//	}
//	
//	private double sigma1(double x) {
//		return 0.5 - 0.5 * (2 * Configs.HIGH - 1) * x;
//	}
//	
//	private double sigma2(double x) {
//		return 0.5 + 0.5 * (2 * Configs.HIGH - 1) * x;
//	}
//	
//	private static double editDistance(String s1, String s2) {
//        /* implements OSA Damerau-Levenshtein distance */
//        // add padding in front
//        s1 = " " + s1;
//        s2 = " " + s2;
//
//        char[] a1 = s1.toCharArray();
//        char[] a2 = s2.toCharArray();
//
//        // initialize the array
//        int[][] distanceMatrix = new int[a1.length][a2.length];
//        for (int i = 0; i < distanceMatrix.length; i++)
//            distanceMatrix[i][0] = i;
//        for (int i = 0; i < distanceMatrix[0].length; i++)
//            distanceMatrix[0][i] = i;
//
//        for (int i = 1; i < distanceMatrix.length; i++) {
//            for (int j = 1; j < distanceMatrix[0].length; j++) {
//                int cost = a1[i] == a2[j] ? 0 : 1;
//                distanceMatrix[i][j] = minimum(distanceMatrix[i-1][j] + 1, 
//                                                  distanceMatrix[i][j-1] + 1,
//                                                  distanceMatrix[i-1][j-1] + cost);
//                if (i > 1 && j > 1 && a1[i] == a2[j - 1] && a1[i -1] == a2[j]) {
//                    distanceMatrix[i][j] = minimum(distanceMatrix[i][j],
//                                                   distanceMatrix[i-2][j-2] + 1);
//                }
//            }
//        }
//        int editDistance = distanceMatrix[s1.length() - 1][s2.length() - 1];
//        double lenSum = (double) (s1.length() - 1 + s2.length() - 1);
//        double similarity =  lenSum - editDistance;
//        return similarity / lenSum;
//    }
//	
//    private static int minimum (int a, int... b) {
//        int min = a;
//        for (int i : b) {
//            min = min < i ? min : i;
//        }
//        return min;
//    }
}
