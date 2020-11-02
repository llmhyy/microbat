package microbat.model.value;

import microbat.model.variable.Variable;
import microbat.model.variable.VirtualVar;

public class VirtualValue extends VarValue {
	private static final long serialVersionUID = 8295559919201412983L;

	private long uniqueId;
	
	public VirtualValue(boolean isRoot, Variable variable, long uniqueID) {
		this.isRoot = isRoot;
		this.variable = variable;
		this.uniqueId = uniqueID;
	}
	
	@Override
	public boolean isTheSameWith(GraphNode node) {
		if(node instanceof VirtualValue){
			VirtualValue thatValue = (VirtualValue)node;
			
			return this.getStringValue().equals(thatValue.getStringValue());
		}
		
		return false;
	}

	public boolean isOfPrimitiveType(){
		if(this.variable instanceof VirtualVar){
			VirtualVar var = (VirtualVar)this.variable;
			return var.isOfPrimitiveType();
		}
		
		return false;
	}

	@Override
	public VarValue clone() {
		VirtualValue clonedValue = new VirtualValue(isRoot, variable.clone(), this.uniqueId);
		return clonedValue;
	}
	
	@Override
	public String getHeapID() {
		return String.valueOf(uniqueId);
	}
}
