package microbat.instrumentation.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.model.BreakPoint;
import microbat.model.value.VarValue;
import microbat.util.PrimitiveUtils;
import sav.common.core.utils.StringUtils;

/**
 * 
 * @author lyly 
 * For invoking, any touch on parameter's or invokeObj's fields
 *         will be recorded. (That means writeField will be handle in ExecutionTracer.
 */
public class InvokingDetail {
	private Set<String> relevantVarIds;
	private VarValue returnValue;
	private BreakPoint invokerEntry;
	private List<VarValue> writtenVarValue;
	private String invokeNodeId;
	
	public InvokingDetail() {
		// EMTPY CONSTRUCTOR
	}

	/* TODO LLT: check if need to convert paramtype from signature to name */
	public InvokingDetail(Object invokeObj, String className, String methodName, Object[] params, String[] paramTypes) {
		/* extract object Ids */
		relevantVarIds = new HashSet<>(params.length + 1);
		if (invokeObj == null) {
			invokeNodeId = StringUtils.dotJoin(className, methodName);
		} else {
			String objectVarId = TraceUtils.getObjectVarId(invokeObj);
			invokeNodeId = StringUtils.dotJoin(objectVarId, methodName);
			relevantVarIds.add(objectVarId);
		}
		for (int i = 0; i < paramTypes.length; i++) {
			if (!PrimitiveUtils.isPrimitiveType(paramTypes[i])) {
				relevantVarIds.add(TraceUtils.getObjectVarId(params[i]));
			}
		}
		writtenVarValue = new ArrayList<>(relevantVarIds.size());
	}
	
	public boolean updateRelevant(String parentVarId, String fieldVarId) {
		if (relevantVarIds.contains(parentVarId)) {
			boolean relevant = true;
			relevantVarIds.add(fieldVarId);
			return relevant;
		}
		return false;
	}

	public VarValue getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(VarValue returnValue) {
		this.returnValue = returnValue;
	}

	public BreakPoint getInvokerEntry() {
		return invokerEntry;
	}

	public void setInvokerEntry(BreakPoint invokerEntry) {
		this.invokerEntry = invokerEntry;
	}

	public void addWrittenValue(VarValue value) {
		writtenVarValue.add(value);
	}

	public String getInvokeNodeId() {
		return invokeNodeId;
	}
	
	public List<VarValue> getWrittenVarValue() {
		return writtenVarValue;
	}
}
