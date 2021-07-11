package microbat.model.trace;

import java.util.Comparator;

public class TraceOrderComparator implements Comparator<TraceNode> {

	@Override
	public int compare(TraceNode node1, TraceNode node2) {
		return node1.getOrder() - node2.getOrder();
	}

}
