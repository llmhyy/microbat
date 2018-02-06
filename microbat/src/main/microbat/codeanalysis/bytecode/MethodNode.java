package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;

import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;

public class MethodNode {
	private String methodSign;
	private Method method;
	
	/**
	 * the instruction handles are the call sites for each method
	 */
	private Map<MethodNode, List<InstructionHandle>> callers = new HashMap<>();
	private Map<InstructionHandle, MethodNode> callees = new HashMap<>();
	
	
	public MethodNode(String methodSign, Method method){
		this.setMethodSign(methodSign);
		this.method = method;
	}
	
	public List<InstructionHandle> findVariableDefinition(Variable var){
		if(var instanceof FieldVar){
			return findFieldDefinition((FieldVar)var);
		}
		else if(var instanceof LocalVar){
			//TODO
		}
		else if(var instanceof ArrayElementVar){
			//TODO
		}
		
		return null;
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

	

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public void addCaller(MethodNode node, InstructionHandle handle){
		List<InstructionHandle> list = callers.get(node);
		if(list==null){
			list = new ArrayList<>();
		}
		
		if(!list.contains(handle)){
			list.add(handle);
		}
		
		callers.put(node, list);
	}
	
	public void addCallee(InstructionHandle handle, MethodNode node){
		callees.put(handle, node);
	}

	public String getMethodSign() {
		return methodSign;
	}

	public void setMethodSign(String methodSign) {
		this.methodSign = methodSign;
	}

	public Map<InstructionHandle, MethodNode> getCallees() {
		return callees;
	}

	public void setCallees(Map<InstructionHandle, MethodNode> callees) {
		this.callees = callees;
	}

	public Map<MethodNode, List<InstructionHandle>> getAllCallers() {
		Map<MethodNode, List<InstructionHandle>> map = new HashMap<>();
		findAllCallers(map, this);
		
		return map;
	}

	private void findAllCallers(Map<MethodNode, List<InstructionHandle>> map, MethodNode methodNode) {
		for(MethodNode caller: methodNode.getCallers().keySet()){
			if(!map.keySet().contains(caller)){
				map.put(caller, methodNode.getCallers().get(caller));
				findAllCallers(map, caller);
			}
		}
	}

	public Map<MethodNode, List<InstructionHandle>> getCallers() {
		return callers;
	}

	public void setCallers(Map<MethodNode, List<InstructionHandle>> callers) {
		this.callers = callers;
	}
	
}
