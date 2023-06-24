package microbat.probability.SPP.vectorization;

import microbat.model.trace.TraceNode;
import microbat.probability.SPP.vectorization.vector.ClassificationVector;
import microbat.probability.SPP.vectorization.vector.NodeVector;
import microbat.probability.SPP.vectorization.vector.Vector;

public class NodeFeatureRecord {
	
//	final public NodeVector nodeVector;
//	final public ClassificationVector classificationVector;
	final public Vector vector;
	final public TraceNode node;
	
	public NodeFeatureRecord(final TraceNode node, final Vector vector) {
		this.node = node;
		this.vector = vector;
	}
	
	@Override
	public String toString() {
		return this.vector.toString() + ":" + this.node.getCodeStatement();
	}
}
