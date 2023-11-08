package microbat.views.utils.contentprovider;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class ReadVariableContenProvider extends VariableContentProvider {

	public ReadVariableContenProvider(TraceNode currentNode) {
		super(currentNode);
	}

	@Override
	protected VarValue findVarValue(String varID) {
		return this.currentNode.getProgramState().findVarValue(varID);
	}

}
