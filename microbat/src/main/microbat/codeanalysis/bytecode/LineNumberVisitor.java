package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.StoreInstruction;
import org.eclipse.jdt.core.dom.CompilationUnit;

import microbat.codeanalysis.ast.ConditionalScopeParser;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.Scope;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.util.JavaUtil;
import sav.common.core.utils.SignatureUtils;

/**
 * collect information such as method signature, and read/written variables in a certain line.
 * 
 * @author Yun Lin
 *
 */
public class LineNumberVisitor extends EmptyVisitor {
	
	private BreakPoint breakPoint;
	
	public LineNumberVisitor(BreakPoint breakPoint) {
		super();
		this.breakPoint = breakPoint;
	}

	@SuppressWarnings("rawtypes")
	public void visitMethod(Method method){
		Code code = method.getCode();
		
		if(code != null){
			List<int[]> rangeList = searchOffsetRange(this.breakPoint.getLineNo(), code);
			
			if(!rangeList.isEmpty()){
//			if(breakPoint.getLineNo() == 60){
//				System.currentTimeMillis();
//			}
				String methodSig = breakPoint.getClassCanonicalName() + "." + method.getName() + method.getSignature();
				breakPoint.setMethodSign(methodSig);
				
				List<InstructionHandle> correspondingInstructions = new ArrayList<>();
				
				InstructionList list = new InstructionList(code.getCode());
				Iterator iter = list.iterator();
				while(iter.hasNext()){
					InstructionHandle instructionHandle = (InstructionHandle)iter.next();
					int offset = instructionHandle.getPosition();
					
					if(isInRange(offset, rangeList)){
						correspondingInstructions.add(instructionHandle);
					}
					
				}
				
				parseReadWrittenVariable(correspondingInstructions, code);
			}
		}
		
		
    }

	private boolean isInRange(int offset, List<int[]> rangeList) {
		for(int[] range: rangeList){
			if(range[0]<=offset && (offset<=range[1] || range[1]==-1)){
				return true;
			}
		}
		return false;
	}

	private void parseReadWrittenVariable(List<InstructionHandle> correspondingInstructions, Code code) {
		BreakPoint point = this.breakPoint;
		
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(point.getClassCanonicalName());
		ConstantPoolGen pool = new ConstantPoolGen(code.getConstantPool()); 
		 
		for(int i=0; i<correspondingInstructions.size(); i++){
			InstructionHandle insHandle = correspondingInstructions.get(i);
			if(insHandle.getInstruction() instanceof FieldInstruction){
				FieldInstruction gIns = (FieldInstruction)insHandle.getInstruction();
				String fullFieldName = gIns.getFieldName(pool);
				if(fullFieldName != null){
					/** rw being true means read; and rw being false means write. **/
					boolean rw = insHandle.getInstruction().getName().toLowerCase().contains("get");;
					boolean isStatic = insHandle.getInstruction().getName().toLowerCase().contains("static");
					
					String type = gIns.getFieldType(pool).getSignature();
					type = SignatureUtils.signatureToName(type);
					
					if(!fullFieldName.contains("$")){
						/**
						 * The reason for additionally use ReadFileRetriever and WrittenFieldRetriever is that
						 * I want to get "a.attr1.attr2" instead of "attr2".
						 */
						if(rw){
							ReadFieldRetriever rfRetriever = new ReadFieldRetriever(cu, point.getLineNo(), fullFieldName);
							cu.accept(rfRetriever);
							fullFieldName = rfRetriever.fullFieldName;							
						}
						else{
							WrittenFieldRetriever wfRetriever = new WrittenFieldRetriever(cu, point.getLineNo(), fullFieldName);
							cu.accept(wfRetriever);
							fullFieldName = wfRetriever.fullFieldName;
						}
					}
					
					FieldVar var = new FieldVar(isStatic, fullFieldName, type);
					
					if(rw){
						point.addReadVariable(var);										
					}
					else{
						point.addWrittenVariable(var);
					}
				}
			}
			else if(insHandle.getInstruction() instanceof LocalVariableInstruction){
				LocalVariableInstruction lIns = (LocalVariableInstruction)insHandle.getInstruction();
				LocalVariable variable = code.getLocalVariableTable().getLocalVariable(lIns.getIndex(), insHandle.getPosition());
				if(variable == null){
//					variable = code.getLocalVariableTable().getLocalVariable(lIns.getIndex());
					variable = findOptimalVariable(insHandle, lIns.getIndex(), code.getLocalVariableTable());
					System.currentTimeMillis();
				}
				
				if(variable != null && !variable.getName().equals("this")){
					LocalVar var = new LocalVar(variable.getName(), SignatureUtils.signatureToName(variable.getSignature()), 
							point.getDeclaringCompilationUnitName(), point.getLineNo());
					if(insHandle.getInstruction() instanceof IINC){
						point.addReadVariable(var);
						point.addWrittenVariable(var);
					}
					else if(insHandle.getInstruction() instanceof LoadInstruction){
						point.addReadVariable(var);
					}
					else if(insHandle.getInstruction() instanceof StoreInstruction){
						point.addWrittenVariable(var);
					}
				}
			}
			else if(insHandle.getInstruction() instanceof ArrayInstruction){
				ArrayInstruction aIns = (ArrayInstruction)insHandle.getInstruction();
				String typeSig = aIns.getType(pool).getSignature();
				String typeName = SignatureUtils.signatureToName(typeSig);
				
				if(insHandle.getInstruction().getName().toLowerCase().contains("load")){
					ReadArrayElementRetriever raeRetriever = new ReadArrayElementRetriever(cu, point.getLineNo(), typeName);
					cu.accept(raeRetriever);
					String readArrayElement = raeRetriever.arrayElementName;
					if(readArrayElement != null){
						ArrayElementVar var = new ArrayElementVar(readArrayElement, typeName);
						point.addReadVariable(var);											
					}
				}
				else if(insHandle.getInstruction().getName().toLowerCase().contains("store")){
					WrittenArrayElementRetriever waeRetriever = new WrittenArrayElementRetriever(cu, point.getLineNo(), typeName);
					cu.accept(waeRetriever);
					String writtenArrayElement = waeRetriever.arrayElementName;
					if(writtenArrayElement != null){
						ArrayElementVar var = new ArrayElementVar(writtenArrayElement, typeName);
						point.addWrittenVariable(var);
					}
				}
			}
			else if(insHandle.getInstruction() instanceof ReturnInstruction){
				point.setReturnStatement(true);
			}
			else if(insHandle.getInstruction() instanceof BranchInstruction){
				setConditionalScope(cu, point);
				
				ClassLocation target0 = transferToLocation(insHandle.getNext(), code);
				if(target0 != null){
					point.addTarget(target0);					
				}
				else{
					System.currentTimeMillis();
				}
				
				BranchInstruction bIns = (BranchInstruction)insHandle.getInstruction();
				InstructionHandle ins1 = bIns.getTarget();
				ClassLocation target1 = transferToLocation(ins1, code);
				point.addTarget(target1);					
			}
		}
		
		
	}

	private ClassLocation transferToLocation(InstructionHandle insHandle, Code code) {
		LineNumberTable table = code.getLineNumberTable();
		int sourceLine = table.getSourceLine(insHandle.getPosition());
		
		ClassLocation location = new ClassLocation(this.breakPoint.getClassCanonicalName(), null, sourceLine);
		return location;
	}

	/**
	 * Based on my observation, if an instruction stores a value into a local variable, the start position of this instruction
	 * will be smaller than the start PC of this local variable. In other words, the instruction is out of the scope of this
	 * local variable, which may cause me to miss-find the correct variable. The diff will be between 1 or 2. <p>
	 * 
	 * In order to address this issue, I searched all the variables in local variable table and return the variable with smallest
	 * diff with the start position of the given instruciton.
	 * @param insHandle
	 * @param variableIndex
	 * @param localVariableTable
	 * @return
	 */
	private LocalVariable findOptimalVariable(InstructionHandle insHandle, int variableIndex, LocalVariableTable localVariableTable) {
		LocalVariable bestVar = null;
		int diff = -1;
		
		for(LocalVariable var: localVariableTable.getLocalVariableTable()){
			if(var.getIndex() == variableIndex){
				if(diff == -1){
					bestVar = var;
					diff = Math.abs(insHandle.getPosition() - var.getStartPC());
				}
				else{
					int newDiff = Math.abs(insHandle.getPosition() - var.getStartPC());
					if(newDiff < diff){
						bestVar = var;
						diff = newDiff;
					}
				}
			}
		}
		
		return bestVar;
	}

	private List<int[]> searchOffsetRange(int lineNumber, Code obj) {
		
		List<int[]> rangeList = new ArrayList<>();
		
		LineNumberTable table = obj.getLineNumberTable();
		LineNumber[] lineNumbers = table.getLineNumberTable();
		for(int i=0; i<lineNumbers.length; i++){
			LineNumber lineNum = lineNumbers[i];
			if(lineNum.getLineNumber() == lineNumber){
				int startPC = lineNum.getStartPC();
				
				int endPC;
				if(i >= lineNumbers.length-1){
					endPC = -1;
				}
				else{
					endPC = lineNumbers[i+1].getStartPC() - 1;
				}
				
				int[] range = new int[]{startPC, endPC};
				rangeList.add(range);
			}
		}
		
		return rangeList;
	}
	
	private void setConditionalScope(CompilationUnit cu, BreakPoint point){
		point.setConditional(true);
		ConditionalScopeParser parser = new ConditionalScopeParser();
		Scope conditionScope = parser.parseScope(cu, point.getLineNo());
		point.setConditionScope(conditionScope);
	}
}
