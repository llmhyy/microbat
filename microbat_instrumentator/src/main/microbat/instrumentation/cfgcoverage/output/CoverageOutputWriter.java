package microbat.instrumentation.cfgcoverage.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.Branch;
import microbat.instrumentation.cfgcoverage.graph.CoveragePath;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.output.OutputWriter;
import microbat.model.BreakPointValue;
import sav.common.core.utils.CollectionUtils;

public class CoverageOutputWriter extends OutputWriter {

	public CoverageOutputWriter(OutputStream out) {
		super(out);
	}
	
	public void writeInputData(Map<Integer, BreakPointValue> inputData) throws IOException {
		writeSerializableMap(inputData);
	}

	public void writeCfgCoverage(CoverageSFlowGraph coverageGraph) throws IOException {
		writeVarInt(coverageGraph.getCfgSize());
		writeVarInt(coverageGraph.getExtensionLayer());
		
		/* covered testcases */
		writeListString(coverageGraph.getCoveredTestcases());

		/* covered testcase indexies */
		writeListInt(coverageGraph.getCoveredTestcaseIdexies());
		
		/* nodeCoverage list */
		writeVarInt(coverageGraph.getNodeList().size());
		for (CoverageSFNode node : coverageGraph.getNodeList()) {
			writeNodeCoverage(node);
		}
		
		/* covered path */
		writeCoveragePaths(coverageGraph.getCoveragePaths());
	}

	private void writeCoveragePaths(List<CoveragePath> coveragePaths) throws IOException {
		writeVarInt(coveragePaths.size());
		for (CoveragePath coveredPath : coveragePaths) {
			writeListInt(coveredPath.getCoveredTcs());
			writeListInt(coveredPath.getPath());
		}
	}
	
	private void writeListCoverageNode(List<CoverageSFNode> list) throws IOException {
		if (list == null) {
			writeVarInt(-1);
		} else if (list.isEmpty()) {
			writeVarInt(0);
		} else {
			writeVarInt(list.size());
		}
		for (CoverageSFNode value : list) {
			writeVarInt(value.getId());
		}
	}

	private void writeNodeCoverage(CoverageSFNode node) throws IOException {
		// type
		writeString(node.getType().name());
		
		/* write nodes in block & aliasId if has */
		switch (node.getType()) {
		case BLOCK_NODE:
			// content
			writeListInt(node.getContent());
			break;
		case ALIAS_NODE:
			writeVarInt(node.getStartIdx()); // startIdx = endIdx
			writeVarInt(node.getAliasId().getPrevNodeIdx());
			writeVarInt(node.getAliasId().getOrgNodeIdx()); // aliasId
			writeVarInt(node.getAliasId().getOutLoopBranch().getFromNodeIdx());
			writeVarInt(node.getAliasId().getOutLoopBranch().getToNodeIdx());
			break;
		case CONDITION_NODE:
			writeVarInt(node.getStartIdx()); // startIdx = endIdx
			/* covered testcases on branches */
			writeVarInt(node.getCoveredTestcasesOnBranches().keySet().size());
			for (Branch branch : node.getCoveredTestcasesOnBranches().keySet()) {
				writeVarInt(branch.getToNodeIdx());
				List<Integer> coveredTcs = node.getCoveredTestcasesOnBranches().get(branch);
				writeListInt(coveredTcs);
			}
			break;
		case INVOKE_NODE:
			writeVarInt(node.getStartIdx()); // startIdx = endIdx
			break;
		}
		/* branches */
		writeVarInt(CollectionUtils.getSize(node.getBranches()));
		for (CoverageSFNode branch : CollectionUtils.nullToEmpty(node.getBranches())) {
			writeVarInt(branch.getId());
		}
		/* covered testcases on node */
		writeListInt(node.getCoveredTestcases());
	}

	
}
