package microbat.vectorization.vector;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class EnvironmentVector extends Vector {
	
	public static final int DIMENSION = 5;
	
	private static final int ORDER_IDX = 0;
	private static final int CONDITION_IDX = 1;
	private static final int CONTROL_IDX = 2;
	private static final int INVOK_LEVEL_IDX = 3;
	
	public EnvironmentVector() {
		super(EnvironmentVector.DIMENSION);
	}
	
	public EnvironmentVector(final TraceNode node) {
		super(EnvironmentVector.DIMENSION);
		
		final Trace trace = node.getTrace();
		final float order = node.getOrder() / (float) trace.size();
		this.vector[EnvironmentVector.ORDER_IDX] = order;
		
		final boolean isCondition = node.isConditional();
		this.vector[EnvironmentVector.CONDITION_IDX] = isCondition ? 1.0f : 0.0f;
		
		
		this.vector[EnvironmentVector.CONTROL_IDX] = node.getControlDominatees().size() / (float) trace.size();
		
		this.vector[EnvironmentVector.INVOK_LEVEL_IDX] = 1 / (float) (node.getInvocationLevel()+1);
		
	}

}