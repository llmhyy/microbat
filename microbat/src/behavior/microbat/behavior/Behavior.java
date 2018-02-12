package microbat.behavior;

public class Behavior {
	private int wrongValueFeedbacks;
	private int wrongPathFeedbacks;
	private int correctFeedbacks;
	private int unclearFeedbacks;

	private int skips;

	private int additionalClickOnSteps;

	private int searchForward;
	private int searchBackward;
	
	private int undo;
	
	private int generateTrace;

	public void increaseWrongValueFeedback(){
		this.wrongValueFeedbacks++;
	}
	
	public void increaseWrongPathFeedback(){
		this.wrongPathFeedbacks++;
	}
	
	public void increaseCorrectFeedback(){
		this.correctFeedbacks++;
	}
	
	public void increaseUnclearFeedback(){
		this.unclearFeedbacks++;
	}
	
	public void increaseSkip(){
		this.skips++;
	}
	
	public void increaseAdditionalClick(){
		this.additionalClickOnSteps++;
	}
	
	public void increaseSearchForward(){
		this.searchForward++;
	}
	
	public void increaseSearchBackward(){
		this.searchBackward++;
	}
	
	public void increaseUndo(){
		this.undo++;
	}
	
	public void increaseGenerateTrace(){
		this.generateTrace++;
	}
	
	public int getWrongValueFeedbacks() {
		return wrongValueFeedbacks;
	}

	public int getWrongPathFeedbacks() {
		return wrongPathFeedbacks;
	}

	public int getCorrectFeedbacks() {
		return correctFeedbacks;
	}

	public int getUnclearFeedbacks() {
		return unclearFeedbacks;
	}

	public int getSkips() {
		return skips;
	}

	public int getAdditionalClickOnSteps() {
		return additionalClickOnSteps;
	}

	public int getSearchForward() {
		return searchForward;
	}

	public int getSearchBackward() {
		return searchBackward;
	}

	public void setWrongValueFeedbacks(int wrongValueFeedbacks) {
		this.wrongValueFeedbacks = wrongValueFeedbacks;
	}

	public void setWrongPathFeedbacks(int wrongPathFeedbacks) {
		this.wrongPathFeedbacks = wrongPathFeedbacks;
	}

	public void setCorrectFeedbacks(int correctFeedbacks) {
		this.correctFeedbacks = correctFeedbacks;
	}

	public void setUnclearFeedbacks(int unclearFeedbacks) {
		this.unclearFeedbacks = unclearFeedbacks;
	}

	public void setSkips(int skips) {
		this.skips = skips;
	}

	public void setAdditionalClickOnSteps(int additionalClickOnSteps) {
		this.additionalClickOnSteps = additionalClickOnSteps;
	}

	public void setSearchForward(int searchForward) {
		this.searchForward = searchForward;
	}

	public void setSearchBackward(int searchBackward) {
		this.searchBackward = searchBackward;
	}

	public int getUndo() {
		return undo;
	}

	public void setUndo(int undo) {
		this.undo = undo;
	}

	public int getGenerateTrace() {
		return generateTrace;
	}

	public void setGenerateTrace(int generateTrace) {
		this.generateTrace = generateTrace;
	}

}
