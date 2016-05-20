package microbat.recommendation;

import java.util.List;

import microbat.model.trace.TraceNode;

public class BranchMistakeBug extends Bug {
	private List<TraceNode> diffNodes;
	
	
	public BranchMistakeBug(List<TraceNode> diffNodes){
		this.diffNodes = diffNodes;
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("The bug may lies in some branch as the following lines:\n");
		for(TraceNode node: diffNodes){
			buffer.append(node.toString());
			buffer.append("\n");
		}
		
		String message = buffer.toString();
		setMessage(message);
	}
	
	public List<TraceNode> getDiffNodes() {
		return diffNodes;
	}
}
