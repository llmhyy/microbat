package microbat.recommendation;

public class Traverse {
	private int moveUps;
	private int moveDowns;
	private int moveRights;

	public Traverse(int ups, int downs, int rights) {
		this.moveUps = ups;
		this.moveDowns = downs;
		this.moveRights = rights;
	}

	public int getMoveUps() {
		return moveUps;
	}

	public void setMoveUps(int moveUps) {
		this.moveUps = moveUps;
	}

	public int getMoveDowns() {
		return moveDowns;
	}

	public void setMoveDowns(int moveDowns) {
		this.moveDowns = moveDowns;
	}

	public int getMoveRights() {
		return moveRights;
	}

	public void setMoveRights(int moveRights) {
		this.moveRights = moveRights;
	}

}
