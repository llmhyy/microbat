package microbat.model.value;

import microbat.model.variable.Variable;
import microbat.model.variable.VirtualVar;

public class VirtualValue extends VarValue {
	
	public VirtualValue(boolean isRoot, Variable variable) {
		this.isRoot = isRoot;
		this.variable = variable;
//		this.stringValue = "? (returned from the method invocation)";
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
}
