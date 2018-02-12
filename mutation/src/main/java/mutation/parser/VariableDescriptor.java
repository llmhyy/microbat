package mutation.parser;

import japa.parser.ast.type.Type;

/**
 * Created by hoangtung on 3/31/15.
 */
public class VariableDescriptor {
	private Type type;
	private int modifier;
	private String name;
	// the dimension in array variable
	private int dimension;

	private Position position;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getModifier() {
		return modifier;
	}

	public void setModifier(int modifier) {
		this.modifier = modifier;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VariableDescriptor [type=" + type + ", modifier=" + modifier
				+ ", name=" + name + ", dimension=" + dimension + ", position="
				+ position + "]";
	}




}
