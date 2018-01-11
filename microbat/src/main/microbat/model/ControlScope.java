package microbat.model;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.TraceNode;

public class ControlScope implements Scope{
	private List<ClassLocation> rangeList = new ArrayList<>();
	private boolean isLoop;
	
	public ControlScope() {
		
	}
	
	public ControlScope(List<ClassLocation> rangeList, boolean isLoop) {
		super();
		this.rangeList = rangeList;
		this.isLoop = isLoop;
	}

	public List<ClassLocation> getRangeList() {
		return rangeList;
	}

	public boolean isLoop() {
		return isLoop;
	}

	public void setRangeList(List<ClassLocation> rangeList) {
		this.rangeList = rangeList;
	}

	public void setLoop(boolean isLoop) {
		this.isLoop = isLoop;
	}

	@Override
	public boolean containLocation(ClassLocation location){
		for(ClassLocation loc: rangeList) {
			if (loc.getClassCanonicalName().equals(location.getClassCanonicalName()) && loc.getLineNumber()==location.getLineNumber()) {
				return true;
			}
		}
		
		return false;
	}

	public boolean containsNodeScope(TraceNode node) {
		return containLocation(node.getBreakPoint());
	}

	@Override
	public String toString() {
		return "LocationScope [isLoop=" + isLoop + ", rangeList=" + rangeList + "]";
	}

	public void addLocation(ClassLocation location) {
		this.rangeList.add(location);
		
	}
	
}
