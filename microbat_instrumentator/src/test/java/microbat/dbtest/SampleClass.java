package microbat.dbtest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.filedb.annotation.Attribute;
import microbat.filedb.annotation.RecordType;
import microbat.model.ControlScope;

@RecordType(name = "SampleClass")
public class SampleClass {

	@Attribute
	private String sampleString;

	private int aInt = 0;

	private ControlScope controlScope;

	@Attribute
	private List<ControlScope> listAttr;

	@Attribute
	private Map<String, ControlScope> mapAttr;

	@Attribute
	private Set<ControlScope> setAttr;
	
	@Attribute
	private List<?> genericListAttr;

	public ControlScope getControlScope() {
		return controlScope;
	}

	public void setControlScope(ControlScope controlScope) {
		this.controlScope = controlScope;
	}

	private User user;

	public User getUser() {
		if (user == null) {
			user = new User("Test");
		}
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getaInt() {
		return aInt;
	}

	public void setaInt(int aInt) {
		this.aInt = aInt;
	}

	public String getSampleString() {
		return sampleString;
	}

	public void setSampleString(String sampleString) {
		this.sampleString = sampleString;
	}

	private void sampleMethod() {
		return;
	}

	public List<ControlScope> getListAttr() {
		return listAttr;
	}

	public void setListAttr(List<ControlScope> listAttr) {
		this.listAttr = listAttr;
	}

	public Map<String, ControlScope> getMapAttr() {
		return mapAttr;
	}

	public void setMapAttr(Map<String, ControlScope> mapAttr) {
		this.mapAttr = mapAttr;
	}

	public Set<ControlScope> getSetAttr() {
		return setAttr;
	}

	public void setSetAttr(Set<ControlScope> setAttr) {
		this.setAttr = setAttr;
	}

}
