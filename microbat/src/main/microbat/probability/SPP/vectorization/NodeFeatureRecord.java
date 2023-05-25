package microbat.probability.SPP.vectorization;

import microbat.model.trace.TraceNode;
import microbat.probability.SPP.vectorization.vector.ClassificationVector;
import microbat.probability.SPP.vectorization.vector.NodeVector;

public class NodeFeatureRecord {
	
	final public NodeVector nodeVector;
	final public ClassificationVector classificationVector;
	final public TraceNode node;
	
	public NodeFeatureRecord(final TraceNode node, final NodeVector nodeVector, final ClassificationVector classificationVector) {
		this.node = node;
		this.nodeVector = nodeVector;
		this.classificationVector = classificationVector;
	}
	
	@Override
	public String toString() {
		return this.nodeVector + ":" + this.classificationVector + ":" + node.getCodeStatement();
	}
}
