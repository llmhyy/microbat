package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;

import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.ConstantVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import sav.common.core.utils.SignatureUtils;
import sav.strategies.dto.AppJavaClassPath;

public class SingleLineByteCodeVisitor extends ByteCodeVisitor {
	private int lineNumber;
	private String className;
	private AppJavaClassPath appJavaClassPath;
	
	
	
	public SingleLineByteCodeVisitor(int lineNumber, String className, AppJavaClassPath appJavaClassPath) {
		super();
		this.lineNumber = lineNumber;
		this.className = className;
		this.appJavaClassPath = appJavaClassPath;
	}

	private List<Variable> readVars = new ArrayList<>();
	private List<Variable> writtenVars = new ArrayList<>();
	private Variable returnedVar;
	private List<InstructionHandle> instructionList = new ArrayList<>();
	private Method method;
	
	public void visitMethod(Method method){
		Code code = method.getCode();
		if(code == null){
			return;
		}
		
		if(method.getName().contains("class$") && this.method!=null) {
			return;
		}
		
		if(isMethodContainLineNumber(method, lineNumber)){
			InstructionList list = new InstructionList(code.getCode());
			List<InstructionHandle> insHandles = findCorrespondingInstructions(lineNumber, code);
			this.setInstructionList(insHandles);
			this.setMethod(method);
			
			for(InstructionHandle insHandle: insHandles){
				VarOp varOp = parseReadWrittenVariable(insHandle, method, appJavaClassPath);
				if(null!=varOp && !(varOp.var instanceof ConstantVar)){
					if(varOp.op.equals(Variable.WRITTEN)){
						writtenVars.add(varOp.var);
					}
					else if(varOp.op.equals(Variable.READ)){
						readVars.add(varOp.var);
					}
				}
			}
			
			parseReturnVariable(list, method);
		}
		
    }
	
	private boolean isMethodContainLineNumber(Method method, int lineNumber) {
		for(LineNumber line: method.getLineNumberTable().getLineNumberTable()){
			if(line.getLineNumber()==lineNumber){
				return true;
			}
		}
		return false;
	}

	private void parseReturnVariable(InstructionList list, Method method) {
		for(InstructionHandle handle: list){
			if(handle.getInstruction() instanceof ReturnInstruction){
				List<InstructionHandle> previousInstructions = findPreviousInstructions(lineNumber, handle.getPosition(), method.getCode());
				Variable var = parseReturnVariableName(method, previousInstructions);
				returnedVar = var;
			}
		}
	}

	private Variable parseReturnVariableName(Method method, List<InstructionHandle> previousInstructions) {
		for(int i=previousInstructions.size()-1; i>=0; i--){
			InstructionHandle insHandle = previousInstructions.get(i);
			VarOp varOp = parseReadWrittenVariable(insHandle, method, appJavaClassPath);
			if(varOp!=null && varOp.op.equals(Variable.READ)){
				Variable var = varOp.var;
//				VirtualVar vVar = new VirtualVar(var.getName(), var.getType());
//				return vVar;
				return var;
			}
		}
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	protected List<InstructionHandle> findPreviousInstructions(int lineNumber, long offset, Code code) {
		List<InstructionHandle> correspondingInstructions = new ArrayList<>();
		
		InstructionList list = new InstructionList(code.getCode());
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			InstructionHandle insHandle = (InstructionHandle) iter.next();
			int instructionLine = code.getLineNumberTable().getSourceLine(insHandle.getPosition());
			
			if(instructionLine == lineNumber && insHandle.getPosition()<offset){
				correspondingInstructions.add(insHandle);
			}
		}
		
		return correspondingInstructions;
	}
	
	@SuppressWarnings("rawtypes")
	protected List<InstructionHandle> findPreviousInstructions(long offset, Code code) {
		List<InstructionHandle> correspondingInstructions = new ArrayList<>();
		
		InstructionList list = new InstructionList(code.getCode());
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			InstructionHandle insHandle = (InstructionHandle) iter.next();
			if(insHandle.getPosition()<offset){
				correspondingInstructions.add(insHandle);
			}
		}
		
		return correspondingInstructions;
	}
	
	class VarOp{
		Variable var;
		String op;
		public VarOp(Variable var, String op) {
			super();
			this.var = var;
			this.op = op;
		}
	}
	
	protected VarOp parseReadWrittenVariable(InstructionHandle insHandle, Method method, AppJavaClassPath appJavaClassPath) {
		Code code = method.getCode();
		ConstantPoolGen pool = new ConstantPoolGen(code.getConstantPool());
		if(insHandle.getInstruction() instanceof FieldInstruction){
			FieldInstruction gIns = (FieldInstruction)insHandle.getInstruction();
			String fullFieldName = gIns.getFieldName(pool);
			if(fullFieldName != null){
				/** rw being true means read; and rw being false means write. **/
				boolean rw = insHandle.getInstruction().getName().toLowerCase().contains("get");
				boolean isStatic = insHandle.getInstruction().getName().toLowerCase().contains("static");
				
				String type = gIns.getFieldType(pool).getSignature();
				type = SignatureUtils.signatureToName(type);
				String declaringType = gIns.getReferenceType(pool).getSignature();
				
				FieldVar var = new FieldVar(isStatic, fullFieldName, type, declaringType);
				
				if(rw){
					if(!readVars.contains(var)){
						return new VarOp(var, Variable.READ);															
					}
				}
				else{
					if(!writtenVars.contains(var)){
						return new VarOp(var, Variable.WRITTEN);						
					}
				}
			}
		}
		else if(insHandle.getInstruction() instanceof LocalVariableInstruction){
			LocalVariableInstruction lIns = (LocalVariableInstruction)insHandle.getInstruction();
			int varIndex = lIns.getIndex();
			String varName = "$" + varIndex;
			LocalVar var = new LocalVar(varName, null, className, lineNumber);
			if(insHandle.getInstruction() instanceof IINC){
				if(!readVars.contains(var)){
					return new VarOp(var, Variable.READ);					
				}
				if(!writtenVars.contains(var)){
					return new VarOp(var, Variable.WRITTEN);				
				}
			}
			else if(insHandle.getInstruction() instanceof LoadInstruction){
				if(!readVars.contains(var)){
					return new VarOp(var, Variable.READ);						
				}
			}
			else if(insHandle.getInstruction() instanceof StoreInstruction){
				if(!writtenVars.contains(var)){
					return new VarOp(var, Variable.WRITTEN);					
				}
			}
		}
		else if(insHandle.getInstruction() instanceof ArrayInstruction){
			ArrayInstruction aIns = (ArrayInstruction)insHandle.getInstruction();
			String typeSig = aIns.getType(pool).getSignature();
			String typeName = SignatureUtils.signatureToName(typeSig);
			
			List<InstructionHandle> previousInstructions = findPreviousInstructions(insHandle.getPosition(), code);
			if(insHandle.getInstruction().getName().toLowerCase().contains("load")){
				Variable var0 = parseArrayName(method, previousInstructions);
//				System.currentTimeMillis();
				if(var0!=null){
					String readArrayElement = var0.getName();
					ArrayElementVar var = new ArrayElementVar(readArrayElement, typeName, null);
					if(!readVars.contains(var)){
						return new VarOp(var, Variable.READ);												
					}
				}
			}
			else if(insHandle.getInstruction().getName().toLowerCase().contains("store")){
				Variable var0 = parseArrayName(method, previousInstructions);
				if(var0!=null){
					String writtenArrayElement = var0.getName();
					ArrayElementVar var = new ArrayElementVar(writtenArrayElement, typeName, null);
					if(!writtenVars.contains(var)){
						return new VarOp(var, Variable.WRITTEN);											
					}
				}
			}
		}
		else if(insHandle.getInstruction() instanceof ConstantPushInstruction){
			ConstantPushInstruction cpIns = (ConstantPushInstruction)(insHandle.getInstruction());
			Number num = cpIns.getValue();
			String typeSig = cpIns.getType(pool).getSignature();
			String typeName = SignatureUtils.signatureToName(typeSig);
			String value = num.toString();
			Variable var = new ConstantVar(value, typeName);
			return new VarOp(var, Variable.READ);
		}
		
		return null;
	}
	
	/**
	 * Scan the previous instructions to look for which variable used before stands for the array
	 * name. The basic idea is that, given an array instruction AINS such as aaload or aastore, we find
	 * its nearest previous instruction handling an array variable. The name of handled array variable is 
	 * assumed to be the name of the given array. However, we should take care that the instructions
	 * popping an array out of frame stack. If we detect k such instructions, the name of variable 
	 * handled by k-th instruction from AINS should be the name we need. We record k in a local variable
	 * <code>popArrayTime</code>. 
	 *  
	 * @param method
	 * @param previousInstructions
	 * @return
	 */
	private Variable parseArrayName(Method method, List<InstructionHandle> previousInstructions){
		
		int popArrayTime = 0;
		
		Code code = method.getCode();
		ConstantPoolGen pool = new ConstantPoolGen(code.getConstantPool());
		for(int i=previousInstructions.size()-1; i>=0; i--){
			InstructionHandle insHandle = previousInstructions.get(i);
			Instruction ins = insHandle.getInstruction();
			if(ins instanceof FieldInstruction){
				FieldInstruction fIns = (FieldInstruction)ins;
				String declaringType = fIns.getReferenceType(pool).getSignature();
				boolean isStatic = insHandle.getInstruction().getName().toLowerCase().contains("static");
				String fieldName = fIns.getFieldName(pool);
				String type = fIns.getFieldType(pool).getSignature();
				if(type.contains("[")){
					if(fIns.getName().contains("get")){
						if(popArrayTime==0){
							type = SignatureUtils.signatureToName(type);
							return new FieldVar(isStatic, fieldName, type, declaringType);											
						}
						else{
							popArrayTime--;
						}
					}
					else{
						popArrayTime++;
					}
				}
			}
			else if(ins instanceof LocalVariableInstruction){
				LocalVariableInstruction lIns = (LocalVariableInstruction)ins;
				int localVarIndex = lIns.getIndex();
				LocalVariable localVariable = code.getLocalVariableTable().getLocalVariable(localVarIndex, insHandle.getPosition());
				if(localVariable==null){
					continue;
				}
				String type = localVariable.getSignature();
				if(type.contains("[")){
					if(lIns.getName().contains("load")){
						if(popArrayTime==0){
							int varIndex = localVariable.getNameIndex();
							ConstantPool pool0 = localVariable.getConstantPool();
							Constant cons = pool0.getConstant(varIndex);
							String varaibleName =((ConstantUtf8) cons).getBytes();
							return new LocalVar(varaibleName, type, className, lineNumber);							
						}
						else{
							popArrayTime--;
						}
					}
					else if(lIns.getName().contains("store")){
						popArrayTime++;
					}
				}
				else if(type.equals("Ljava/lang/Object;")){
					int varIndex = lIns.getIndex();
					LocalVariable lVar = method.getLocalVariableTable().getLocalVariable(varIndex, insHandle.getPosition());
					if(lVar != null){
						type = lVar.getSignature();
						if(type.contains("[")){
							if(lIns.getName().contains("load")){
								if(popArrayTime==0){
									return new ArrayElementVar(lVar.getName(), type, null);									
								}
								else{
									popArrayTime--;
								}
							}
							else if(lIns.getName().contains("store")){
								popArrayTime++;
							}
						}						
					}
				}
			}
			else if(ins instanceof ArrayInstruction){
				ArrayInstruction aIns = (ArrayInstruction)ins;
				String type = aIns.getType(pool).getSignature();
				if(type.contains("[")){
					
					List<InstructionHandle> preIns = new ArrayList<>();
					for(InstructionHandle handle: previousInstructions){
						if(handle.getPosition()<insHandle.getPosition()){
							preIns.add(insHandle);
						}
					}
					
					Variable var = parseArrayName(method, preIns);
					if(null != var){
						if(aIns.getName().contains("load")){
							if(popArrayTime==0){
								String varName = var.getName();
								return new ArrayElementVar(varName, var.getType(), null);							
							}
							else{
								popArrayTime--;
							}
						}
					}		
				}
				
				popArrayTime++;
			}
			else if(ins instanceof InvokeInstruction){
				InvokeInstruction iIns = (InvokeInstruction)ins;
				Type type = iIns.getReturnType(pool);
				//TODO handle the returned array by method invocation.
			}
		}
		
		return null;
	}

	public List<Variable> getReadVars() {
		return readVars;
	}

	public void setReadVars(List<Variable> readVars) {
		this.readVars = readVars;
	}

	public List<Variable> getWrittenVars() {
		return writtenVars;
	}

	public void setWrittenVars(List<Variable> writtenVars) {
		this.writtenVars = writtenVars;
	}

	public Variable getReturnedVar() {
		return returnedVar;
	}

	public void setReturnedVar(Variable returnedVar) {
		this.returnedVar = returnedVar;
	}

	public List<InstructionHandle> getInstructionList() {
		return instructionList;
	}

	public void setInstructionList(List<InstructionHandle> instructionList) {
		this.instructionList = instructionList;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}
