package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;

import microbat.model.variable.FieldVar;

public class MethodNode {
	private String methodSign;
	private Method method;
	
	private List<MethodNode> callers = new ArrayList<>();
	private List<MethodNode> callees = new ArrayList<>();
	
	
	public MethodNode(String methodSign, Method method){
		this.setMethodSign(methodSign);
		this.method = method;
	}
	
	public List<InstructionHandle> findFieldDefinition(FieldVar field){
		List<InstructionHandle> hList = new ArrayList<>();
		
		ConstantPoolGen gen = new ConstantPoolGen(method.getConstantPool());
		
		InstructionList list = new InstructionList(method.getCode().getCode());
		for(InstructionHandle handle: list.getInstructionHandles()){
			Instruction instruction = handle.getInstruction();
			if(instruction instanceof PUTFIELD){
				PUTFIELD ins = (PUTFIELD)instruction;
				String fieldName = ins.getFieldName(gen);
				String className = ins.getReferenceType(gen).getSignature();
				
				if(field.getName().equals(fieldName) && field.getDeclaringType().equals(className)){
					hList.add(handle);
				}
				
			}
			else if(instruction instanceof PUTSTATIC){
				PUTSTATIC ins = (PUTSTATIC)instruction;
				String fieldName = ins.getFieldName(gen);
				String className = ins.getReferenceType(gen).getSignature();
				
				if(field.getName().equals(fieldName) && field.getDeclaringType().equals(className)){
					hList.add(handle);
				}
			}
		}
		
		return hList;
	}
	
	@Override
	public String toString() {
		return "MethodNode [methodSign=" + getMethodSign() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getMethodSign() == null) ? 0 : getMethodSign().hashCode());
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
		if (getMethodSign() == null) {
			if (other.getMethodSign() != null)
				return false;
		} else if (!getMethodSign().equals(other.getMethodSign()))
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
	
	public void addCaller(MethodNode node){
		callers.add(node);
	}
	
	public void addCallee(MethodNode node){
		callees.add(node);
	}

	public String getMethodSign() {
		return methodSign;
	}

	public void setMethodSign(String methodSign) {
		this.methodSign = methodSign;
	}
	
}
