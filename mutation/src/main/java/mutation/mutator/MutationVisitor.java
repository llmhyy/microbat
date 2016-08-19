package mutation.mutator;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.visitor.CloneVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mutation.mutator.mapping.MutationMap;
import mutation.parser.ClassAnalyzer;
import mutation.parser.ClassDescriptor;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.Randomness;

/**
 * Created by hoangtung on 4/3/15.
 */
public class MutationVisitor extends AbstractMutationVisitor {
	private static final int INT_CONST_ADJUST_HALF_VALUE = 5;
	private List<Integer> lineNumbers;
	private Map<Integer, List<MutationNode>> result;
	private MutationMap mutationMap;
	private CloneVisitor nodeCloner;
	private ClassAnalyzer clasAnalyzer;
	private ClassDescriptor classDescriptor;
	private VariableSubstitution varSubstitution;
	
	public void reset(ClassDescriptor classDescriptor, List<Integer> lineNos) {
		this.lineNumbers = lineNos;
		this.classDescriptor = classDescriptor;
		varSubstitution = new VariableSubstitution(classDescriptor);
		result.clear();
	}

	public MutationVisitor(MutationMap mutationMap, ClassAnalyzer classAnalyzer) {
		lineNumbers = new ArrayList<Integer>();
		result = new HashMap<Integer, List<MutationNode>>();
		nodeCloner = new CloneVisitor();
		setMutationMap(mutationMap);
		setClasAnalyzer(classAnalyzer);
	}
	
	public void run(CompilationUnit cu) {
		cu.accept(this, true);
		
	}
	
	@Override
	protected boolean beforeVisit(Node node) {
		for (Integer lineNo : lineNumbers) {
			if (lineNo >= node.getBeginLine() && lineNo <= node.getEndLine()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected boolean beforeMutate(Node node) {
		return lineNumbers.contains(node.getBeginLine());
	}
	
	@Override
	public void visit(ForStmt n, Boolean arg) {
		if (beforeVisit(n)) {
			n.getBody().accept(this, arg);
		}
	}
	
	@Override
	public void visit(WhileStmt n, Boolean arg) {
		if (beforeVisit(n)) {
			n.getBody().accept(this, arg);
		}
	}
	
	private MutationNode newNode(Node node) {
		MutationNode muNode = new MutationNode(node);
		CollectionUtils.getListInitIfEmpty(result, node.getBeginLine())
				.add(muNode);
		return muNode;
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
	
	public void setMutationMap(MutationMap mutationMap) {
		this.mutationMap = mutationMap;
	}
	
	public void setClasAnalyzer(ClassAnalyzer clasAnalyzer) {
		this.clasAnalyzer = clasAnalyzer;
	}
	
	public CloneVisitor getNodeCloner() {
		return nodeCloner;
	}
	
	public Map<Integer, List<MutationNode>> getResult() {
		return result;
	}
	
	public static class MutationNode {
		private Node orgNode;
		private List<Node> mutatedNodes;
		
		public MutationNode(Node orgNode) {
			mutatedNodes = new ArrayList<Node>();
			this.orgNode = orgNode;
		}
		
		public static MutationNode of(Node orgNode, Node newNode) {
			MutationNode node = new MutationNode(orgNode);
			node.mutatedNodes.add(newNode);
			return node;
		}

		public Node getOrgNode() {
			return orgNode;
		}

		public List<Node> getMutatedNodes() {
			return mutatedNodes;
		}
	}
}
