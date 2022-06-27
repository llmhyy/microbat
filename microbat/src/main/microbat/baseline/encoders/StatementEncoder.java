package microbat.baseline.encoders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.ConstraintType;
import microbat.baseline.constraints.StatementConstraint;
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
		// double prob = calStructureProb(tn) * varToStatementConstraint(tn) * Math.log10((1+ nameConstraint(tn)) * 10);
//		double prob = varToStatementConstraint(tn);
		int totalLen = tn.getReadVariables().size() + tn.getWrittenVariables().size() + 1;
		if (tn.getControlDominator() != null) {
			totalLen += 1;
		}
		
		if (totalLen > 30) {
			return hasChange;
		}
		
		List<Constraint> constraints = this.genConstraints(tn);
		int maxInt = 1 << totalLen;
		
		HashMap<Integer, Double> memoization = new HashMap<>();
		double denominator = 0;
		for (int i=0; i<maxInt; ++i) {
			double product = 1;
			for (Constraint constraint : constraints) {
				if (tn.getOrder() == 9) {
					System.out.println(constraint);
					System.out.println(constraint.getProbability(i));
				}
				product *= constraint.getProbability(i);
			}
		
			memoization.put(i, product);
			denominator += product;
		}
		
		double sum = 0;
		for (int i : this.getNumbers(totalLen, totalLen-1)) {
			sum += memoization.get(i);
		}
		double prob = sum / denominator;
		
		if (Math.abs(tn.getProbability() - prob) > 0.01) {
			hasChange = true;
		}

		tn.setProbability(prob);
		return hasChange;
	}
	
	private List<Integer> getNumbers(int length, int bitPos) {
		/*
		 * bitPos is 0-indexed
		 */
		List<Integer> result = new ArrayList<>(1 << length);
		int numBitLeft = bitPos;
		int numBitRight = length - (numBitLeft + 1);
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
	
	private List<Constraint> genConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		constraints.addAll(this.genVarToStatConstraints(node));
//		constraints.addAll(this.genStructureConstraints(node));
//		constraints.addAll(this.genNamingConstraints(node));
		constraints.addAll(this.genPriorConstraints(node));
		return constraints;
	}
	
	private List<Constraint> genVarToStatConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		int readLen = node.getReadVariables().size();
		int writeLen = node.getWrittenVariables().size();
		
		// Also include structure factor, naming factor and the statement conclusion
		// Also include statement conclusion
		int totalLen = readLen + writeLen + 1;
		
		// Check control dominator exist or not
		TraceNode controlDominator = node.getControlDominator();
		boolean haveControlDominator = controlDominator != null;
		if (haveControlDominator) {
			totalLen++;
		}
		
		final int conclusionIdx = totalLen - 1;
		
		// Constraint A1, A2, A3 include the same variable
		BitRepresentation variableIncluded = new BitRepresentation(totalLen);
		variableIncluded.set(0, readLen + writeLen);	// Include all read and write variables
		if (haveControlDominator) {
			variableIncluded.set(totalLen - 2);	// Include control dominator if exist
		}
		variableIncluded.set(conclusionIdx);	// Include conclusion statement
		
		final int writeStartIdx = readLen == 0 ? 0 : readLen - 1;
		// Variable to statement constraint A1
		Constraint constraintA1 = new StatementConstraint(variableIncluded, conclusionIdx, Configs.HIGH, ConstraintType.VAR_TO_STAT_1, writeStartIdx);
		constraints.add(constraintA1);
		
		// Variable to statement constraint A2
		Constraint constraintA2 = new StatementConstraint(variableIncluded, conclusionIdx, Configs.HIGH, ConstraintType.VAR_TO_STAT_2, writeStartIdx);
		constraints.add(constraintA2);
		
		// Variable to statement constraint A3
		Constraint constraintA3 = new StatementConstraint(variableIncluded, conclusionIdx, Configs.HIGH, ConstraintType.VAR_TO_STAT_3, writeStartIdx);
		constraints.add(constraintA3);
		
		// Variable to statement constraint A
		return constraints;
	}
	
	private List<Constraint> genStructureConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		int readLen = node.getReadVariables().size();
		int writeLen = node.getWrittenVariables().size();
		
		// Also include structure factor, naming factor and the statement conclusion
		int totalLen = readLen + writeLen + 3;
		
		// Check control dominator exist or not
		TraceNode controlDominator = node.getControlDominator();
		boolean haveControlDominator = controlDominator != null;
		if (haveControlDominator) {
			totalLen++;
		}
		
		// Define which variable is included
		BitRepresentation variableIncluded = new BitRepresentation(totalLen);
		final int structureIdx = totalLen - 3;
		final int conclusionIdx = totalLen - 1;
		variableIncluded.set(structureIdx);
		variableIncluded.set(conclusionIdx);
		
		// Create forward and backward constraint
		Constraint constraint_forward = new StatementConstraint(variableIncluded, conclusionIdx, Configs.HIGH, ConstraintType.PROG_STRUCTURE, readLen - 1);
		Constraint constraint_backward = new StatementConstraint(variableIncluded, structureIdx, Configs.HIGH, ConstraintType.PROG_STRUCTURE, readLen - 1);
		constraints.add(constraint_forward);
		constraints.add(constraint_backward);
		
		// Create prior constraint
		final double prob = this.calStructureProb(node);
		BitRepresentation varBit = new BitRepresentation(totalLen);
		varBit.set(structureIdx);
		Constraint priorConstraint = new StatementConstraint(varBit, structureIdx, prob, ConstraintType.PRIOR, readLen - 1);
		constraints.add(priorConstraint);
		
		return constraints;
	}
	
	private List<Constraint> genNamingConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		return constraints;
	}
	
	private List<Constraint> genPriorConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		final int readLen = node.getReadVariables().size();
		final int writeLen = node.getWrittenVariables().size();
		
		final int writeStartIdx = readLen == 0 ? 0 : readLen - 1;
		
		// Also include the conclusion statement
		int totalLen = readLen + writeLen + 1;
		
		// Handle pred constraint
		TraceNode controlDominator = node.getControlDominator();
		if (controlDominator != null) {
			totalLen++;
			
			BitRepresentation bitRep = new BitRepresentation(totalLen);
			final int predIdx = totalLen - 2;
			bitRep.set(predIdx);
			Constraint constarint = new StatementConstraint(bitRep, predIdx, controlDominator.getPredProb(), ConstraintType.PRIOR, writeStartIdx);
			constraints.add(constarint);
		}
		
		// Handle read variables prior constraints
		for (int i=0; i<readLen; i++) {
			VarValue readVar = node.getReadVariables().get(i);
			TraceNode dataDominator = trace.findDataDependency(node, readVar);
			if (dataDominator == null) {
				continue;
			}
			
			for (VarValue prevVar : dataDominator.getWrittenVariables()) {
				// Only handle if the probability is not uncertain
				if (prevVar.equals(readVar) && prevVar.getProbability() != Configs.UNCERTAIN) {
					BitRepresentation br = new BitRepresentation(totalLen);
					br.set(i);
					Constraint constraint = new StatementConstraint(br, i, prevVar.getProbability(), ConstraintType.PRIOR, writeStartIdx);
					constraints.add(constraint);
					break;
				}
			}
		}
		
		// Handle write variables prior constraints
		for (int i = 0; i<writeLen; ++i) {
			VarValue writeVar = node.getWrittenVariables().get(i);
			int pos = i+readLen;
			List<TraceNode> dataDependentees = trace.findDataDependentee(node, writeVar);
			if (dataDependentees.size() == 0) {
				continue;
			}
			
			for (TraceNode nextNode : dataDependentees) {
				for (VarValue nextVar : nextNode.getReadVariables()) {
					if (nextVar.equals(writeVar) && nextVar.getProbability() != Configs.UNCERTAIN) {
						BitRepresentation br = new BitRepresentation(totalLen);
						br.set(pos);
						
						Constraint constraint = new StatementConstraint(br, pos, nextVar.getProbability(), ConstraintType.PRIOR, writeStartIdx);
						constraints.add(constraint);
						break;
					}
				}
			}
		}
		return constraints;
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
	
	private double calStructureProb(TraceNode node) {
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
		
		TraceNode controlDominator = tn.getControlDominator();
		if (controlDominator != null) {
			if (controlDominator.getPredProb() < 0.3) {
				return Configs.LOW;
			}
		}
		
		
		if (isReadCorrect && isWrittenCorrect) {
			// all read and written variables are correct
			return Configs.HIGH;
		} else if (isReadCorrect && !isWrittenCorrect) {
			// all read variables are correct but at least one written var is wrong
			return Configs.LOW;
		} else if (isWrittenCorrect && !isReadCorrect){
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
