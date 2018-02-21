package microbat.model.variable;

public class FieldVar extends Variable{
	private static final long serialVersionUID = -1516127948248246001L;
	private boolean isStatic;
	private String declaringType;
	
	public FieldVar(boolean isStatic, String name, String type, String declaringType){
		super(name, type);
		this.isStatic = isStatic;
		this.declaringType = declaringType;
	}
	
	public boolean isStatic() {
		return isStatic;
	}
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}
	public String getDeclaringType() {
		return declaringType;
	}
	public void setDeclaringType(String declaringType) {
		this.declaringType = declaringType;
	}

	@Override
	public String toString() {
		return "Field [isStatic=" + isStatic + ", type=" + type
				+ ", variableName=" + variableName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isStatic ? 1231 : 1237);
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
		FieldVar other = (FieldVar) obj;
		if (isStatic != other.isStatic)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		} else if (!variableName.equals(other.variableName))
			return false;
		return true;
	}
	
	@Override
	public String getSimpleName() {
		if(!variableName.contains(".")){
			return variableName;
		}
		else{
			String sName = variableName.substring(variableName.lastIndexOf(".")+1, variableName.length());
			return sName;			
		}
	}

	@Override
	public Variable clone() {
		FieldVar var = new FieldVar(isStatic, variableName, type, declaringType);
		var.setVarID(varID);
		var.setDeclaringType(declaringType);
		return var;
	}
}
