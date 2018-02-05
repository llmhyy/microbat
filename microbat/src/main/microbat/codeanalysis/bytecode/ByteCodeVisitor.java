package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.generic.ArrayInstruction;
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

import microbat.codeanalysis.ast.LoopHeadParser;
import microbat.codeanalysis.ast.SourceScopeParser;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.SourceScope;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.util.JavaUtil;
import sav.common.core.utils.SignatureUtils;
import sav.strategies.dto.AppJavaClassPath;

public class ByteCodeVisitor extends EmptyVisitor{
	@SuppressWarnings("rawtypes")
	protected List<InstructionHandle> findCorrespondingInstructions(int lineNumber, Code code) {
		List<InstructionHandle> correspondingInstructions = new ArrayList<>();
		
		InstructionList list = new InstructionList(code.getCode());
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			InstructionHandle insHandle = (InstructionHandle) iter.next();
			int instructionLine = code.getLineNumberTable().getSourceLine(insHandle.getPosition());
			
			if(instructionLine == lineNumber){
				correspondingInstructions.add(insHandle);
			}
		}
		
		return correspondingInstructions;
	}
	
	@SuppressWarnings("rawtypes")
	protected List<InstructionHandle> findCorrespondingInstructions(Code code) {
		List<InstructionHandle> correspondingInstructions = new ArrayList<>();
		
		InstructionList list = new InstructionList(code.getCode());
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			InstructionHandle insHandle = (InstructionHandle) iter.next();
			correspondingInstructions.add(insHandle);
		}
		
		return correspondingInstructions;
	}

	protected void parseReadWrittenVariable(BreakPoint point, List<InstructionHandle> correspondingInstructions, 
			Code code, CFG cfg, AppJavaClassPath appJavaClassPath) {
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(point.getDeclaringCompilationUnitName(), appJavaClassPath);

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
							ReadFieldRetriever rfRetriever = new ReadFieldRetriever(cu, point.getLineNumber(), fullFieldName);
							cu.accept(rfRetriever);
							if(rfRetriever.fullFieldName != null){
								fullFieldName = rfRetriever.fullFieldName;															
							}
						}
						else{
							WrittenFieldRetriever wfRetriever = new WrittenFieldRetriever(cu, point.getLineNumber(), fullFieldName);
							cu.accept(wfRetriever);
							if(wfRetriever.fullFieldName != null){
								fullFieldName = wfRetriever.fullFieldName;
							}
						}
					}
					
					String declaringType = gIns.getReferenceType(pool).getSignature();
					FieldVar var = new FieldVar(isStatic, fullFieldName, type, declaringType);
					
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
				
				if(variable != null){
					if(variable.getName().equals("this") && !isThisAppearOnRightHandSide(point, appJavaClassPath)){
						continue;
					}
					
					LocalVar var = new LocalVar(variable.getName(), SignatureUtils.signatureToName(variable.getSignature()), 
							point.getDeclaringCompilationUnitName(), point.getLineNumber());
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
					ReadArrayElementRetriever raeRetriever = new ReadArrayElementRetriever(cu, point.getLineNumber(), typeName);
					cu.accept(raeRetriever);
					for(String readArrayElement: raeRetriever.arrayElementNameList){
						ArrayElementVar var = new ArrayElementVar(readArrayElement, typeName, null);
						point.addReadVariable(var);											
					}
				}
				else if(insHandle.getInstruction().getName().toLowerCase().contains("store")){
					WrittenArrayElementRetriever waeRetriever = new WrittenArrayElementRetriever(cu, point.getLineNumber(), typeName);
					cu.accept(waeRetriever);
					String writtenArrayElement = waeRetriever.arrayElementName;
					if(writtenArrayElement != null){
						ArrayElementVar var = new ArrayElementVar(writtenArrayElement, typeName, null);
						point.addWrittenVariable(var);
					}
				}
			}
			else if(insHandle.getInstruction() instanceof ReturnInstruction){
				point.setReturnStatement(true);
			}
			
			CFGNode cfgNode = cfg.findNode(insHandle.getPosition());
			
			if(cfgNode.isBranch()){
				setConditionalScope(insHandle, point, cfg, code, cu);
			}
		}
	}
	
	private boolean isThisAppearOnRightHandSide(BreakPoint point, AppJavaClassPath appJavaClassPath) {
//		CompilationUnit cu = JavaUtil.findCompiltionUnitBySourcePath(point.getFullJavaFilePath(), 
//				point.getDeclaringCompilationUnitName());
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(point.getDeclaringCompilationUnitName(), appJavaClassPath);
		ThisChecker checker = new ThisChecker(cu, point);
		cu.accept(checker);
		return checker.containsValidThis;
	}


	private void setConditionalScope(InstructionHandle handle, BreakPoint point, CFG cfg, Code code, 
			CompilationUnit cu){
		point.setConditional(true);
		
		CFGNode node = cfg.findNode(handle);
		List<CFGNode> scopeNodes = node.getControlDependentees();
		List<ClassLocation> list = new ArrayList<>();
		for(CFGNode n: scopeNodes){
			ClassLocation location = transferToLocation(n.getInstructionHandle(), code, point);
			if(!list.contains(location)){
				list.add(location);
			}
		}
		
		LoopHeadParser lhParser = new LoopHeadParser(cu, point);
		cu.accept(lhParser);
		
		List<ClassLocation> confirmedList = lhParser.extractLocation();
		if(null != confirmedList){
			for(ClassLocation location: confirmedList){
				if(!list.contains(location)){
					list.add(location);
				}
			}			
		}
		
		point.mergeControlScope(new ControlScope(list, lhParser.isLoop()));
		if(lhParser.isLoop()){
			SourceScopeParser parser = new SourceScopeParser();
			SourceScope sourceScope = parser.parseScope(cu, point.getLineNumber());
			point.setLoopScope(sourceScope);
		}
	}

	private ClassLocation transferToLocation(InstructionHandle insHandle, Code code, BreakPoint point) {
		LineNumberTable table = code.getLineNumberTable();
		int sourceLine = table.getSourceLine(insHandle.getPosition());
		
		ClassLocation location = new ClassLocation(point.getClassCanonicalName(), null, sourceLine);
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
	protected LocalVariable findOptimalVariable(InstructionHandle insHandle, int variableIndex, LocalVariableTable localVariableTable) {
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

	protected JavaClass javaClass;
	
	public void setJavaClass(JavaClass clazz) {
		this.javaClass = clazz;
	}

	public JavaClass getJavaClass() {
		return javaClass;
	}
	
}
