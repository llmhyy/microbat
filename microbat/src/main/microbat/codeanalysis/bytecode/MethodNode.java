package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Method;

public class MethodNode {
	private String methodSign;
	private Method method;
	
	private List<MethodNode> callers = new ArrayList<>();
	private List<MethodNode> callees = new ArrayList<>();
	
	
	public MethodNode(String methodSign, Method method){
		this.methodSign = methodSign;
		this.method = method;
	}
	
	@Override
	public String toString() {
		return "MethodNode [methodSign=" + methodSign + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methodSign == null) ? 0 : methodSign.hashCode());
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
		MethodNode other = (MethodNode) obj;
		if (methodSign == null) {
			if (other.methodSign != null)
				return false;
		} else if (!methodSign.equals(other.methodSign))
			return false;
		return true;
	}

	public List<MethodNode> getCallers() {
		return callers;
	}

	public void setCallers(List<MethodNode> callers) {
		this.callers = callers;
	}

	public List<MethodNode> getCallees() {
		return callees;
	}

	public void setCallees(List<MethodNode> callees) {
		this.callees = callees;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	
}
