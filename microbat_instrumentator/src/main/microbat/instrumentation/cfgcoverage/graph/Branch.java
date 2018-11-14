package microbat.instrumentation.cfgcoverage.graph;

import java.io.Serializable;

import org.apache.bcel.Const;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;

import microbat.codeanalysis.bytecode.CFGNode;
import sav.common.core.utils.CollectionUtils;

public class Branch implements Serializable {
	private static final long serialVersionUID = -1054499814399081119L;
	protected CoverageSFNode fromNode;
	protected CoverageSFNode toNode;
	private BranchType branchType;
	private BranchCondition branchCond;
	
	Branch(CoverageSFNode fromNode, CoverageSFNode toNode) {
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	
	public String getBranchID(){
		return getBranchId(fromNode, toNode);
	}
	
	public static String getBranchId(CoverageSFNode from, CoverageSFNode to) {
		return from.getCvgIdx() + "-" + to.getCvgIdx();
	}
	
	public static Branch getBrach(String branchId, CoverageSFlowGraph graph) {
		String[] fromTo = branchId.split("-");
		return of(graph.getNodeList().get(Integer.valueOf(fromTo[0])), graph.getNodeList().get(Integer.valueOf(fromTo[1])));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fromNode.getCvgIdx();
		result = prime * result + toNode.getCvgIdx();
		return result;
	}
	
	public BranchType getBranchType() {
		if (branchType == null) {
			if (!fromNode.isConditionalNode()) {
				branchType = BranchType.TRUE;
			} else {
				CFGNode cfgNode = fromNode.getLastCFGNode();
				if (cfgNode .getInstructionHandle().getInstruction() instanceof BranchInstruction) {
					BranchInstruction branchInsn = (BranchInstruction) cfgNode.getInstructionHandle().getInstruction();
					InstructionHandle branchTarget = branchInsn.getTarget();
					if (toNode.getFirstCFGNode().getInstructionHandle().equals(branchTarget)) {
						branchType = BranchType.TRUE;
					} else {
						branchType = BranchType.FALSE;
					}
				} else {
					branchType = BranchType.TRUE;
				}
			}
		}
		return branchType;
	}
	
	public BranchCondition getBranchCondition() {
		if (branchCond != null || !fromNode.isConditionalNode()) {
			return branchCond;
		}
		CFGNode cfgNode = fromNode.getLastCFGNode();
		if (cfgNode.getInstructionHandle().getInstruction() instanceof IfInstruction) {
			if (CollectionUtils.existIn(cfgNode.getInstructionHandle().getPrev().getInstruction().getOpcode(),
					Const.DCMPG, Const.DCMPL, Const.FCMPG, Const.FCMPL, Const.LCMP)) {
				branchCond = BranchCondition.valueOf(cfgNode.getInstructionHandle().getPrev().getInstruction().getOpcode(),
						cfgNode.getInstructionHandle().getInstruction().getOpcode());
			} else {
				branchCond = BranchCondition.valueOf(cfgNode.getInstructionHandle().getInstruction().getOpcode());
			}
		} else {
			branchCond = BranchCondition.EQ;
		}
		if (getBranchType() == BranchType.FALSE) {
			branchCond = BranchCondition.negate(branchCond);
		}
		return branchCond;
	}
	
	public boolean isCovered(){
		return this.getFromNode().getCoveredBranches().contains(this.getToNode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Branch other = (Branch) obj;
		if (fromNode.getCvgIdx() != other.fromNode.getCvgIdx())
			return false;
		if (toNode.getCvgIdx() != other.toNode.getCvgIdx())
			return false;
		return true;
	}

	public int getFromNodeIdx() {
		return fromNode.getCvgIdx();
	}

	public int getToNodeIdx() {
		return toNode.getCvgIdx();
	}
	
	public CoverageSFNode getFromNode() {
		return fromNode;
	}
	
	public CoverageSFNode getToNode() {
		return toNode;
	}

	@Override
	public String toString() {
		return "Branch [" + fromNode.getCvgIdx() + "(line " + fromNode.getFirstCFGNode().getLineNo() + ")" + "  -->  " 
				+ toNode.getCvgIdx() + "(line " + toNode.getFirstCFGNode().getLineNo() + ")" + "]";
	}

	public static Branch of(CoverageSFNode fromNode, CoverageSFNode toNode) {
		return fromNode.getGraph().getBranch(fromNode, toNode);
	}
}
