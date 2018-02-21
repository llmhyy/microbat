package microbat.model.variable;

public class LocalVar extends Variable{
	private static final long serialVersionUID = 8801623623453887555L;
	//	private String variableName;
//	private String type;
	private int lineNumber;
	private String locationClass;
	private int byteCodeIndex;
	
	private boolean isParameter = false;
	
	public LocalVar(String name, String type, String locationClass, int lineNumber){
		super(name, type);
		this.lineNumber = lineNumber;
		this.locationClass = locationClass;
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
		LocalVar other = (LocalVar) obj;
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
	public String toString() {
		return "LocalVariable [type=" + type + ", variableName=" + variableName
				+ "]";
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getLocationClass() {
		return locationClass;
	}

	public void setLocationClass(String locationClass) {
		this.locationClass = locationClass;
	}
	
	@Override
	public String getSimpleName() {
		String sName = variableName;
		return sName;
	}

	@Override
	public Variable clone() {
		LocalVar var = new LocalVar(variableName, type, locationClass, lineNumber);
		var.setVarID(varID);
		var.setParameter(isParameter);
		return var;
	}

	public boolean isParameter() {
		return isParameter;
	}

	public void setParameter(boolean isParameter) {
		this.isParameter = isParameter;
	}

	public int getByteCodeIndex() {
		return byteCodeIndex;
	}

	public void setByteCodeIndex(int byteCodeIndex) {
		this.byteCodeIndex = byteCodeIndex;
	}
}
