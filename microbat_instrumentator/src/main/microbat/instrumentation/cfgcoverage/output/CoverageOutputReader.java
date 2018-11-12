package microbat.instrumentation.cfgcoverage.output;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.Branch;
import microbat.instrumentation.cfgcoverage.graph.CoveragePath;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode.Type;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.cfgcoverage.runtime.MethodExecutionData;
import microbat.instrumentation.output.ByteConverter;
import microbat.instrumentation.output.OutputReader;

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
		
		/* nodeCoverage list */
		int nodeListSize = readVarInt();
		List<CoverageSFNode> nodeList = new ArrayList<CoverageSFNode>(nodeListSize);
		for (int i = 0; i < nodeListSize; i++) {
			nodeList.add(new CoverageSFNode(i, coverageGraph));
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
		if (size < 0) {
			size = 0;
		}
		List<CoveragePath> coveragePaths = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			CoveragePath path = new CoveragePath();
			path.setCoveredTcs(readListInt());
			path.setPath(readListCoverageNode(nodeList));
			coveragePaths.add(path);
		}
		return coveragePaths;
	}
	
	private List<CoverageSFNode> readListCoverageNode(List<CoverageSFNode> allNodes) throws IOException {
		int size = readVarInt();
		if (size == -1) {
			return null;
		}
		List<CoverageSFNode> list = new ArrayList<CoverageSFNode>(size);
		for (int i = 0; i < size; i++) {
			list.add(allNodes.get(readVarInt()));
		}
		return list;
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
			break;
		case CONDITION_NODE:
			node.setStartIdx(readVarInt());
			node.setBlockScope();
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
		node.setCoveredTestcases(readListString());
		/* read covered testcases on branch */
		/* covered testcases on branches */
		Map<CoverageSFNode, List<String>> coveredTcsOnBranches = node.getCoveredTestcasesOnBranches();
		size = readVarInt();
		for (int i = 0; i < size; i++) {
			int toNodeIdx = readVarInt();
			List<String> coveredTcs = readListString();
			coveredTcsOnBranches.put(nodeList.get(toNodeIdx), coveredTcs);
		}
		node.setCoveredTestcasesOnBranches(coveredTcsOnBranches);
		return node;
	}
	
	@SuppressWarnings("unchecked")
	protected Map<Branch, List<Integer>> readCoveredTestcasesOnBranches() throws IOException {
		int size = readVarInt();
		if (size == 0) {
			return new HashMap<>();
		}
		byte[] bytes = readByteArray();
		if (bytes == null || bytes.length == 0) {
			return new HashMap<>();
		}
		Map<Branch, List<Integer>> map;
		try {
			map = (Map<Branch, List<Integer>>) ByteConverter.convertFromBytes(bytes);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		return map;
	}

	public Map<Integer, List<MethodExecutionData>> readInputData() throws IOException {
		return readSerializableObj();
	}

}
