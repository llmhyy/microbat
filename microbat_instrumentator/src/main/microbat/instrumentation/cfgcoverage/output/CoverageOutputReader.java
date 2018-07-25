package microbat.instrumentation.cfgcoverage.output;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.AliasNodeId;
import microbat.instrumentation.cfgcoverage.graph.Branch;
import microbat.instrumentation.cfgcoverage.graph.CoveragePath;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode.Type;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.output.OutputReader;
import microbat.model.BreakPointValue;

public class CoverageOutputReader extends OutputReader{

	public CoverageOutputReader(InputStream in) {
		super(in);
	}
	
	public CoverageSFlowGraph readCfgCoverage() throws IOException {
		/* cdgSize and cdgLayer */
		int cfgSize = readVarInt();
		CoverageSFlowGraph coverageGraph = new CoverageSFlowGraph(cfgSize);
		coverageGraph.setCdgLayer(readVarInt());
		
		/* covered testcases */
		coverageGraph.setCoveredTestcases(readListString());
		
		/* covered testcase indexies */
		coverageGraph.setCoveredTestcaseIdexies(readListInt());
		
		/* nodeCoverage list */
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
		
		/* covered path */
		List<CoveragePath> coveragePaths = readCoveragePaths(nodeList);
		coverageGraph.setCoveragePaths(coveragePaths);
		return coverageGraph;
	}

	private List<CoveragePath> readCoveragePaths(List<CoverageSFNode> nodeList) throws IOException {
		int size = readVarInt();
		List<CoveragePath> coveragePaths = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			CoveragePath path = new CoveragePath();
			path.setCoveredTcs(readListInt());
			path.setPath(readListInt());
			coveragePaths.add(path);
		}
		return coveragePaths;
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
			node.setBlockScope();
			break;
		case ALIAS_NODE:
			// startIdx, endIdx
			node.setStartIdx(readVarInt());
			node.setBlockScope();
			// aliasId
			int aliasPrevNodeIdx = readVarInt();
			int aliasOrgNodeIdx = readVarInt();
			node.setAliasId(new AliasNodeId(aliasPrevNodeIdx, aliasOrgNodeIdx));
			Branch outLoopBranch = new Branch(readVarInt(), readVarInt());
			node.getAliasId().setOutLoopBranch(outLoopBranch);
			break;
		case CONDITION_NODE:
			node.setStartIdx(readVarInt());
			node.setBlockScope();
			/* covered testcases on branches */
			Map<Branch, List<Integer>> coveredTcsOnBranches = node.getCoveredTestcasesOnBranches();
			int size = readVarInt();
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
			node.setBlockScope();
			break;
		}
		/* branches */
		int size = readVarInt();
		for (int i = 0; i < size; i++) {
			int branchCvgNodeIdx = readVarInt();
			node.addBranch(nodeList.get(branchCvgNodeIdx));
		}
		/* covered testcases on node */
		node.setCoveredTestcases(readListInt());
		return node;
	}

	public Map<Integer, BreakPointValue> readInputData() throws IOException {
		return readSerializableMap();
	}

}
