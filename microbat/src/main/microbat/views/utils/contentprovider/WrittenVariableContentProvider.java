package microbat.views.utils.contentprovider;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class WrittenVariableContentProvider extends VariableContentProvider {

	public WrittenVariableContentProvider(TraceNode currentNode) {
		super(currentNode);
	}

	@Override
	protected VarValue findVarValue(String varID) {
		VarValue var = null;
		if (this.currentNode.getStepOverNext() != null) {
			var = this.currentNode.getStepOverNext().getProgramState().findVarValue(varID);
		}
		if (this.currentNode.getStepInNext() != null) {
			var = currentNode.getStepInNext().getProgramState().findVarValue(varID);
		}
		return var;
	}

}
