package microbat.instrumentation.cfgcoverage.runtime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import microbat.model.BreakPointValue;

public class TestInputData implements Serializable {
	private static final long serialVersionUID = 2224373310288325990L;
	private int testIdx;
	private BreakPointValue methodInputValue;
	private Map<Integer, Double> conditionVariationMap;

	public TestInputData() {
		conditionVariationMap = new HashMap<>();
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
}
