package microbat.recommendation.conflicts;

import java.util.List;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public abstract class ConflictRule {
	public abstract TraceNode checkConflicts(Trace trace, int order);
	
	protected TraceNode findOldestConflictNode(List<TraceNode> list){
		assert list.size() > 1;
		
		TraceNode oldNode = null;
		for(TraceNode node: list){
			if(oldNode == null){
				oldNode = node;
			}
			else{
				if(oldNode.getCheckTime() > node.getCheckTime()){
					oldNode = node;
				}
			}
		}
		
		return oldNode;
	}
}
