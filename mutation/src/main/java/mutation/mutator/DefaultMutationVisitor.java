package mutation.mutator;

import java.util.List;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import mutation.mutator.mapping.MutationMap;
import mutation.parser.ClassAnalyzer;
import sav.common.core.utils.Randomness;

public class DefaultMutationVisitor extends MutationVisitor {
	private static final int INT_CONST_ADJUST_HALF_VALUE = 5;
	
	public DefaultMutationVisitor() {
		super();
	}

	public DefaultMutationVisitor(MutationMap mutationMap, ClassAnalyzer classAnalyzer) {
		super(mutationMap, classAnalyzer);
	}

	@Override
	public boolean mutate(FieldAccessExpr n) {
		return false;
	}
	
	@Override
	public boolean mutate(AssignExpr n) {
		MutationNode muNode = newNode(n);
		// change the operator
		List<Operator> muOps = mutationMap.getMutationOp(n.getOperator());
		if (!muOps.isEmpty()) {
			for (Operator muOp : muOps) {
				AssignExpr newNode = (AssignExpr) nodeCloner.visit(n, null); 
				newNode.setOperator(muOp);
				muNode.getMutatedNodes().add(newNode);
			}
		}
		return true;
	}
	
	@Override
	public boolean mutate(BinaryExpr n) {
		MutationNode muNode = newNode(n);
		// change the operator
		List<BinaryExpr.Operator> muOps = mutationMap.getMutationOp(n.getOperator());
		if (!muOps.isEmpty()) {
			for (BinaryExpr.Operator muOp : muOps) {
				BinaryExpr newNode = (BinaryExpr) nodeCloner.visit(n, null); 
				newNode.setOperator(muOp);
				muNode.getMutatedNodes().add(newNode);
			}
		}
		return true;
	}
	
	@Override
	public boolean mutate(NameExpr n) {
		
//		Node parent = n.getParentNode();
//		if(parent instanceof AssignExpr){
//			AssignExpr assignExpr = (AssignExpr)parent;
//			Expression target = assignExpr.getTarget();
//			
//			if(target != n){
//				MutationNode muNode = newNode(n);
//				List<VariableDescriptor> candidates = varSubstitution
//						.findSubstitutions(n.getName(), n.getEndLine(),
//								n.getEndColumn());
//				for (VariableDescriptor var : candidates) {
//					if (!var.getName().equals(n.getName())) {
//						muNode.getMutatedNodes().add(nameExpr(var.getName()));
//					}
//				}
//			}
//			else{
//				System.out.println("stoping mutating the variable of a right-hand-side assignment");
//			}
//			
//		}
		
//		MutationNode muNode = newNode(n);
//		List<VariableDescriptor> candidates = varSubstitution
//				.findSubstitutions(n.getName(), n.getEndLine(),
//						n.getEndColumn());
//		for (VariableDescriptor var : candidates) {
//			if (!var.getName().equals(n.getName())) {
//				muNode.getMutatedNodes().add(nameExpr(var.getName()));
//			}
//		}
		return false;
	}
	
	@Override
	public boolean mutate(BooleanLiteralExpr n) {
		newNode(n).getMutatedNodes().add(new BooleanLiteralExpr(!n.getValue()));
		return false;
	}
	
	@Override
	public boolean mutate(IntegerLiteralExpr n) {
		final String stringValue = n.getValue();
		Integer val = stringValue.startsWith("0x") ? Integer.decode(stringValue) : Integer.valueOf(stringValue);
		int newVal = val;
		if (val.intValue() == Integer.MAX_VALUE) {
			newVal -= Randomness.nextInt(INT_CONST_ADJUST_HALF_VALUE);
		} else if (val.intValue() == Integer.MIN_VALUE) {
			newVal += Randomness.nextInt(INT_CONST_ADJUST_HALF_VALUE);
		} else {
			newVal = Randomness.nextInt(val - INT_CONST_ADJUST_HALF_VALUE, 
					val + INT_CONST_ADJUST_HALF_VALUE);
		}
		newNode(n).getMutatedNodes().add(
				new IntegerLiteralExpr(String.valueOf(newVal)));
		return false;
	}
}
