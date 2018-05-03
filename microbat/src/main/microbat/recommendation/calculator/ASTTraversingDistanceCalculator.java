package microbat.recommendation.calculator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import microbat.model.BreakPoint;
import microbat.util.JavaUtil;
import microbat.util.MinimumASTNodeFinder;
import sav.strategies.dto.AppJavaClassPath;

public class ASTTraversingDistanceCalculator {
	private AppJavaClassPath appPath;
	
	public ASTTraversingDistanceCalculator(AppJavaClassPath appPath) {
		super();
		this.appPath = appPath;
	}

	public ASTTraverse calculateASTTravsingDistance(BreakPoint testPoint, BreakPoint avoidPoint) {
		if(!testPoint.getMethodSign().equals(avoidPoint.getMethodSign())) {
			return new ASTTraverse(-1000, -1000, -1000);
		}
		
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(testPoint.getDeclaringCompilationUnitName(), appPath);
		ASTNode testNode = findSpecificNode(cu, testPoint);
		ASTNode avoidNode = findSpecificNode(cu, avoidPoint);
		
		ASTTraverse traverse = evaluateTraverse(testNode, avoidNode);
		return traverse;
	}
	
	class LocationIdentier extends ASTVisitor{
		ASTNode parent;
		ASTNode child;
		
		public LocationIdentier(ASTNode parent, ASTNode child) {
			super();
			this.parent = parent;
			this.child = child;
		}

		int count = 0;
		boolean done = false;
		
		@Override
		public void preVisit(ASTNode node) {
			if(node.getParent()==parent && !done) {
				count++;
				if(node==child) {
					done = true;
				}
			}
		}
	}
	
	private ASTTraverse evaluateTraverse(ASTNode testNode, ASTNode avoidNode) {
		ASTNode commonParent = findCommonParent(testNode, avoidNode);
		if (commonParent == null) {
			/*
			 * for the case in which one of the two nodes is FieldDeclaration &
			 * the other is a node inside init method
			 */
			return new ASTTraverse(-1000, -1000, -1000);
		}
		if(commonParent.equals(testNode)) {
			int depth = getDepth(avoidNode, commonParent);
			return new ASTTraverse(0, depth, 0);
		}
		else {
			int ups = getDepth(testNode, commonParent)-1;
			int downs = getDepth(avoidNode, commonParent)-1;
			
			ASTNode directTestChild = getDirectChild(testNode, commonParent);
			ASTNode directAvoidChild = getDirectChild(avoidNode, commonParent);
			
			LocationIdentier li1 = new LocationIdentier(commonParent, directTestChild);
			commonParent.accept(li1);
			
			LocationIdentier li2 = new LocationIdentier(commonParent, directAvoidChild);
			commonParent.accept(li2);
			
			int rights = li2.count-li1.count;
			
			ASTTraverse traverse = new ASTTraverse(ups, downs, rights);
			
			return traverse;
		}
		
	}

	private ASTNode getDirectChild(ASTNode node, ASTNode commonParent) {
		ASTNode n = node;
		ASTNode prev = n;
		while(!n.equals(commonParent)) {
			prev = n;
			n = n.getParent();
		}
		
		return prev;
	}

	private int getDepth(ASTNode testNode, ASTNode commonParent) {
		ASTNode n = testNode;
		int d = 0;
		while(!n.equals(commonParent)) {
			d++;
			n = n.getParent();
		}
		return d;
	}

	private ASTNode findCommonParent(ASTNode testNode, ASTNode avoidNode) {
		
		List<ASTNode> testParents = findParentsIncludeItself(testNode);
		List<ASTNode> avoidParents = findParentsIncludeItself(avoidNode);
		
		for(ASTNode tParent: testParents) {
			if(avoidParents.contains(tParent)) {
				return tParent;
			}
		}
		
		return null;
	}

	private List<ASTNode> findParentsIncludeItself(ASTNode node) {
		List<ASTNode> list = new ArrayList<>();
		list.add(node);
		
		ASTNode parent = node.getParent();
		while(parent!=null && !(parent instanceof MethodDeclaration)) {
			list.add(parent);
			parent = parent.getParent();
		}
		
		if(parent!=null && parent instanceof MethodDeclaration){
			list.add(parent);
		}
		
		return list;
	}

	private ASTNode findSpecificNode(CompilationUnit cu, BreakPoint point) {
		MinimumASTNodeFinder finder = new MinimumASTNodeFinder(point.getLineNumber(), cu);
		cu.accept(finder);
		return finder.getMinimumNode();
	}
}
