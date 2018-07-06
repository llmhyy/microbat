package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockGraph {
	private List<BlockNode> list = new ArrayList<>();
	
	private Map<BlockNode, Set<BlockNode>> postDominanceMap;
	
	private BlockNode startNode;
	private List<BlockNode> exitNodeList = new ArrayList<>();
	
	public static BlockGraph createBlockGraph(CFG cfg){
		BlockGraph bGraph = new BlockGraph();
		
		Map<CFGNode, BlockNode> map = new HashMap<>();
		
		CFGNode cNode = cfg.getStartNode();
		BlockNode bNode = new BlockNode();
		bNode.addContent(cNode);
		map.put(cNode, bNode);
		bGraph.addNode(bNode);
		
		bGraph.setStartNode(bNode);
		
		constructBlockGraph(bGraph, bNode, cNode, map);
		
		return bGraph;
	}

	private void addNode(BlockNode bNode) {
		if(!getList().contains(bNode)){
			getList().add(bNode);
		}
	}

	/**
	 * precondition: 
	 * (1) bNode has added cNode as its content
	 * (2) map has put <cNode, bNode>
	 * (3) bGraph has added bNode
	 * 
	 * @param bGraph
	 * @param bNode
	 * @param cNode
	 * @param map
	 */
	private static void constructBlockGraph(BlockGraph bGraph, BlockNode bNode, CFGNode cNode,
			Map<CFGNode, BlockNode> map) {
		
		assert(bNode.getContents().contains(cNode));
		assert(map.containsKey(cNode));
		assert(bGraph.getList().contains(bNode));
		
		if(cNode.getChildren().size()<=1){
			while(cNode.getChildren().size()<=1){
				if(cNode.getChildren().size()==0){
					bGraph.addExitNode(bNode);
					return;
				}
				
				cNode = cNode.getChildren().get(0);
				boolean visited = map.get(cNode)!=null;
				if(visited){
					BlockNode childBNode = map.get(cNode);
					childBNode.addParent(bNode);
					bNode.addChild(childBNode);
					return;
				}
				
				if(cNode.getParents().size()<=1){
					bNode.addContent(cNode);
					map.put(cNode, bNode);					
				}
				else{
					BlockNode newBNode = map.get(cNode);
					if(newBNode==null){
						newBNode = new BlockNode();
						newBNode.addContent(cNode);
						bGraph.addNode(newBNode);
						map.put(cNode, newBNode);
					}
					bNode.addChild(newBNode);
					newBNode.addParent(bNode);
					
					bNode = newBNode;
				}
			}
			
			constructBlockGraph(bGraph, bNode, cNode, map);
		}
		else if(cNode.getChildren().size()>1){
			for(CFGNode childCNode: cNode.getChildren()){
				BlockNode childBNode = map.get(childCNode);
				boolean visited = true;
				if(childBNode == null){
					visited = false;
					
					childBNode = new BlockNode();
					childBNode.addContent(childCNode);
					bGraph.addNode(childBNode);
					map.put(childCNode, childBNode);
				}
				
				bNode.addChild(childBNode);
				childBNode.addParent(bNode);
				if(!visited){
					constructBlockGraph(bGraph, childBNode, childCNode, map);
				}
			}
		}
		
		
		
	}

	private void addExitNode(BlockNode bNode) {
		if(!exitNodeList.contains(bNode)){
			exitNodeList.add(bNode);
		}
		
	}

	public BlockNode getStartNode() {
		return startNode;
	}

	public void setStartNode(BlockNode startNode) {
		this.startNode = startNode;
	}

	public List<BlockNode> getExitNodeList() {
		return exitNodeList;
	}

	public void setExitNodeList(List<BlockNode> exitNodeList) {
		this.exitNodeList = exitNodeList;
	}

	public List<BlockNode> getList() {
		return list;
	}

	public void setList(List<BlockNode> list) {
		this.list = list;
	}

	public Map<BlockNode, Set<BlockNode>> getPostDominanceMap() {
		return postDominanceMap;
	}

	public void setPostDominanceMap(Map<BlockNode, Set<BlockNode>> postDominanceMap) {
		this.postDominanceMap = postDominanceMap;
	}
}
