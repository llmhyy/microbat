package microbat.instrumentation.cfgcoverage.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.model.BreakPointValue;

public class MethodExecutionData implements Serializable {
	private static final long serialVersionUID = 2224373310288325990L;
	private int testIdx;
	private BreakPointValue methodInputValue;
	private Map<Integer, Double> conditionVariationMap;
	private transient List<CoverageSFNode> execPath;

	public MethodExecutionData(int testIdx) {
		this.testIdx = testIdx;
		conditionVariationMap = new HashMap<>();
		execPath = new ArrayList<>();
	}
	
	public void appendExecPath(CoverageSFNode node) {
		execPath.add(node);
	}
	
	public void addConditionVariation(int coverageSFNodeId, double condVariation) {
		getConditionVariationMap().put(coverageSFNodeId, condVariation);		
	}

	public int getTestIdx() {
		return testIdx;
	}

	public void setTestIdx(int testIdx) {
		this.testIdx = testIdx;
	}

	public BreakPointValue getMethodInputValue() {
		return methodInputValue;
	}

	public void setMethodInputValue(BreakPointValue methodInputValue) {
		this.methodInputValue = methodInputValue;
	}

	public Map<Integer, Double> getConditionVariationMap() {
		return conditionVariationMap;
	}

	public void setConditionVariationMap(Map<Integer, Double> conditionVariationMap) {
		this.conditionVariationMap = conditionVariationMap;
	}
	
	public List<Integer> getExecPathId() {
		List<Integer> ids = new ArrayList<>(execPath.size());
		for (CoverageSFNode node : execPath) {
			ids.add(node.getCvgIdx());
		}
		return ids;
	}
	
	public List<CoverageSFNode> getExecPath() {
		return execPath;
	}

}
