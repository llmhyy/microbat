package microbat.model.trace;

import java.io.Serializable;
import java.util.ArrayList;

public class ConstWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6695169653983838562L;
	private byte tag;
	private ArrayList<String> attr;
	
	public ConstWrapper(byte tag, String attr1, String attr2, String attr3) {
		this(tag, attr1, attr2);
		this.attr.add(attr3);
	}
	
	public ConstWrapper(byte tag, String attr1, String attr2) {
		this(tag, attr1);
		this.attr.add(attr2);
	}
	
	public ConstWrapper(byte tag, String attr1) {
		this.tag = tag;
		this.attr = new ArrayList<>();
		attr.add(attr1);
	}
	
	public byte getTag() {
		return this.tag;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + tag + "]");
		sb.append("(");
		for (int i = 0; i < attr.size(); i++) {
			if (i != 0)
				sb.append(", ");
			sb.append(attr.get(i));
		}
		sb.append(")");
		return sb.toString();
	}
}
