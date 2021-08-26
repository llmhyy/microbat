package microbat.model.variable;

public class ArrayElementVar extends Variable {
	private static final long serialVersionUID = 9214338494353676319L;

	public ArrayElementVar(String name, String type, String aliasVarID) {
		super(name, type);
		this.aliasVarID = aliasVarID;
	}
	
	public String getIndex(){
		if(varID !=null){
			if(varID.contains("[") && varID.contains("]")){
				return varID.substring(varID.indexOf("[")+1, varID.indexOf("]"));
			}
		}
		
		return null;
	}
	
	@Override
	public String getVarID() {
		return this.varID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result
				+ ((variableName == null) ? 0 : variableName.hashCode());
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
		ArrayElementVar other = (ArrayElementVar) obj;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		} else if (!variableName.equals(other.variableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ArrayElementVar [type=" + type + ", variableName="
				+ variableName + "]";
	}

	@Override
	public String getSimpleName() {
		String sName = variableName.substring(variableName.indexOf("[")+1, variableName.length()-1);
		return sName;
	}

	@Override
	public Variable clone() {
		ArrayElementVar var = new ArrayElementVar(variableName, type, aliasVarID);
		var.setVarID(varID);
		return var;
	}
	
}
