package microbat.recommendation;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class SimpleDetailInspector extends DetailInspector{
	
	public TraceNode recommendDetailNode(TraceNode currentNode, Trace trace) {
		TraceNode nextNode;
		if(currentNode.getOrder() > this.inspectingRange.endNode.getOrder()){
			nextNode = this.inspectingRange.startNode;
		}
		else{
			nextNode = trace.getExectionList().get(currentNode.getOrder());
		}
		return nextNode;
	}

	@Override
	public DetailInspector clone() {
		DetailInspector inspector = new SimpleDetailInspector();
		if(this.inspectingRange != null){
			inspector.setInspectingRange(this.inspectingRange.clone());			
		}
		return inspector;
	}
	
	
}
