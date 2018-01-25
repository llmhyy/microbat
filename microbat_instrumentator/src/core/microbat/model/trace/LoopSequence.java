package microbat.model.trace;

public class LoopSequence {
	private int startOrder;
	private int endOrder;

	public LoopSequence(int startOrder, int endOrder) {
		super();
		this.startOrder = startOrder;
		this.endOrder = endOrder;
	}

	public int getStartOrder() {
		return startOrder;
	}

	public void setStartOrder(int startOrder) {
		this.startOrder = startOrder;
	}

	public int getEndOrder() {
		return endOrder;
	}

	public void setEndOrder(int endOrder) {
		this.endOrder = endOrder;
	}

	public boolean containsRangeOf(TraceNode node) {
		boolean isContain = startOrder <= node.getOrder()
				&& node.getOrder() <= endOrder;
		return isContain;
	}

}
