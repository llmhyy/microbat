package microbat.instrumentation.runtime;

import java.util.HashSet;
import java.util.Set;

import microbat.model.trace.TraceNode;
import microbat.util.PrimitiveUtils;

/**
 * 
 * @author lyly 
 * For invoking, any touch on parameter's or invokeObj's fields
 *         will be recorded. (That means writeField will be handle in ExecutionTracer.
 */
public class InvokingDetail {
	private TraceNode node;
	private Set<String> relevantVars = null;
	
	public InvokingDetail(TraceNode node) {
		this.node = node;
	}
	
	public boolean updateRelevantVar(Object refValue, Object fieldValue, String fieldType) {
		if (refValue == null) {
			return false;
		}
		
		String objectVarId = TraceUtils.getObjectVarId(refValue);
		boolean relevant = relevantVars.contains(objectVarId);
		if (!relevant && (node.getBreakPoint().getClassCanonicalName().equals(refValue.getClass().getName()))) {
			relevant = true;
		}
		if (relevant && !PrimitiveUtils.isPrimitive(fieldType)) {
			relevantVars.add(objectVarId);
		}
		return relevant;
	}
	
	public void initRelevantVars(Object invokeObj, Object[] args, String[] argType) {
		if (relevantVars == null) {
			relevantVars = new HashSet<>();
			if (invokeObj != null) {
				relevantVars.add(TraceUtils.getObjectVarId(invokeObj));
			}
			for (int i = 0; i < argType.length; i++) {
				if (!PrimitiveUtils.isPrimitive(argType[i])) {
					relevantVars.add(TraceUtils.getObjectVarId(args[i]));
				}
			}
		}
	}

	public void initRelevantVars(Object invokeObj, Object[] params, String paramTypeSignsCode) {
		if (relevantVars != null) {
			return;
		}
		initRelevantVars(invokeObj, params, TraceUtils.parseArgTypesOrNames(paramTypeSignsCode));
	}
}
