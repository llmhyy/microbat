package microbat.recommendation.calculator;

public class TraceTraverse {
	private int moveIns;
	private int moveOuts;
	private int moveDowns;

	public TraceTraverse(int moveIns, int moveOuts, int moveDowns) {
		super();
		this.moveIns = moveIns;
		this.moveOuts = moveOuts;
		this.moveDowns = moveDowns;
	}

	public int getMoveIns() {
		return moveIns;
	}

	public void setMoveIns(int moveIns) {
		this.moveIns = moveIns;
	}

	public int getMoveOuts() {
		return moveOuts;
	}

	public void setMoveOuts(int moveOuts) {
		this.moveOuts = moveOuts;
	}

	public int getMoveDowns() {
		return moveDowns;
	}

	public void setMoveDowns(int moveDowns) {
		this.moveDowns = moveDowns;
	}

}
