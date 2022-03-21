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

import microbat.baseline.Configs;
import microbat.baseline.constraints.Constraint;
import microbat.baseline.constraints.ConstraintType;
import microbat.baseline.constraints.DefiniteConstraint;
import microbat.baseline.constraints.ModConstraint;
import microbat.baseline.constraints.ReferenceConstraints;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/*
 * First phase of the encoding.
 * At this phase, we will assume that the statements are correct
 * and only evaluate the variables in each statement
 */

public class VariableEncoder {
	
	private Trace trace;
	
	public VariableEncoder(Trace trace) {
		this.trace = trace;
	}
	
	public void encode() {
		for (TraceNode tn : trace.getExecutionList())
			encode(tn);
	}
	
	private void encode(TraceNode tn) {

	}
	
	private List<Constraint> getConstraints(TraceNode tn) {
		System.out.println(tn.getOrder());
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
			BitSet variablesIncluded = new BitSet(totalVariableLength);
			variablesIncluded.set(0, readLength);
			variablesIncluded.set(i + readLength);
			Constraint constraint = new DefiniteConstraint(variablesIncluded, i + readLength);
			constraints.add(constraint);
		}
		// TODO: Consider using stack
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
			BitSet variablesIncluded = new BitSet(totalVariableLength);
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
					constraint = new ReferenceConstraints(variablesIncluded, i, n);
					break;
				case MOD:
					constraint = new ModConstraint(variablesIncluded, i);
					break;
				default:
					constraint = new DefiniteConstraint(variablesIncluded, i);
				}
			} else {
				constraint = new DefiniteConstraint(variablesIncluded, i);
			}
			constraints.add(constraint);
		}
		return constraints;
	}
}
