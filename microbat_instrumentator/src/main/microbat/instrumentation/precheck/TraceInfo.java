package microbat.instrumentation.precheck;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.model.ClassLocation;

public class TraceInfo {
	Set<ClassLocation> visitedLocs = new HashSet<>();
	List<ClassLocation> steps = new ArrayList<>();
	int stepsTotal = 0;
	int stepLimit;
	ClassLocation lastStep = null;
	
	public TraceInfo(int stepLimit) {
		this.stepLimit = stepLimit;
	}

	public ClassLocation getLastStep() {
		return lastStep;
//		if (steps.size() == 0) {
//			return null;
//		}
//		return steps.get(steps.size() - 1);
	}
	
	public void addStep(ClassLocation loc) {
//		steps.add(loc);
		lastStep = loc;
		stepsTotal++;
		visitedLocs.add(loc);
	}
	
	public List<ClassLocation> getSteps() {
		return steps;
	}
	
	public Set<ClassLocation> getVisitedLocs() {
		return visitedLocs;
	}
	
	public int getStepTotal() {
		return stepsTotal;
//		return steps.size();
	}
	
	public boolean isOverLong() {
		return getStepTotal() > stepLimit;
	}
}
