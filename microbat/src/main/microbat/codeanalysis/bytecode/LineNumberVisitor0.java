package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.StoreInstruction;

import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import sav.common.core.utils.SignatureUtils;
import sav.strategies.dto.AppJavaClassPath;

public class LineNumberVisitor0 extends ByteCodeVisitor {
	private int lineNumber;
//	private int offset;
	private String className;
	private AppJavaClassPath appJavaClassPath;
	
	
	
	public LineNumberVisitor0(int lineNumber, String className, int offset, AppJavaClassPath appJavaClassPath) {
		super();
		this.lineNumber = lineNumber;
		this.className = className;
//		this.offset = offset;
		this.appJavaClassPath = appJavaClassPath;
	}

	private List<Variable> readVars = new ArrayList<>();
	private List<Variable> writtenVars = new ArrayList<>();
	private Variable returnedVar;
	
	public void visitMethod(Method method){
		Code code = method.getCode();
		
		if(code == null){
			return;
		}
		
		if(isMethodContainLineNumber(method, lineNumber)){
			InstructionList list = new InstructionList(code.getCode());
			List<InstructionHandle> insHandles = findCorrespondingInstructions(lineNumber, code);
			
			for(InstructionHandle insHandle: insHandles){
				parseReadWrittenVariable(insHandle, method, appJavaClassPath);					
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
				Variable var = parseVariableName(method, previousInstructions);
				returnedVar = var;
			}
		}
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
	
	protected void parseReadWrittenVariable(InstructionHandle insHandle, Method method, AppJavaClassPath appJavaClassPath) {
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
				
				FieldVar var = new FieldVar(isStatic, fullFieldName, type);
				
				if(rw){
					if(!readVars.contains(var)){
						readVars.add(var);															
					}
				}
				else{
					if(!writtenVars.contains(var)){
						writtenVars.add(var);						
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
					readVars.add(var);					
				}
				if(!writtenVars.contains(var)){
					writtenVars.add(var);					
				}
			}
			else if(insHandle.getInstruction() instanceof LoadInstruction){
				if(!readVars.contains(var)){
					readVars.add(var);					
				}
			}
			else if(insHandle.getInstruction() instanceof StoreInstruction){
				if(!writtenVars.contains(var)){
					writtenVars.add(var);					
				}
			}
		}
		else if(insHandle.getInstruction() instanceof ArrayInstruction){
			ArrayInstruction aIns = (ArrayInstruction)insHandle.getInstruction();
			String typeSig = aIns.getType(pool).getSignature();
			String typeName = SignatureUtils.signatureToName(typeSig);
			
			List<InstructionHandle> previousInstructions = findPreviousInstructions(lineNumber, insHandle.getPosition(), code);
			if(insHandle.getInstruction().getName().toLowerCase().contains("load")){
				Variable var0 = parseVariableName(method, previousInstructions);
				if(var0!=null){
					String readArrayElement = var0.getName();
					ArrayElementVar var = new ArrayElementVar(readArrayElement, typeName);
					if(!readVars.contains(var)){
						readVars.add(var);												
					}
				}
			}
			else if(insHandle.getInstruction().getName().toLowerCase().contains("store")){
				Variable var0 = parseVariableName(method, previousInstructions);
				if(var0!=null){
					String writtenArrayElement = var0.getName();
					ArrayElementVar var = new ArrayElementVar(writtenArrayElement, typeName);
					if(!writtenVars.contains(var)){
						writtenVars.add(var);											
					}
				}
			}
		}
	}
	
	private Variable parseVariableName(Method method, List<InstructionHandle> previousInstructions){
		Code code = method.getCode();
		ConstantPoolGen pool = new ConstantPoolGen(code.getConstantPool());
		for(int i=previousInstructions.size()-1; i>=0; i--){
			InstructionHandle insHandle = previousInstructions.get(i);
			//TODO
			Instruction ins = insHandle.getInstruction();
			if(ins instanceof FieldInstruction){
				FieldInstruction fIns = (FieldInstruction)ins;
				boolean isStatic = insHandle.getInstruction().getName().toLowerCase().contains("static");
				String fieldName = fIns.getFieldName(pool);
				String type = fIns.getFieldType(pool).getSignature();
				type = SignatureUtils.signatureToName(type);
				return new FieldVar(isStatic, fieldName, type);
			}
			else if(ins instanceof LocalVariableInstruction){
				LocalVariableInstruction lIns = (LocalVariableInstruction)ins;
				int varIndex = lIns.getIndex();
				return new LocalVar("$" + varIndex, null, className, lineNumber);
			}
			else if(ins instanceof ArrayInstruction){
				List<InstructionHandle> preIns = new ArrayList<>();
				for(InstructionHandle handle: previousInstructions){
					if(handle.getPosition()<insHandle.getPosition()){
						preIns.add(insHandle);
					}
				}
				
				Variable var = parseVariableName(method, preIns);
				if(null != var){
					String varName = var.getName();
					return new ArrayElementVar(varName, var.getType());
				}
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
}
