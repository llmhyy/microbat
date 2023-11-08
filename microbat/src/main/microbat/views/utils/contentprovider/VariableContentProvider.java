package microbat.views.utils.contentprovider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import microbat.model.trace.TraceNode;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.util.MicroBatUtil;

public abstract class VariableContentProvider implements ITreeContentProvider{

	protected TraceNode currentNode;
	
	public VariableContentProvider(final TraceNode currentNode) {
		this.currentNode = currentNode;
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof ArrayList<?> elements){
			return elements.toArray(new VarValue[0]);
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ReferenceValue parent){
			List<VarValue> children = parent.getChildren();
			if(children == null){
				String varID = Variable.truncateSimpleID(parent.getVarID());
				VarValue vv = this.findVarValue(varID);
				if(vv != null){
					List<VarValue> retrievedChildren = vv.getAllDescedentChildren();
					MicroBatUtil.assignWrittenIdentifier(retrievedChildren, currentNode);
					parent.setChildren(vv.getChildren());
					return vv.getChildren().toArray(new VarValue[0]);
				}
			}
			else{
				return parent.getChildren().toArray(new VarValue[0]);
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] children = this.getChildren(element);
		return (children != null && children.length != 0);
	}
	
	abstract protected VarValue findVarValue(final String varID);
}
