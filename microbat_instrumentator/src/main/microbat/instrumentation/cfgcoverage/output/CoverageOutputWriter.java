package microbat.instrumentation.cfgcoverage.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.CoveragePath;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.cfgcoverage.runtime.MethodExecutionData;
import microbat.instrumentation.output.OutputWriter;
import sav.common.core.utils.CollectionUtils;

public class CoverageOutputWriter extends OutputWriter {

	public CoverageOutputWriter(OutputStream out) {
		super(out);
	}
	
	public void writeInputData(Map<Integer, List<MethodExecutionData>> inputData) throws IOException {
		writeSerializableObj(inputData);
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
			writeCoverageNode(node);
		}
		
		/* covered path */
		writeCoveragePaths(coverageGraph.getCoveragePaths());
	}

	private void writeCoveragePaths(List<CoveragePath> coveragePaths) throws IOException {
		writeSize(coveragePaths);
		for (CoveragePath coveredPath : CollectionUtils.nullToEmpty(coveragePaths)) {
			writeListInt(coveredPath.getCoveredTcs());
			writeListCoverageNode(coveredPath.getPath());
		}
	}
	
	private void writeListCoverageNode(List<CoverageSFNode> list) throws IOException {
		writeSize(list);
		for (CoverageSFNode value : CollectionUtils.nullToEmpty(list)) {
			writeVarInt(value.getId());
		}
	}
	
	private void writeCoverageNode(CoverageSFNode node) throws IOException {
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
			break;
		case CONDITION_NODE:
			writeVarInt(node.getStartIdx()); // startIdx = endIdx
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
		/* covered testcases on branches */
		writeVarInt(node.getCoveredTestcasesOnBranches().keySet().size());
		for (CoverageSFNode branch : node.getCoveredTestcasesOnBranches().keySet()) {
			writeVarInt(branch.getCvgIdx());
			List<Integer> coveredTcs = node.getCoveredTestcasesOnBranches().get(branch);
			writeListInt(coveredTcs);
		}
	}
}
