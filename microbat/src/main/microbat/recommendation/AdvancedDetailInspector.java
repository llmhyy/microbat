package microbat.recommendation;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class AdvancedDetailInspector extends DetailInspector {

	@Override
	public TraceNode recommendDetailNode(TraceNode currentNode, Trace trace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DetailInspector clone() {
		DetailInspector inspector = new AdvancedDetailInspector();
		if(this.inspectingRange != null){
			inspector.setInspectingRange(this.inspectingRange.clone());			
		}
		return inspector;
	}

}
