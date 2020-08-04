package microbat.dbtest;

import microbat.filedb.annotation.Attribute;
import microbat.filedb.annotation.RecordType;
import microbat.model.ControlScope;

@RecordType(name="SampleClass")
public class SampleClass {

	@Attribute 
	private String sampleString;
	
	private  int aInt=0;
	
	private ControlScope controlScope;
	
	public ControlScope getControlScope() {
		return controlScope;
	}

	public void setControlScope(ControlScope controlScope) {
		this.controlScope = controlScope;
	}

	private User user;

	public User getUser() {
		if (user==null) {
			user =new User("Test");
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
	
}
