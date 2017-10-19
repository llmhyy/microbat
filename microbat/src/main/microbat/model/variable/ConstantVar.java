package microbat.model.variable;

public class ConstantVar extends Variable {

	private String value;
	
	public ConstantVar(String name, String type) {
		super(name, type);
		this.value = name;
	}

	@Override
	public String getSimpleName() {
		return value;
	}

	@Override
	public Variable clone() {
		return new ConstantVar(variableName, type);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
