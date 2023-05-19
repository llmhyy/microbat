package microbat.probability.SPP.vectorization.vector;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class EnvironmentVector extends Vector {
	
	public static final int DIMENSION = 6;
	
	private static final int ORDER_IDX = 0;
	private static final int CONDITION_IDX = 1;
	private static final int COST_IDX = 2;
	private static final int REPEAT_IDX = 3;
	private static final int CONTROL_IDX = 4;
	private static final int INVOK_LEVEL_IDX = 5;
	
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
		
		this.vector[EnvironmentVector.COST_IDX] = (float) node.getComputationCost();
		
		this.vector[EnvironmentVector.REPEAT_IDX] = 1/(float) (node.repeatedCount+1);
		
		this.vector[EnvironmentVector.CONTROL_IDX] = node.getControlDominatees().size() / (float) trace.size();
		
		this.vector[EnvironmentVector.INVOK_LEVEL_IDX] = 1 / (float) (node.getInvocationLevel()+1);
		
	}

}