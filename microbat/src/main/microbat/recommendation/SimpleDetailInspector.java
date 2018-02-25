package microbat.recommendation;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SimpleDetailInspector extends DetailInspector{
	
	public TraceNode recommendDetailNode(TraceNode currentNode, Trace trace, VarValue wrongValue) {
		TraceNode nextNode;
		if(currentNode.getOrder() > this.inspectingRange.endNode.getOrder()){
			nextNode = this.inspectingRange.startNode;
		}
		else{
			nextNode = trace.getExecutionList().get(currentNode.getOrder());
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
