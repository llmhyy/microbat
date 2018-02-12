package microbat.evaluation.model;

import java.util.List;

public class Trial {
	
	public static final String NOT_KILL = "not kill";
	public static final String OVER_LONG = "over long";
	public static final String SUCESS = "sucess";
	public static final String FAIL = "fail";
	
	private String testCaseName;
	private boolean isBugFound;
	private int totalSteps;
	private String mutatedFile;
	private int mutatedLineNumber;
	private List<String> jumpSteps;
	private int time;
	
	private String result;

	public Trial(){
		
	}
	
	public int getUnclearFeedbackNumber(){
		int count = 0;
		for(String str: jumpSteps){
			int index = str.indexOf(" ...");
			String feedback = str.substring(index-8, index);
			if(feedback.contains("unclear")){
				count++;
			}
		}
		
		return count;
	}
	
	public String getTestCaseName() {
		return testCaseName;
	}

	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}

	public int getMutatedLineNumber() {
		return mutatedLineNumber;
	}

	public void setMutatedLineNumber(int mutatedLineNumber) {
		this.mutatedLineNumber = mutatedLineNumber;
	}

	public boolean isBugFound() {
		return isBugFound;
	}

	public void setBugFound(boolean isBugFound) {
		this.isBugFound = isBugFound;
	}

	public List<String> getJumpSteps() {
		return jumpSteps;
	}

	public void setJumpSteps(List<String> jumpSteps) {
		this.jumpSteps = jumpSteps;
	}

	public int getTotalSteps() {
		return totalSteps;
	}

	public void setTotalSteps(int totalSteps) {
		this.totalSteps = totalSteps;
	}

	@Override
	public String toString() {
		return "Trial [testCaseName=" + testCaseName + ", mutatedFile="
				+ mutatedFile + ", isBugFound=" + isBugFound
				+ ", jumpSteps=" + jumpSteps + ", totalSteps=" + totalSteps
				+ "]";
	}

	public String getMutatedFile() {
		return mutatedFile;
	}

	public void setMutatedFile(String mutatedFile) {
		this.mutatedFile = mutatedFile;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mutatedLineNumber;
		result = prime * result
				+ ((testCaseName == null) ? 0 : testCaseName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trial other = (Trial) obj;
		if (mutatedLineNumber != other.mutatedLineNumber)
			return false;
		if (testCaseName == null) {
			if (other.testCaseName != null)
				return false;
		} else if (!testCaseName.equals(other.testCaseName))
			return false;
		return true;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	
}
