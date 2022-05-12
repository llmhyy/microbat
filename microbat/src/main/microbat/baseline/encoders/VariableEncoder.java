package microbat.baseline.encoders;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.DREM;
import org.apache.bcel.generic.FREM;
import org.apache.bcel.generic.IOR;
import org.apache.bcel.generic.IREM;
import org.apache.bcel.generic.ISHR;
import org.apache.bcel.generic.IUSHR;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LREM;
import org.apache.bcel.generic.LSHR;
import org.apache.bcel.generic.LUSHR;
import org.apache.bcel.generic.LoadInstruction;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.ConstraintType;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;

/*
 * First phase of the encoding.
 * At this phase, we will assume that the statements are correct
 * and only evaluate the variables in each statement
 */

public class VariableEncoder {
	private List<TraceNode> executionList;
	private Trace trace;
	
	public VariableEncoder(Trace trace, List<TraceNode> executionList) {
		this.executionList = executionList;
		this.trace = trace;
	}
	
	public boolean encode() {
		boolean hasChange = false;
		for (TraceNode tn : executionList) {
			// the order matters as Java short circuit evaluate
			hasChange = encode(tn) || hasChange;
		}
		return hasChange;
	}
	
	private boolean encode(TraceNode tn) {
		boolean hasChange = false;
//		System.out.println(tn.getOrder());
//		System.out.println(tn.getInstructions());

		int readLength = tn.getReadVariables().size();
		int writeLength = tn.getWrittenVariables().size();
		int totalVariableLength = readLength + writeLength;
		if (totalVariableLength > 30) {
			// TODO: might want to reconsider
			return hasChange; 
		}
		
		List<Constraint> constraints = getAstConstraints(tn);
		List<Constraint> priorConstraints = getPriorConstraints(tn);
		constraints.addAll(priorConstraints);
		int maxInt = 1 << totalVariableLength; 
		
		HashMap<Integer, Double> memoization = new HashMap<>();
		double denominator = 0;
		for (int i = 0; i < maxInt; i++) {
			double product = 1; 
			for (Constraint c: constraints) {
				product *= c.getProbability(i);
			}
			memoization.put(i, product);
			denominator += product;
		}
		
		for (int i = 0; i < totalVariableLength; i++) {
			List<Integer> numbers = getNumbers(totalVariableLength, i);
			VarValue var;
			if (i < readLength)
				var = tn.getReadVariables().get(i);
			else
				var = tn.getWrittenVariables().get(i - readLength);
			double sum = 0;
			for (int number : numbers) {
				sum += memoization.get(number);
			}
			double prob = sum / denominator;
			if (Math.abs(var.getProbability() - prob) > 0.01) {
				hasChange = true;
//				System.out.println("Prev prob: " + var.getProbability() + " | Current prob: " + prob);
			}
			var.setProbability(prob);
		}
		System.gc();
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
	
	/* 
	 * Get a list of constraints for a specific variable
	 */
	private List<Constraint> getAstConstraints(TraceNode tn) {
		List<Constraint> constraints = new ArrayList<>();
		
		int readLength = tn.getReadVariables().size();
		int writeLength = tn.getWrittenVariables().size();
		int totalLength = readLength + writeLength;
		
		/*
		 * For written variables (A1), it is always high
		 * unless the read variable are all true, while the
		 * written variable is wrong. We will assume that
		 * all written variables are independent from one another
		 */
		for (int i = 0; i < writeLength; i++){
			BitRepresentation variablesIncluded = new BitRepresentation(totalLength);
			variablesIncluded.set(0, readLength);
			variablesIncluded.set(i + readLength);
			Constraint constraint = new Constraint(variablesIncluded, i + readLength, Configs.HIGH);
			constraints.add(constraint);
		}
		
		/*
		 * We will handle all the read constraints here, which
		 * is more complicated than that of the written, and
		 * follows the rule of A2. We will use the ASTNode to
		 * derive which rule it is.
		 */
		System.out.println(tn.getOrder() + "(" + tn.getAstNode().getNodeType() + ")");
		System.out.println(tn.getReadMap());
		HashMap<String, Double> scoreTable = new HashMap<>();
		Stack<Double> probStack = new Stack<>();
		probStack.push(Configs.HIGH);
		getProbability(tn.getAstNode(), scoreTable, probStack);
		System.out.println(scoreTable);
		
		return constraints;
	}
	
	private void getProbability(ASTNode node, HashMap<String, Double> scoreTable, Stack<Double> probStack) {
		if (node == null)
			return;
		switch(node.getNodeType()) {
		case ASTNode.IF_STATEMENT:
			System.out.println(node);
			IfStatement ifNode = (IfStatement) node;
			getProbability(ifNode.getExpression(), scoreTable, probStack);
			break;
		case ASTNode.INFIX_EXPRESSION:
			InfixExpression infixExp = (InfixExpression) node;
			// cannot use switch here as it is not an enum
			if (infixExp.getOperator() == InfixExpression.Operator.EQUALS ||
					infixExp.getOperator() == InfixExpression.Operator.TIMES ||
					infixExp.getOperator() == InfixExpression.Operator.DIVIDE ||
					infixExp.getOperator() == InfixExpression.Operator.PLUS ||
					infixExp.getOperator() == InfixExpression.Operator.MINUS ||
					infixExp.getOperator() == InfixExpression.Operator.CONDITIONAL_AND) {
				
				probStack.push(Configs.HIGH);
			} else {
				probStack.push(Configs.UNCERTAIN);
			}
			getProbability(infixExp.getLeftOperand(), scoreTable, probStack);
			getProbability(infixExp.getRightOperand(), scoreTable, probStack);
			probStack.pop(); // remove what is added
			break;
		case ASTNode.METHOD_INVOCATION:
			// not handled at the moment
			break;
		case ASTNode.ARRAY_ACCESS:
			ArrayAccess aAccess = (ArrayAccess) node;
			// calculate tau using index
			Expression indexNode = aAccess.getIndex();
			System.out.println(indexNode);
			
			int index;
			switch(indexNode.getNodeType()) {
			case ASTNode.INFIX_EXPRESSION:
				break;
			case ASTNode.NUMBER_LITERAL:
				NumberLiteral nl = (NumberLiteral) indexNode;
				index = Integer.parseInt(nl.getToken());
				break;
			default:
				throw new IllegalArgumentException();
			}
			// add tau to stack
			// process array
			System.out.println(aAccess.getArray());
			
			break;
		case ASTNode.SIMPLE_NAME:
			// variables
			SimpleName var = (SimpleName) node;
			System.out.println(var.getFullyQualifiedName());
			scoreTable.put(var.getFullyQualifiedName(), probStack.peek());
			break;
		case ASTNode.NUMBER_LITERAL:
		case ASTNode.NULL_LITERAL:
		case ASTNode.CHARACTER_LITERAL:
			System.out.print("Skipping... ");
			System.out.println(node);
			break;
		default:
			break;
		}
	}
	
	private boolean isEqual(Expression a, Expression b, HashMap<String, String> varTable) {
		// compares whether two expressions are equal
		System.out.println(a.resolveConstantExpressionValue());
		System.out.println(b.resolveConstantExpressionValue());
		return false;
	}
	
	private List<Constraint> getPriorConstraints(TraceNode tn) {
		List<Constraint> constraints = new ArrayList<>();
		int readLength = tn.getReadVariables().size();
		int writeLength = tn.getWrittenVariables().size();
		int totalLength = readLength + writeLength;
		
		// Handle the probability passed down from the previous node
		for (int i = 0; i < readLength; i++) {
			VarValue v = tn.getReadVariables().get(i);
			TraceNode prev = trace.findDataDependency(tn, v);
			if (prev == null)
				continue;
			for (VarValue pv : prev.getWrittenVariables()) {
				// only handle if the probability is not uncertain
				if (pv.equals(v) && pv.getProbability() != Configs.UNCERTAIN) {
					BitRepresentation br = new BitRepresentation(totalLength);
					br.set(i);
					
					Constraint constraint = new Constraint(br, i, pv.getProbability());
					constraints.add(constraint);
					break;
				}
			}
		}
		
		for (int i = 0; i < writeLength ; i++) {
			VarValue v = tn.getWrittenVariables().get(i);
			int pos = i + readLength;
			List<TraceNode> nextNodes = trace.findDataDependentee(tn, v);
			if (nextNodes.size() == 0)
				continue;
			for (TraceNode nextNode : nextNodes) {
				for (VarValue nv : nextNode.getReadVariables()) {
					if (nv.equals(v) && nv.getProbability() != Configs.UNCERTAIN) {
						BitRepresentation br = new BitRepresentation(totalLength);
						br.set(pos);

						Constraint constraint = new Constraint(br, pos, nv.getProbability());
						constraints.add(constraint);
						break;
					}
				}
			}
		}
		
		return constraints;
	}
	
	private List<Constraint> getConstraints(TraceNode tn) {
		List<Constraint> constraints = new ArrayList<>();
		int readLength = tn.getReadVariables().size();
		int writeLength = tn.getWrittenVariables().size();
		int totalVariableLength = readLength + writeLength;
		/*
		 * For written variables (A1):
		 * It is always high unless the read variables are
		 * all right but it is wrong. We will assume that
		 * all written variables have the same probability
		 */
		for (int i = 0; i < writeLength; i++) {
			/* A write variable will use all the read variables but not any
			 * of the other write variables
			 * TODO: Handle cases where it is int x = (y++) + z;
			 */
			BitRepresentation variablesIncluded = new BitRepresentation(totalVariableLength);
			variablesIncluded.set(0, readLength);
			variablesIncluded.set(i + readLength);
			Constraint constraint = new Constraint(variablesIncluded, i + readLength, Configs.HIGH);
			constraints.add(constraint);
		}
		
		Stack<String> runtimeStack = new Stack<>();
		String[] localVarStack = tn.getStackVariables();
		HashMap<String, ConstraintType> variableMapping = new HashMap<>();
		
		for (InstructionHandle ih: tn.getInstructions()) {
			/*
			 * We will find the most "impactful" instruction here for each variable
			 */
			Instruction instruction = ih.getInstruction();
			if (instruction instanceof LoadInstruction) {
				LoadInstruction li = (LoadInstruction) instruction;
				int varIndex = li.getIndex();
				if (varIndex < localVarStack.length) {
					runtimeStack.push(localVarStack[varIndex]);
				} else {
					/* TODO: For now, there are some variables that
					 * are missing from the stack. We will need to see
					 * why that is the case and fix it. We will assume
					 * that it is a null variable for now
					 */
					runtimeStack.push(null);
				}
			} else if (instruction instanceof DREM || instruction instanceof FREM ||
					instruction instanceof IREM || instruction instanceof LREM ||
					instruction instanceof IOR || instruction instanceof ISHR ||
					instruction instanceof IUSHR || instruction instanceof LSHR ||
					instruction instanceof LUSHR) {
				String var1 = runtimeStack.pop();
				String var2 = runtimeStack.pop();
				variableMapping.put(var1, ConstraintType.MOD);
				variableMapping.put(var2, ConstraintType.MOD);
			} else if (instruction instanceof ArrayInstruction && 
					instruction.getClass().getSimpleName().toUpperCase().contains("LOAD")) {
				String index = runtimeStack.pop();
				String var = runtimeStack.pop();
				variableMapping.put(var, ConstraintType.REFERENCE);
			}
		}
		
		
		/*
		 * For read variables, it is more complicated depending on the
		 * computation rules mentioned in the second section
		 */
		for (int i = 0; i < readLength; i++) {
			/*
			 * A use variable will affect all the write variables
			 * TODO: Should we classify them as individual constraints or 
			 * one big constraint?
			 */
			BitRepresentation variablesIncluded = new BitRepresentation(totalVariableLength);
			// all variables are involved for use statement
			variablesIncluded.set(0, totalVariableLength);
			
			// Get the constraint type based on the mapping done previously
			VarValue var = tn.getReadVariables().get(i);
			Constraint constraint;
			if (variableMapping.containsKey(var.getVarName())) {
				ConstraintType ct = variableMapping.get(var.getVarName());
				switch (ct) {
				case REFERENCE:
					// TODO: Store the number of variables with same field
					int n = 1;
					double probability = Constraint.tau(n);
					constraint = new Constraint(variablesIncluded, i, probability);
					break;
				case MOD:
					constraint = new Constraint(variablesIncluded, i, 0.5);
					break;
				default:
					constraint = new Constraint(variablesIncluded, i, Configs.HIGH);
				}
			} else {
				constraint = new Constraint(variablesIncluded, i, Configs.HIGH);
			}
			constraints.add(constraint);
		}
		return constraints;
	}
}
