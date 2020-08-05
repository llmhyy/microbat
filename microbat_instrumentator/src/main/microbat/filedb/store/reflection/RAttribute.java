package microbat.filedb.store.reflection;

import java.lang.reflect.Method;

/**
 * @author LLT
 *
 */
public class RAttribute<T> {
	private RType<T> rType;
	private String className;
	private Method getter;
	private boolean isEmbedded;
	private String name;

	public RAttribute(String className) {		
		this.className = className;
	}

	public RAttribute(RType<T> rClass) {
		this.rType = rClass;
	}

	public Method getGetter() {
		return getter;
	}

	public void setGetter(Method getter) {
		this.getter = getter;
	}

	public boolean isPrimiveType() {
		return rType == null;
	}

	public boolean isEmbedded() {
		return isEmbedded;
	}

	public void setEmbedded(boolean isEmbedded) {
		this.isEmbedded = isEmbedded;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public RType<T> getRType() {
		return rType;
	}
	
	public String getClassName() {
		return className;
	}
	
	public boolean isCollectionAttr() {
		return false;
	}
}
