package microbat.baseline.encoders;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

import java.util.List;

public class ProbabilityEncoder {
	private StatementEncoder stmtEncoder;
	private VariableEncoder varEncoder;
	private Trace trace;
	
	public ProbabilityEncoder(Trace trace) {
		this.stmtEncoder = new StatementEncoder();
		this.varEncoder = new VariableEncoder();
		this.trace = trace;
	}
	
	public void encode() {
		
	}
	
	private void preEncode() {
		/* 
		 * method before encoding that does:
		 * 1. set all input variable to be HIGH
		 * 2. set all output variable to be LOW
		*/
		List<TraceNode> executionList = this.trace.getExecutionList();
		for (TraceNode tn : executionList) {
			
		}
	}
}
