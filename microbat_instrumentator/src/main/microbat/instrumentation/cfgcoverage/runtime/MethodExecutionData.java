package microbat.instrumentation.cfgcoverage.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import microbat.instrumentation.cfgcoverage.graph.Branch;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.model.BreakPointValue;

public class MethodExecutionData implements Serializable {
	private static final long serialVersionUID = 2224373310288325990L;
	private int testIdx;
	private BreakPointValue methodInputValue;
	private List<Integer> execPath; // coverageSFNode.idx
	private transient Map<Integer, Double> conditionVariationMap; // variation is always (b - a)
	private Map<String, Double> branchFitnessMap;

	public MethodExecutionData(int testIdx) {
		this.testIdx = testIdx;
		conditionVariationMap = new HashMap<>();
		execPath = new ArrayList<>();
	}
	
	public void appendExecPath(CoverageSFNode node) {
		execPath.add(node.getCvgIdx());
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
		return execPath;
	}
	
	public void calculateBranchFitnessMap(CoverageSFlowGraph graph) {
		branchFitnessMap = new HashMap<>();
		for (Entry<Integer, Double> entry : conditionVariationMap.entrySet()) {
			CoverageSFNode condNode = graph.getNodeList().get(entry.getKey());
			double fitness = 0.0;
			for (Branch branch : condNode.getBranches()) {
				switch (branch.getBranchCondition()) {
				case EQ:
					fitness = Math.abs(entry.getValue());
					break;
				case GT_GE:
					fitness = entry.getValue();
					break;
				case LT_LE:
					fitness = -entry.getValue();
					break;
				case NEQ:
					fitness = 1/(Math.abs(entry.getValue()) + 1);
					break;
				default:
					break;
				}
				branchFitnessMap.put(branch.getBranchID(), fitness);
			}
		}
	}
	
	public void setBranchFitnessMap(Map<String, Double> branchFitnessMap) {
		this.branchFitnessMap = branchFitnessMap;
	}
	
	public Map<String, Double> getBranchFitnessMap() {
		return branchFitnessMap;
	}
	
	public Map<Branch, Double> getBranchFitnessMap(CoverageSFlowGraph graph) {
		Map<Branch, Double> map = new HashMap<>();
		for (Entry<String, Double> entry : branchFitnessMap.entrySet()) {
			map.put(Branch.getBrach(entry.getKey(), graph), entry.getValue());
		}
		return map;
	}
}
