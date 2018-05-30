package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

public class BlockNode {
	private List<BlockNode> children = new ArrayList<>();
	private List<BlockNode> parents = new ArrayList<>();
	
	private List<CFGNode> contents = new ArrayList<>();

	public String toString(){
		if(!contents.isEmpty()){
			return contents.get(0).toString();
		}
		
		return "";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getContents().get(0) == null) ? 0 : getContents().get(0).hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockNode other = (BlockNode) obj;
		if (getContents().get(0) == null) {
			if (other.getContents().get(0) != null)
				return false;
		} else if (!getContents().get(0).equals(other.getContents().get(0)))
			return false;
		return true;
	}

	public void addContent(CFGNode cNode) {
		if(!getContents().contains(cNode)){
			getContents().add(cNode);
			cNode.setBlockNode(this);
		}
	}
	
	public void addChild(BlockNode child){
		if(!children.contains(child)){
			children.add(child);			
		}
	}
	
	public void addParent(BlockNode parent){
		if(!parents.contains(parent)){
			parents.add(parent);			
		}
	}

	public List<CFGNode> getContents() {
		return contents;
	}

	public void setContents(List<CFGNode> contents) {
		this.contents = contents;
	}

	public List<BlockNode> getChildren() {
		return children;
	}

	public void setChildren(List<BlockNode> children) {
		this.children = children;
	}

	public List<BlockNode> getParents() {
		return parents;
	}

	public void setParents(List<BlockNode> parents) {
		this.parents = parents;
	}
}
