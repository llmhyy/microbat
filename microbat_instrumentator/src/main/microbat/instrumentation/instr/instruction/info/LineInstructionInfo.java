package microbat.instrumentation.instr.instruction.info;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;

import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.instrumentation.AgentConstants;
import sav.common.core.utils.StringUtils;

public class LineInstructionInfo {
	protected int line;
	protected InstructionHandle lineNumberInsn;
	protected List<InstructionHandle> lineInsns;
	protected LocalVariableTable localVarTable;
	protected ConstantPoolGen constPool;
	protected LineNumberTable lineNumberTable;
	protected List<RWInstructionInfo> rwInsructionInfo;
	protected List<InstructionHandle> invokeInsns;
	protected List<InstructionHandle> returnInsns;
	private List<InstructionHandle> exitInsns; 
	private boolean hasExceptionTarget;
	
	public LineInstructionInfo() {
		// 
	}
	
	public LineInstructionInfo(String locId, ConstantPoolGen constPool, Method method, MethodGen methodGen, Set<InstructionHandle> exceptionTargets, LineNumberGen lineGen, CFG cfg,
			boolean isAppClass) {
		this.line = lineGen.getSourceLine();
		this.lineNumberInsn = lineGen.getInstruction();
		this.localVarTable = method.getLocalVariableTable();
		this.constPool = constPool;
		this.lineNumberTable = method.getLineNumberTable();
		InstructionList insnList = methodGen.getInstructionList();
		lineInsns = findCorrespondingInstructions(insnList , lineNumberTable, lineGen.getSourceLine());
		rwInsructionInfo = extractRWInstructions(locId, isAppClass);
		invokeInsns = extractInvokeInstructions(lineInsns);
		returnInsns = extractReturnInstructions(lineInsns);
		exitInsns = extractExitInsns(cfg, lineInsns);
		for (InstructionHandle insn : lineInsns) {
			if (exceptionTargets.remove(insn)) {
				hasExceptionTarget = true;
			}
		}
	}

	private List<InstructionHandle> extractExitInsns(CFG cfg, List<InstructionHandle> lineInsns) {
		List<InstructionHandle> list = new ArrayList<>();
		for(InstructionHandle handle: lineInsns){
			for(CFGNode node: cfg.getExitList()){
				if(node.getInstructionHandle().getPosition()==handle.getPosition()){
					list.add(handle);
					break;
				}
			}
		}
		return list;
	}

	public List<RWInstructionInfo> getRWInstructions() {
		return rwInsructionInfo;
	}
	
	public ConstantPoolGen getConstPool() {
		return constPool;
	}

	protected List<RWInstructionInfo> extractRWInstructions(String locId, boolean isAppClass) {
		List<RWInstructionInfo> rwInsns = new ArrayList<>();
		for (InstructionHandle insnHandler : lineInsns) {
			Instruction insn = insnHandler.getInstruction();
			/* 
			 * filter instrumentation
			 * For external libraries, only store for the case of 
			 * Written Fields or ArrayElement.
			 * */
			if (!(isAppClass || (insn instanceof FieldInstruction)
								|| ((insn instanceof ArrayInstruction)))) {
				continue;
			}
			
			/* collect instruction info */
			if (insn instanceof FieldInstruction) {
				FieldInstruction fieldInsn = (FieldInstruction) insn;
				ReferenceType refType = fieldInsn.getReferenceType(constPool);
				FieldInstructionInfo info = new FieldInstructionInfo(insnHandler, line);
				info.setFieldBcType(fieldInsn.getFieldType(constPool));
				info.setRefType(refType.getSignature());
				info.setVarStackSize(refType.getSize());
				info.setVarType(fieldInsn.getSignature(constPool));
				info.setVarName(fieldInsn.getFieldName(constPool));
				info.setIsStore(existIn(insn.getOpcode(), Const.PUTFIELD, Const.PUTSTATIC));
				rwInsns.add(info);
			} else if (insn instanceof ArrayInstruction) {
				ArrayInstructionInfo info = new ArrayInstructionInfo(insnHandler, line);
				ArrayInstruction arrInsn = (ArrayInstruction) insn;
				Type eleType = arrInsn.getType(constPool);
				info.setElementType(eleType);
				info.setVarType(eleType.getSignature());
				info.setVarStackSize(eleType.getSize());
				info.setIsStore(existIn(insn.getOpcode(), Const.AASTORE, Const.FASTORE, Const.LASTORE,
						Const.CASTORE, Const.IASTORE, Const.BASTORE, Const.SASTORE, Const.DASTORE));
				rwInsns.add(info);
			} else if (insn instanceof LocalVariableInstruction) {
				LocalVariableInstruction localVarInsn = (LocalVariableInstruction) insn;
				LocalVariable localVar = null;
				if (localVarTable != null) {
					localVar = localVarTable.getLocalVariable(localVarInsn.getIndex(),
							insnHandler.getPosition() + insn.getLength());
				}
				if (localVar == null) {
//					AgentLogger.debug(String.format("Warning: Cannot find localVar with (index = %s, pc = %s) at %s",
//							localVarInsn.getIndex(), insnHandler.getPosition(), locId));
					Type type = localVarInsn.getType(constPool);
					String localVarName = String.format("%s:%s", locId, insnHandler.getPosition());
					String localVarTypeSign = type.getSignature();
					LocalVarInstructionInfo info = new LocalVarInstructionInfo(insnHandler, line, localVarName, localVarTypeSign);
					info.setIsStore(existIn(((LocalVariableInstruction) insn).getCanonicalTag(), Const.FSTORE, Const.IINC, Const.DSTORE, Const.ASTORE,
							Const.ISTORE, Const.LSTORE));
					info.setVarStackSize(type.getSize());
					info.setVarScopeStartLine(AgentConstants.UNKNOWN_LINE);
					info.setVarScopeEndLine(AgentConstants.UNKNOWN_LINE);
					rwInsns.add(info);
					continue;
				} else {
					LocalVarInstructionInfo info = new LocalVarInstructionInfo(insnHandler, line, localVar.getName(), localVar.getSignature());
					info.setIsStore(existIn(((LocalVariableInstruction) insn).getCanonicalTag(), Const.FSTORE, Const.IINC, Const.DSTORE, Const.ASTORE,
							Const.ISTORE, Const.LSTORE));
					Type type = localVarInsn.getType(constPool);
					info.setVarStackSize(type.getSize());
					
					/**
					 * Lin Yun: provide an over-estimation for the scope of a local variable, note
					 * that, a local variable may have multiple start PC, we may need to consider
					 * all of them.
					 */
					int startPC = -1;
					int endPC = -1;
					for(LocalVariable v: localVarTable.getLocalVariableTable()){
						if(v.getIndex()==localVar.getIndex()){
							if(startPC==-1){
								startPC = v.getStartPC();
								endPC = startPC + v.getLength();
							}
							else{
								int sPC = v.getStartPC();
								int ePC = sPC + v.getLength();
								
								if(sPC < startPC){
									startPC = sPC;
								}
								
								if(ePC > endPC){
									endPC = ePC;
								}
							}
						}
					}
					
					info.setVarScopeStartLine(lineNumberTable.getSourceLine(startPC));
					info.setVarScopeEndLine(lineNumberTable.getSourceLine(endPC));
					rwInsns.add(info);
				}
			}
		}
		return rwInsns;
	}
	
	protected static boolean existIn(short opCode, short... checkOpCodes) {
		for (short checkOpCode : checkOpCodes) {
			if (opCode == checkOpCode) {
				return true;
			}
		}
		return false;
	}
	
	public List<InstructionHandle> getInvokeInstructions() {
		return invokeInsns;
	}
	
	public static List<InstructionHandle> findCorrespondingInstructions(InstructionList list, LineNumberTable lineTable,
			int lineNumber) {
		List<InstructionHandle> correspondingInstructions = new ArrayList<>();
		Iterator<?> iter = list.iterator();
		while (iter.hasNext()) {
			InstructionHandle insHandle = (InstructionHandle) iter.next();
			int instructionLine = lineTable.getSourceLine(insHandle.getPosition());
			if (instructionLine == lineNumber) {
				correspondingInstructions.add(insHandle);
			}
		}
		return correspondingInstructions;
	}
	
	protected static List<InstructionHandle> extractInvokeInstructions(List<InstructionHandle> insns) {
		List<InstructionHandle> invokeInsns = new ArrayList<>(3);
		for (InstructionHandle insnHandler : insns) {
			Instruction insn = insnHandler.getInstruction();
			if (insn instanceof InvokeInstruction) {
				invokeInsns.add(insnHandler);
			}
//			if (insn instanceof InvokeInstruction && 
//					(!(insn instanceof INVOKESPECIAL))) {
//				invokeInsns.add(insnHandler);
//			}
		}
		return invokeInsns;
	}
	
	protected List<InstructionHandle> extractReturnInstructions(List<InstructionHandle> lineInsns) {
		List<InstructionHandle> returnInsns = new ArrayList<>(1);
		for (InstructionHandle insnHandler : lineInsns) {
			Instruction insn = insnHandler.getInstruction();
			if ((insn instanceof ReturnInstruction)) {
				returnInsns.add(insnHandler);
			}
		}
		return returnInsns;
	}
	
	public List<InstructionHandle> getReturnInsns() {
		return returnInsns;
	}
	
	public int getLine() {
		return line;
	}

	public InstructionHandle getLineNumberInsn() {
		return lineNumberInsn;
	}

	public void dispose() {
		// free memory
		lineInsns = null;
		rwInsructionInfo = null;
		invokeInsns = null;
		returnInsns = null;
	}

	public boolean hasNoInstrumentation() {
		return rwInsructionInfo.isEmpty() && invokeInsns.isEmpty() && returnInsns.isEmpty();
	}

	public List<InstructionHandle> getExitInsns() {
		return exitInsns;
	}

	public boolean hasExceptionTarget() {
		return hasExceptionTarget;
	}

	public static List<InstructionHandle> getInstrInstructions(InstructionList list,
			LineNumberTable lineTable, int lineNumber) {
		List<InstructionHandle> result = new ArrayList<>();
		List<InstructionHandle> correspondingInsns = findCorrespondingInstructions(list, lineTable, lineNumber);
		for (InstructionHandle insnHandler : correspondingInsns) {
			Instruction insn = insnHandler.getInstruction();
			if ((insn instanceof FieldInstruction) || (insn instanceof LocalVariableInstruction)
					|| (insn instanceof ArrayInstruction) || (insn instanceof ReturnInstruction)
					|| (insn instanceof InvokeInstruction)) {
				result.add(insnHandler);
			}
		}
		return result;
	}
	
	public static List<LineInstructionInfo> buildLineInstructionInfos(ClassGen classGen, ConstantPoolGen constPool,
			MethodGen methodGen, Method method, boolean isAppClass, InstructionList insnList) {
		List<LineInstructionInfo> lineInsnInfos = new ArrayList<>();
		Set<InstructionHandle> exceptionTargets = collectExecptionTargets(methodGen);
		CFGConstructor cfgConstructor = new CFGConstructor();
		CFG cfg = cfgConstructor.constructCFG(method.getCode());

		Set<Integer> visitedLines = new HashSet<>();
		for (LineNumberGen lineGen : methodGen.getLineNumbers()) {
			if (!visitedLines.contains(lineGen.getSourceLine())) {
				String loc = StringUtils.dotJoin(classGen.getClassName(), method.getName(), lineGen.getSourceLine());
				lineInsnInfos.add(new LineInstructionInfo(loc, constPool, method, methodGen, exceptionTargets, lineGen, cfg, isAppClass));
				visitedLines.add(lineGen.getSourceLine());
			}
		}
		/* class does not include line number */
		if (visitedLines.isEmpty()) {
			String loc = classGen.getClassName() + "." + method.getName();
			lineInsnInfos.add(new UnknownLineInstructionInfo(loc, constPool, insnList, isAppClass));
		}
		return lineInsnInfos;
	}

	private static Set<InstructionHandle> collectExecptionTargets(MethodGen methodGen) {
		Set<InstructionHandle> targets = new HashSet<>();
		for (CodeExceptionGen exception : methodGen.getExceptionHandlers()) {
			targets.add(exception.getHandlerPC());
		}
		return targets;
	}

	public int getReadWriteInsnTotal(boolean isWritten) {
		int total = 0;
		for (RWInstructionInfo info : rwInsructionInfo) {
			if (info.isStoreInstruction() == isWritten) {
				total++;
			}
		}
		return total;
	}
}
