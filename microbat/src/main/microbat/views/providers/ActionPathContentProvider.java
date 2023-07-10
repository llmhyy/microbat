package microbat.views.providers;

import java.util.Arrays;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.wala.util.collections.Pair;

import microbat.probability.SPP.pathfinding.ActionPath;
import debuginfo.NodeFeedbacksPair;

public class ActionPathContentProvider implements IStructuredContentProvider {
	
	// wrapper object to store the index
	public class ContentWrapper {
		private NodeFeedbacksPair nodePair;
		private int index;
		protected ContentWrapper(int index, NodeFeedbacksPair nodePair) {
			this.index = index;
			this.nodePair = nodePair;
		}
		
		public int getIndex() {
			return index;
		}
		
		public NodeFeedbacksPair getNode() {
			return nodePair;
		}
		@Override
		public boolean equals(Object other) {
			if (other instanceof ContentWrapper) {
				ContentWrapper cw = (ContentWrapper) other;
				return cw.index == this.index && cw.getNode() == this.getNode();
			}
			return false;
		}
	}
	
	@Override
	public void dispose() {
		
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {		
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof ActionPath)) return null;
		// TODO Auto-generated method stub
		ActionPath actionPath = (ActionPath) inputElement;
		NodeFeedbacksPair[] nodeFeedbacksPairs = new NodeFeedbacksPair[actionPath.getLength()];
		for (int i = 0; i < actionPath.getLength(); ++i) {			
			nodeFeedbacksPairs[i] = actionPath.get(i);
		}
		Arrays.sort(nodeFeedbacksPairs, (pair1, pair2) -> {
			return pair2.getNode().getOrder() - pair1.getNode().getOrder();
		});
		Object[] result = new Object[actionPath.getLength()];
		for (int i = 0; i < actionPath.getLength(); ++i) {
			result[i] = new ContentWrapper(i+1, nodeFeedbacksPairs[i]);
		}
		
		return result;
	}
	
	
}
