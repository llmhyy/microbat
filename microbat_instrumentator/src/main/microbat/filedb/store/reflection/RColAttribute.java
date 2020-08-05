package microbat.filedb.store.reflection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LLT
 *
 */
public class RColAttribute<T> extends RAttribute<T> {
	private List<Object> contentTypes = new ArrayList<Object>(2);
	
	public RColAttribute(String className) {
		super(className);
	}

	public List<Object> getContentTypes() {
		return contentTypes;
	}

	public void setContentTypes(List<Object> contentTypes) {
		this.contentTypes = contentTypes;
	}
	
	@Override
	public boolean isCollectionAttr() {
		return true;
	}

	public boolean isListAttr() {
		return List.class.getName().equals(getClassName());
	}
}
