package mutation.mutator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.visitor.CloneVisitor;
import mutation.mutator.mapping.MutationMap;
import mutation.parser.ClassAnalyzer;
import mutation.parser.ClassDescriptor;
import sav.common.core.utils.CollectionUtils;

/**
 * Created by hoangtung on 4/3/15.
 */
public class MutationVisitor extends AbstractMutationVisitor {
	private List<Integer> lineNumbers;
	private Map<Integer, List<MutationNode>> result;
	protected MutationMap mutationMap;
	protected CloneVisitor nodeCloner;
	protected ClassAnalyzer clasAnalyzer;
	protected ClassDescriptor classDescriptor;
	protected VariableSubstitution varSubstitution;
	
	public void reset(ClassDescriptor classDescriptor, List<Integer> lineNos) {
		this.lineNumbers = lineNos;
		this.classDescriptor = classDescriptor;
		varSubstitution = new VariableSubstitution(classDescriptor);
		result.clear();
	}
	
	public MutationVisitor() {
		
	}

	public MutationVisitor(MutationMap mutationMap, ClassAnalyzer classAnalyzer) {
		init(mutationMap, classAnalyzer);
	}
	
	public void init(MutationMap mutationMap2, ClassAnalyzer classAnalyzer) {
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
	
	protected MutationNode newNode(Node n) {
		MutationNode muNode = new MutationNode(n);
		CollectionUtils.getListInitIfEmpty(result, n.getBeginLine())
				.add(muNode);
		return muNode;
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
		private Map<Integer, String> mutationTypes;
		
		public MutationNode(Node orgNode) {
			mutatedNodes = new ArrayList<Node>();
			this.orgNode = orgNode;
			mutationTypes = new HashMap<>();
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
		
		public void add(Node newNode, String mutationType) {
			mutatedNodes.add(newNode);
			if (mutationType != null) {
				mutationTypes.put(mutatedNodes.size() - 1, mutationType);
			}
		}
		
		public String getMutationType(int nodeIdx) {
			return mutationTypes.get(nodeIdx);
		}
	}

}
