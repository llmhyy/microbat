package microbat.instrumentation.cfgcoverage.output;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.AliasNodeId;
import microbat.instrumentation.cfgcoverage.graph.Branch;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode.Type;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.output.OutputReader;

public class CoverageOutputReader extends OutputReader{

	public CoverageOutputReader(InputStream in) {
		super(in);
	}
	
	public CoverageSFlowGraph readCfgCoverage() throws IOException {
		CoverageSFlowGraph coverageGraph = new CoverageSFlowGraph();
		coverageGraph.setCdgLayer(readVarInt());
		coverageGraph.setCoveredTestcases(readListString());
		coverageGraph.setCoveredTestcaseIdexies(readListInt());
		
		int nodeListSize = readVarInt();
		List<CoverageSFNode> nodeList = new ArrayList<CoverageSFNode>(nodeListSize);
		for (int i = 0; i < nodeListSize; i++) {
			nodeList.add(new CoverageSFNode(i));
		}
		for (int i = 0; i < nodeListSize; i++) {
			readCoverageNode(nodeList, nodeList.get(i));
		}
		coverageGraph.setNodeList(nodeList);
		coverageGraph.setStartNode(nodeList.get(0));
		return coverageGraph;
	}

	private CoverageSFNode readCoverageNode(List<CoverageSFNode> nodeList, CoverageSFNode node) throws IOException {
		/* type */
		Type type = Type.valueOf(readString());
		node.setType(type);
		
		/* nodeIds & aliasId */
		switch (type) {
		case BLOCK_NODE:
			// content
			node.setContent(readListInt());
			node.setStartEndIdx();
			break;
		case ALIAS_NODE:
			// startIdx, endIdx
			node.setStartIdx(readVarInt());
			node.setStartEndIdx();
			// aliasId
			int aliasOrgNodeIdx = readVarInt();
			node.setAliasId(new AliasNodeId(node.getStartIdx(), aliasOrgNodeIdx));
			break;
		case CONDITION_NODE:
			node.setStartIdx(readVarInt());
			node.setStartEndIdx();
			/* branches */
			int size = readVarInt();
			for (int i = 0; i < size; i++) {
				int branchCvgNodeIdx = readVarInt();
				node.addBranch(nodeList.get(branchCvgNodeIdx));
			}
			/* covered testcases on branches */
			Map<Branch, List<Integer>> coveredTcsOnBranches = node.getCoveredTestcasesOnBranches();
			size = readVarInt();
			for (int i = 0; i < size; i++) {
				int toNodeIdx = readVarInt();
				Branch branch = new Branch(node.getEndIdx(), toNodeIdx);
				List<Integer> coveredTcs = readListInt();
				coveredTcsOnBranches.put(branch, coveredTcs);
			}
			node.setCoveredTestcasesOnBranches(coveredTcsOnBranches);
			break;
		case INVOKE_NODE:
			node.setStartIdx(readVarInt());
			node.setStartEndIdx();
			break;
		}
		
		/* covered testcases on node */
		node.setCoveredTestcases(readListInt());
		return node;
	}

}
