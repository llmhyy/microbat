package microbat.baseline.encoders;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import microbat.baseline.BitRepresentation;
import microbat.baseline.Configs;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.PriorConstraint;
import microbat.baseline.constraints.VariableConstraint;
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
	
	private boolean encode(TraceNode node) {
		boolean hasChange = false;
		
		// Skip the inefficient trace node
		if (node.getCodeStatement().equals("}")) {
			return hasChange;
		}
		
		int readLength = node.getReadVariables().size();
		int writeLength = node.getWrittenVariables().size();
		int totalVariableLength = readLength + writeLength;
		
		TraceNode controlDominator = node.getControlDominator();
		boolean haveControlDominator = controlDominator != null;
		if (haveControlDominator) {
			totalVariableLength += 1;
		}
		
		// skip when there are more than 30 variables
		if (totalVariableLength > 30) {
			return hasChange; 
		}
		
		// Generate constraints for target trace node: A1, A2, A3
		List<Constraint> constraints = this.genConstraints(node);
		
		InferenceModel model = new InferenceModel(constraints);
		for (int i=0; i<totalVariableLength; ++i) {
			VarValue var = null;
			if (i<readLength) {
				// Update read variables probability
				var = node.getReadVariables().get(i);
			} else if (i < readLength + writeLength) {
				// Update write variables probability
				var = node.getWrittenVariables().get(i-readLength);
			}
			
			// We do not update control dominator probability
			if (var == null) {
				continue;
			}
			
			double prob = model.getProbability(i);
			if (prob != -1) {
				if (Math.abs(var.getProbability() - prob) > 0.01) {
					hasChange = true;
				}
				var.setProbability(prob);
			} else {
				System.out.println("TraceNode: " + node.getOrder() + " don't have constraints or have invalid constraints");
			}
		}
		
		System.gc();
		return hasChange;
		
		
//		int maxInt = 1 << totalVariableLength; 
//		
//		HashMap<Integer, Double> memoization = new HashMap<>();
//		double denominator = 0;
//		for (int i = 0; i < maxInt; i++) {
//			double product = 1; 
//			for (Constraint c: constraints) {
//				product *= c.getProbability(i);
//			}
//			memoization.put(i, product);
//			denominator += product;
//		}
//		
//		for (int i = 0; i < totalVariableLength; i++) {
//			List<Integer> numbers = getNumbers(totalVariableLength, i);
//			
//			if (i < readLength + writeLength) {
//				// Update variable probability
//				VarValue var;
//				if (i < readLength)
//					var = node.getReadVariables().get(i);
//				else
//					var = node.getWrittenVariables().get(i - readLength);
//				double sum = 0;
//				for (int number : numbers) {
//					sum += memoization.get(number);
//				}
//				double prob = sum / denominator;
//				if (Math.abs(var.getProbability() - prob) > 0.01) {
//					hasChange = true;
//				}
//				var.setProbability(prob);
//			} else {
//				// Update predicate probability
////				double sum = 0;
////				for (int number : numbers) {
////					sum += memoization.get(number);
////				}
////				double prob = sum / denominator;
////				if (Math.abs(controlDominator.getPredProb() - prob) > 0.01) {
////					hasChange = true;
////				}
////				controlDominator.setPredProb(prob);
//			}
//		}
	}

	/**
	 * Generate all constraints for given trace node
	 * @param node Target trace node to generate constraints
	 * @return All constraints that based on given trace node
	 */
	private List<Constraint> genConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		constraints.addAll(this.genVarConstraints(node));
		constraints.addAll(this.genPriorConstraints(node));
		return constraints;
	}
	
	private List<Constraint> genVarConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		
		final int readLen = node.getReadVariables().size();
		final int writeLen = node.getWrittenVariables().size();
		
		boolean haveControlDom = node.getControlDominator() != null;
		final int totalLen = haveControlDom ? readLen + writeLen : readLen + writeLen + 1;
		
		// Index of control dominator is set to be the last bit if exists. Otherwise, it is set to be -1
		final int predIdx = haveControlDom ? totalLen - 1 : -1;
		
		/*
		 * For written variables (A1), it is always high
		 * unless the read variable are all true, while the
		 * written variable is wrong. We will assume that
		 * all written variables are independent from one another
		 */
		for (int idx=0; idx<writeLen; idx++) {
			final int writeIdx = idx + readLen;	// Index of target write variable
			
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0, readLen);		// All read variables are included
			varsIncluded.set(writeIdx);			// Include one particular write variable
			
			if (haveControlDom) {
				varsIncluded.set(predIdx);		// Include control dominator
			}
			
			Constraint constraint = new VariableConstraint(varsIncluded, writeIdx, Configs.HIGH);
			constraints.add(constraint);
		}
		
		/*
		 * For read variable (A2), the only invalid case is that,
		 * all the variable and control dominator except for that
		 * target read variable is correct, but the target read
		 * variable is wrong.
		 * 
		 * The propagation probability depends on the statement
		 */
		
		for (int readIdx=0; readIdx<readLen; readIdx++) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0, totalLen); 		// Include all variable and control dominator

		}
		
		/*
		 * For control dominator (A3), the only invalid case is that,
		 * all the write and read variable are correct but the control
		 * dominator is not correct
		 */
		if (haveControlDom) {
			BitRepresentation varsIncluded = new BitRepresentation(totalLen);
			varsIncluded.set(0, totalLen);		// Include all variable and control dominator
			Constraint constraint = new VariableConstraint(varsIncluded, predIdx, Configs.HIGH);
			constraints.add(constraint);
		}
		
		return constraints;
	}
	
	private List<Constraint> genPriorConstraints(TraceNode node) {
		List<Constraint> constraints = new ArrayList<>();
		int readLength = node.getReadVariables().size();
		int writeLength = node.getWrittenVariables().size();
		int totalLength = readLength + writeLength;
		
		// Predicate prior constraint
		TraceNode controlDominator = node.getControlDominator();
		if (controlDominator != null) {
			totalLength += 1;
		}
		
		// Handle the probability passed down from the previous node
		for (int i = 0; i < readLength; i++) {
			VarValue readVar = node.getReadVariables().get(i);
			TraceNode dataDominator = trace.findDataDependency(node, readVar);
			if (dataDominator == null)
				continue;
			for (VarValue prevVar : dataDominator.getWrittenVariables()) {
				if (prevVar.equals(readVar)) {
					BitRepresentation br = new BitRepresentation(totalLength);
					br.set(i);
					
					Constraint constraint = new PriorConstraint(br, i, prevVar.getProbability());
					constraints.add(constraint);
					break;
				}
			}
		}
		
		for (int i = 0; i < writeLength ; i++) {
			VarValue writeVar = node.getWrittenVariables().get(i);
			int pos = i + readLength;
			List<TraceNode> dataDependentees = trace.findDataDependentee(node, writeVar);
			if (dataDependentees.size() == 0)
				continue;
			for (TraceNode nextNode : dataDependentees) {
				for (VarValue nextVar : nextNode.getReadVariables()) {
					if (nextVar.equals(writeVar)) {
						BitRepresentation br = new BitRepresentation(totalLength);
						br.set(pos);

						Constraint constraint = new PriorConstraint(br, pos, nextVar.getProbability());
						constraints.add(constraint);
						break;
					}
				}
			}
		}
		
		if (controlDominator != null) {
			BitRepresentation bitRep = new BitRepresentation(totalLength);
			int index = readLength + writeLength;
			bitRep.set(index);
			
			Constraint constraint = new PriorConstraint(bitRep, index, controlDominator.getProbability());
			constraints.add(constraint);
		}
		
		return constraints;
	}
	
//	/**
//	 * Get all the constraints (A1, A2, A3) for given trace node
//	 * @param node Target trace node
//	 * @return List of correspondence constraints
//	 */
//	private List<Constraint> getAstConstraints(TraceNode node) {
//		
//		List<Constraint> constraints = new ArrayList<>();
//		
//		int readLength = node.getReadVariables().size();
//		int writeLength = node.getWrittenVariables().size();
//		int totalLength = readLength + writeLength;
//		
//		TraceNode controlDominator = node.getControlDominator();
//		boolean haveControlDominator = controlDominator != null;
//		
//		if (haveControlDominator) {
//			System.out.println("Have control Dominator");
//			totalLength += 1;
//		}
//		
//		/*
//		 * For written variables (A1), it is always high
//		 * unless the read variable are all true, while the
//		 * written variable is wrong. We will assume that
//		 * all written variables are independent from one another
//		 */
//		for (int i = 0; i < writeLength; i++){
//			BitRepresentation variablesIncluded = new BitRepresentation(totalLength);
//			variablesIncluded.set(0, readLength);
//			variablesIncluded.set(i + readLength);
//			if (haveControlDominator) {
//				variablesIncluded.set(readLength + writeLength);
//			}
//			
//			Constraint constraint = new Constraint(variablesIncluded, i + readLength, Configs.HIGH, ConstraintType.DEFINE);
//			constraints.add(constraint);
//		}
//		
//		/*
//		 * We will handle all the read constraints here, which
//		 * is more complicated than that of the written, and
//		 * follows the rule of A2. We will use the ASTNode to
//		 * derive which rule it is.
//		 */
//		PropagationCalculator calculator = new PropagationCalculator();
//		ASTNode astNode = node.getAstNode();
//		int readVarIdx = 0;
//		for (VarValue readVar : node.getReadVariables()) {
//			double propProb = calculator.calProb(astNode, readVar);
//			BitRepresentation variableIncluded = new BitRepresentation(totalLength);
//			variableIncluded.set(0 + totalLength - 1);
//			Constraint constraint = new Constraint(variableIncluded, readVarIdx, propProb, ConstraintType.USE);
//			constraints.add(constraint);
//			readVarIdx++;
//		}
		
//		HashMap<String, Double> scoreTable = new HashMap<>();
//		Stack<Double> probStack = new Stack<>();
//		probStack.push(Configs.HIGH);
//		getProbability(node.getAstNode(), scoreTable, probStack);

//		System.out.println(scoreTable);
		
		// Get A3 constraints if the given node have control dominator
//		if (haveControlDominator) {
//			BitRepresentation variablesIncluded = new BitRepresentation(totalLength);
//			variablesIncluded.set(0, totalLength);
//			Constraint constraint = new Constraint(variablesIncluded, readLength + writeLength, Configs.HIGH, ConstraintType.PREDICATE);
//			constraints.add(constraint);
//		}
//		return constraints;
//	}
	
//	private void getProbability(ASTNode node, HashMap<String, Double> scoreTable, Stack<Double> probStack) {
//		if (node == null)
//			return;
//		switch(node.getNodeType()) {
//		case ASTNode.IF_STATEMENT:
//			// System.out.println(node);
//			IfStatement ifNode = (IfStatement) node;
//			getProbability(ifNode.getExpression(), scoreTable, probStack);
//			break;
//		case ASTNode.INFIX_EXPRESSION:
//			// infix expression is the operation between two operands: A+B
//			InfixExpression infixExp = (InfixExpression) node;
//			// cannot use switch here as it is not an enum
//			if (infixExp.getOperator() == InfixExpression.Operator.EQUALS ||
//					infixExp.getOperator() == InfixExpression.Operator.TIMES ||
//					infixExp.getOperator() == InfixExpression.Operator.DIVIDE ||
//					infixExp.getOperator() == InfixExpression.Operator.PLUS ||
//					infixExp.getOperator() == InfixExpression.Operator.MINUS ||
//					infixExp.getOperator() == InfixExpression.Operator.CONDITIONAL_AND) {
//				
//				probStack.push(Configs.HIGH);
//			} else {
//				probStack.push(Configs.UNCERTAIN);
//			}
//			getProbability(infixExp.getLeftOperand(), scoreTable, probStack);
//			getProbability(infixExp.getRightOperand(), scoreTable, probStack);
//			probStack.pop(); // remove what is added
//			break;
//		case ASTNode.METHOD_INVOCATION:
//			// not handled at the moment
//			break;
//		case ASTNode.ARRAY_ACCESS:
//			ArrayAccess aAccess = (ArrayAccess) node;
//			// calculate tau using index
//			Expression indexNode = aAccess.getIndex();
//			// System.out.println(indexNode);
//			
//			int index;
//			switch(indexNode.getNodeType()) {
//			case ASTNode.INFIX_EXPRESSION:
//				break;
//			case ASTNode.NUMBER_LITERAL:
//				NumberLiteral nl = (NumberLiteral) indexNode;
//				index = Integer.parseInt(nl.getToken());
//				break;
//			default:
//				throw new IllegalArgumentException();
//			}
//			// add tau to stack
//			// process array
//			// System.out.println(aAccess.getArray());
//			
//			break;
//		case ASTNode.SIMPLE_NAME:
//			// variables
//			SimpleName var = (SimpleName) node;
//			// System.out.println(var.getFullyQualifiedName());
//			scoreTable.put(var.getFullyQualifiedName(), probStack.peek());
//			break;
//		case ASTNode.NUMBER_LITERAL:
//		case ASTNode.NULL_LITERAL:
//		case ASTNode.CHARACTER_LITERAL:
////			System.out.print("Skipping... ");
////			System.out.println(node);
//			break;
//		default:
//			break;
//		}
//	}
}
