package microbat.codeanalysis.runtime;

import java.util.ArrayList;
import java.util.List;

import microbat.model.ClassLocation;

public class PreCheckInformation {
	private int threadNum;
	private int stepNum;
	private boolean isOverLong;
	private List<ClassLocation> visitedLocations = new ArrayList<>();

	public PreCheckInformation(int threadNum, int stepNum, boolean isOverLong, List<ClassLocation> visitedLocations) {
		super();
		this.threadNum = threadNum;
		this.stepNum = stepNum;
		this.isOverLong = isOverLong;
		this.visitedLocations = visitedLocations;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	public int getStepNum() {
		return stepNum;
	}

	public void setStepNum(int stepNum) {
		this.stepNum = stepNum;
	}

	public List<ClassLocation> getVisitedLocations() {
		return visitedLocations;
	}

	public void setVisitedLocations(List<ClassLocation> visitedLocations) {
		this.visitedLocations = visitedLocations;
	}

	public boolean isOverLong() {
		return isOverLong;
	}

	public void setOverLong(boolean isOverLong) {
		this.isOverLong = isOverLong;
	}

}
