package microbat.model.value;

import java.util.List;

public interface GraphNode {
	public List<? extends GraphNode> getChildren();
	public List<? extends GraphNode> getParents();
	
	/**
	 * This method compares the labels of two nodes. It will not further compare their children.
	 * @param node
	 * @return
	 */
	public boolean match(GraphNode node);
	
	/**
	 * compare the content of two graph nodes
	 * @param node
	 * @return
	 */
	public boolean isTheSameWith(GraphNode node);
	
	//public String getStringValue();
}
