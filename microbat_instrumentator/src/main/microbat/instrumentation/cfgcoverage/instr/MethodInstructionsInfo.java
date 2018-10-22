package microbat.instrumentation.cfgcoverage.instr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.instrumentation.cfgcoverage.InstrumentationUtils;
import microbat.instrumentation.cfgcoverage.graph.CFGInstance.UniqueNodeId;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.model.ClassLocation;
import sav.common.core.utils.CollectionUtils;

public class MethodInstructionsInfo {
	private static Map<String, Set<Integer>> instmInstructionMap;
	private static Set<String> needToInstrumentClasses;
	private List<InstructionInfo> nodeInsns;
	private List<InstructionHandle> exitInsns;
	private List<InstructionInfo> notIntCmpInIfInsns;
	private List<InstructionInfo> conditionInsns;
	
	public static void initInstrInstructions(CoverageSFlowGraph coverageFlowGraph) {
		instmInstructionMap = new HashMap<>();
		for (CoverageSFNode node : coverageFlowGraph.getNodeList()) {
			UniqueNodeId probeNodeId = node.getEndNodeId();
			CollectionUtils.getSetInitIfEmpty(instmInstructionMap, probeNodeId.getMethodId())
					.add(probeNodeId.getLocalNodeIdx());
		}
		needToInstrumentClasses = new HashSet<>();
		for (String methodId : instmInstructionMap.keySet()) {
			ClassLocation loc = InstrumentationUtils.getClassLocation(methodId);
			needToInstrumentClasses.add(loc.getClassCanonicalName());
		}
	}
	
	public static boolean hasClassInInstrumentationList(String className) {
		return needToInstrumentClasses.contains(className);
	}

	public static MethodInstructionsInfo getInstrumentationInstructions(InstructionList insnList, Method method,
			String className) {
		String methodId = InstrumentationUtils.getMethodId(className, method);
		Set<Integer> methodInstrmInsnIdexies = instmInstructionMap.get(methodId);
		if (methodInstrmInsnIdexies == null) {
			return null;
		}
		MethodInstructionsInfo instmInsns = new MethodInstructionsInfo();
		List<InstructionInfo> nodeInsns = new ArrayList<InstructionInfo>(methodInstrmInsnIdexies.size());
		List<InstructionInfo> conditionInsns = new ArrayList<>();
		List<InstructionInfo> notIntCmpInIfInsns = new ArrayList<>();
		int idx = 0;
		for (InstructionHandle insnHandler : insnList) {
			if (methodInstrmInsnIdexies.contains(idx)) {
				InstructionInfo insnInfo = new InstructionInfo(insnHandler, idx);
				nodeInsns.add(insnInfo);
				if (insnHandler.getInstruction() instanceof IfInstruction) {
					conditionInsns.add(insnInfo);
					if (CollectionUtils.existIn(insnHandler.getPrev().getInstruction().getOpcode(),
							Const.DCMPG, Const.DCMPL, Const.FCMPG, Const.FCMPL, Const.LCMP)) {
						insnInfo.setNotIntCmpIf(true);
						notIntCmpInIfInsns.add(new InstructionInfo(insnHandler.getPrev(), idx - 1));
					}
				}
			}
			idx++;
		}
		instmInsns.nodeInsns = nodeInsns;
		instmInsns.conditionInsns = conditionInsns;
		instmInsns.notIntCmpInIfInsns = notIntCmpInIfInsns;
		CFGConstructor cfgConstructor = new CFGConstructor();
		CFG cfg = cfgConstructor.constructCFG(method.getCode());
		List<InstructionHandle> exitInsns = new ArrayList<>();
		InstructionHandle[] insnHandlers = insnList.getInstructionHandles();
		for(CFGNode node: cfg.getExitList()){
			exitInsns.add(insnHandlers[node.getIdx()]);
		}
		instmInsns.exitInsns = exitInsns;
		return instmInsns;
	}

	public List<InstructionInfo> getNodeInsns() {
		return nodeInsns;
	}

	public List<InstructionHandle> getExitInsns() {
		return exitInsns;
	}

	public List<InstructionInfo> getConditionInsns() {
		return conditionInsns;
	}
	
	public List<InstructionInfo> getNotIntCmpInIfInsns() {
		return notIntCmpInIfInsns;
	}
}
