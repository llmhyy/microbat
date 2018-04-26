package microbat.recommendation.calculator;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.TraceNode;

public class TraceTraversingDistanceCalculator {

	class CommonParentInfo {
		TraceNode commonInvocationParent;
		TraceNode childInTestSide;
		TraceNode childInOccurSide;

		public CommonParentInfo(TraceNode commonInvocationParent, TraceNode childInTestSide,
				TraceNode childInOccurSide) {
			super();
			this.commonInvocationParent = commonInvocationParent;
			this.childInTestSide = childInTestSide;
			this.childInOccurSide = childInOccurSide;
		}

	}

	public TraceTraverse calculateASTTravsingDistance(TraceNode testNode, TraceNode occurNode) {
		CommonParentInfo info = findCommonParent(testNode, occurNode);

		TraceNode commonParent = info.commonInvocationParent;
		int parentLevel = -1;
		if (commonParent != null) {
			parentLevel = commonParent.getInvocationLevel();
		}

		int moveIns = occurNode.getInvocationLevel() - parentLevel - 1;
		int moveOuts = testNode.getInvocationLevel() - parentLevel - 1;
		int moveDowns = info.childInOccurSide.getOrder() - info.childInTestSide.getOrder();
		
		TraceNode node = info.childInOccurSide;
		int count = 0;
		while(node !=null && !node.equals(info.childInTestSide)){
			count++;
			node = node.getStepOverPrevious();
		}
		
		if(node !=null){
			moveDowns = count;
		}

		return new TraceTraverse(moveIns, moveOuts, moveDowns);
	}

	private CommonParentInfo findCommonParent(TraceNode testNode, TraceNode occurNode) {
		
		if(testNode.equals(occurNode)){
			return new CommonParentInfo(testNode.getInvocationParent(), testNode, testNode);
		}
		
		List<TraceNode> testParents = findParentsIncludeItself(testNode);
		List<TraceNode> occurParents = findParentsIncludeItself(occurNode);
		
		TraceNode commonParent = null;
		for(TraceNode testParent: testParents){
			if(occurParents.contains(testParent)){
				commonParent = testParent;
				
				int testIndex = testParents.indexOf(testParent);
				int occurIndex = occurParents.indexOf(testParent);
				
				TraceNode childInOccurSide = occurParents.get(occurIndex-1);
				if(commonParent.equals(testNode)){
					return new CommonParentInfo(commonParent, childInOccurSide, childInOccurSide);
				}
				else{
					TraceNode childInTestSide = testParents.get(testIndex-1);
					return new CommonParentInfo(commonParent, childInTestSide, childInOccurSide);
				}
			}
		}
		
		
		return new CommonParentInfo(null, testParents.get(0), occurParents.get(0));
	}

	private List<TraceNode> findParentsIncludeItself(TraceNode testNode) {
		List<TraceNode> list = new ArrayList<>();
		list.add(testNode);
		TraceNode p = testNode.getInvocationParent();
		while (p != null) {
			list.add(p);
			p = p.getInvocationParent();
		}

		return list;
	}
}
