package microbat.model.trace;

import java.util.Comparator;

public class TraceNodeOrderComparator implements Comparator<TraceNode> {

	@Override
	public int compare(TraceNode o1, TraceNode o2) {
		if(o1.getOrder() < o2.getOrder()){
			return -1;
		}
		else if(o1.getOrder() > o2.getOrder()){
			return 1;
		}
		return 0;
	}

}
