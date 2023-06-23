package microbat.views.providers;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;

import microbat.probability.SPP.pathfinding.ActionPath;
import debuginfo.NodeFeedbacksPair;

public class ActionPathContentProvider implements IStructuredContentProvider {
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
		Object[] objects = new Object[actionPath.getLength()];
		for (int i = 0; i < actionPath.getLength(); ++i) {
			objects[i] = actionPath.get(i);
		}
		return objects;
	}
	
	
}
