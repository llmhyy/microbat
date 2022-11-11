package microbat.handler;

import java.util.Collection;

import microbat.model.value.VarValue;

public interface RequireIO {
	public void registerHandler();
	public void addInputs(Collection<VarValue> inputs);
	public void addOutputs(Collection<VarValue> outputs);
	public void printIO();
	public void clearData();
}
