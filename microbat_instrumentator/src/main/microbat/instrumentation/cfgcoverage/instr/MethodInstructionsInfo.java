package microbat.instrumentation.cfgcoverage.instr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.instrumentation.cfgcoverage.InstrumentationUtils;
import microbat.instrumentation.cfgcoverage.graph.CFGInstance.UniqueNodeId;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import sav.common.core.utils.CollectionUtils;

public class MethodInstructionsInfo {
	private static Map<String, Set<Integer>> instmInstructionMap;
	private List<InstructionInfo> nodeInsns;
	private List<InstructionHandle> exitInsns;
	
	public static void initInstrInstructions(CoverageSFlowGraph coverageFlowGraph) {
		instmInstructionMap = new HashMap<>();
		for (CoverageSFNode node : coverageFlowGraph.getNodeList()) {
			if (node.isAliasNode()) {
				continue;
			}
			UniqueNodeId probeNodeId = node.getEndNodeId();
			CollectionUtils.getSetInitIfEmpty(instmInstructionMap, probeNodeId.getMethodId())
					.add(probeNodeId.getLocalNodeIdx());
		}
	}

	public static MethodInstructionsInfo getInstrumentationInstructions(InstructionList insnList, Method method, String className) {
		String methodId = InstrumentationUtils.getMethodId(className, method);
		Set<Integer> instmInstructionIdexies = instmInstructionMap.get(methodId);
		MethodInstructionsInfo instmInsns = new MethodInstructionsInfo();
		List<InstructionInfo> nodeInsns = Collections.emptyList();
		if (instmInstructionIdexies != null) {
			nodeInsns = new ArrayList<InstructionInfo>(instmInstructionIdexies.size());
			int idx = 0;
			for (InstructionHandle insnHandler : insnList) {
				if (instmInstructionIdexies.contains(idx)) {
					InstructionInfo insnInfo = new InstructionInfo(insnHandler, idx);
					nodeInsns.add(insnInfo);
				}
				idx++;
			}
		}
		instmInsns.nodeInsns = nodeInsns;
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

}
