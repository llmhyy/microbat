package microbat.instrumentation.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.SWAP;
import org.apache.bcel.generic.Type;

import microbat.instrumentation.trace.data.ExecutionTracer;
import microbat.instrumentation.trace.example.FieldIndex;
import sav.common.core.SavRtException;

public class TraceInstrumenter {

	protected byte[] instrument(String className, byte[] classfileBuffer) throws Exception {
		ClassParser cp = new ClassParser(new java.io.ByteArrayInputStream(classfileBuffer), className);
		JavaClass jc = cp.parse();
		// First, make sure we have to instrument this class:
		if (!jc.isClass()) // could be an interface
			return null;
		ClassGen cg = new ClassGen(jc);
		ConstantPoolGen cpg = cg.getConstantPool();
		setMethodIndex(cpg);
		JavaClass newJC = null;
		for (Method meth : jc.getMethods()) {
			if (meth.isNative() || meth.isAbstract() || meth.getCode() == null) {
				continue; // Only instrument methods with code in them!
			}
			boolean changed = false;
			MethodGen mg = new MethodGen(meth, className, cpg);
			changed = instrumentMethod(cg, cpg, mg);
			if (changed) {
				cg.replaceMethod(meth, mg.getMethod());
			}
			newJC = cg.getJavaClass();
			newJC.setConstantPool(cpg.getFinalConstantPool());
		}
		System.out.println("===============================================");
		dumpClass(jc);
		System.out.println("=================NEW JAVACLASS==============================");
		dumpClass(newJC);
		if (newJC != null) {
			return newJC.getBytes();
		}
		return null;
	}
	
	private void dumpClass(JavaClass jc) {
		ClassGen cg = new ClassGen(jc);
		ConstantPoolGen cpg = cg.getConstantPool();
		System.out.println(jc.toString());
		for (Method meth : jc.getMethods()) {
			MethodGen mg = new MethodGen(meth, jc.getClassName(), cpg);
			System.out.println(mg.getName());
			System.out.println("==========================================");
			for (InstructionHandle ih : mg.getInstructionList()) {
				System.out.println(ih);
			}
		}
	}

	private int executionTracer_getTracer_idx;
	private int executionTracer_hitLine_idx;
	private int executionTracer_writeField_idx;
	private int executionTracer_writeLocalVar_idx;
	private boolean instrumentMethod(ClassGen classGen, ConstantPoolGen constPool, MethodGen methodGen) {
		InstructionList insnList = methodGen.getInstructionList();
		Map<LineNumberGen, List<InstructionHandle>> lineNoInsnsMap = getLineNoInsnsMap(methodGen, constPool);
		LocalVariableGen tracerVar = injectCodeInitTracer(methodGen);
		if (tracerVar == null) {
			throw new SavRtException("tracerVar == null");
		}
		for (LineNumberGen lineGen : lineNoInsnsMap.keySet()) {
			List<InstructionHandle> lineInsns =lineNoInsnsMap.get(lineGen);
			List<InstructionHandle> rwInsns = extractRWInstructions(lineInsns);
			if (rwInsns.isEmpty()) {
				injectCodeTracerHitLine(insnList, constPool, tracerVar, lineGen);
			} else {
				for (InstructionHandle rwInsn : rwInsns) {
					InstructionList newInsns = null;
					Instruction insn = rwInsn.getInstruction();
					if (insn instanceof FieldInstruction) {
						newInsns = getInjectCodeTracerRWriteField(constPool, tracerVar, (FieldInstruction)insn);
					} else if (insn instanceof ArrayInstruction) {
						newInsns = getInjectCodeTracerRWriteArray(constPool, tracerVar, (ArrayInstruction)insn);
					} else if (insn instanceof LocalVariableInstruction) {
						newInsns = getInjectCodeTracerRWriteLocalVar(constPool, tracerVar, (LocalVariableInstruction)insn);
					}
					if ((newInsns != null) && (newInsns.getLength() > 0)) {
						InstructionHandle pos = insnList.insert(rwInsn, newInsns);
						updateTargeters(rwInsn, pos);
						newInsns.dispose();
					}
				}
			}
		}
		return true;
	}
	
	private InstructionList getInjectCodeTracerRWriteField(ConstantPoolGen constPool, LocalVariableGen tracerVar, FieldInstruction insn) {
		InstructionList newInsns = new InstructionList();
		if (insn instanceof PUTFIELD) {
			Type fieldType = insn.getFieldType(constPool);
			if (fieldType instanceof ReferenceType) {
				int fieldIndex = getFieldIndex(constPool, insn);
				newInsns.append(new DUP()); // Duplicates object and value
				newInsns.append(new ALOAD(tracerVar.getIndex()));
				newInsns.append(new SWAP());
				newInsns.append(new INVOKEVIRTUAL(executionTracer_writeField_idx)); // record hit
				return newInsns;
			} else if (fieldType instanceof BasicType) {
				
			}
		} else if (insn instanceof PUTSTATIC) {
			
		} else if (insn instanceof GETFIELD) {
			
		} else if (insn instanceof GETSTATIC) {
			
		}
		return newInsns;
	}
	
	private InstructionList getInjectCodeTracerRWriteLocalVar(ConstantPoolGen constPool, LocalVariableGen tracerVar,
			LocalVariableInstruction insn) {
		InstructionList newInsns = new InstructionList();
		if (insn instanceof ASTORE) {
			ASTORE storeInsn = (ASTORE) insn;
			newInsns.append(new DUP());
			newInsns.append(new ALOAD(tracerVar.getIndex()));
			newInsns.append(new SWAP());
			newInsns.append(new PUSH(constPool, storeInsn.getIndex()));
			newInsns.append(new INVOKEVIRTUAL(executionTracer_writeLocalVar_idx));
		}
		return newInsns;
	}

	private InstructionList getInjectCodeTracerRWriteArray(ConstantPoolGen constPool, LocalVariableGen tracerVar,
			ArrayInstruction insn) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static int getFieldIndex(ConstantPoolGen cpg, FieldInstruction putfield) {
		ObjectType refType = (ObjectType) putfield.getReferenceType(cpg);
		int fieldIndex = FieldIndex.getFieldIndex(refType.getClassName(), putfield.getFieldName(cpg));
		return cpg.addInteger(fieldIndex); // Is now a constant-pool ref
	}

	private void injectCodeTracerHitLine(InstructionList insnList, ConstantPoolGen constPool,
			LocalVariableGen tracerVar, LineNumberGen lineGen) {
		InstructionList newInsns = new InstructionList();
		newInsns.append(new ALOAD(tracerVar.getIndex()));
		newInsns.append(new PUSH(constPool, lineGen.getSourceLine()));
		newInsns.append(new INVOKEVIRTUAL(executionTracer_hitLine_idx));
		InstructionHandle pos = insnList.insert(lineGen.getInstruction(), newInsns);
		updateTargeters(lineGen.getInstruction(), pos);
		newInsns.dispose();
	}

	private LocalVariableGen injectCodeInitTracer(MethodGen methodGen) {
		InstructionList insnList = methodGen.getInstructionList();
		InstructionHandle startInsn = insnList.getStart();
		if (startInsn == null) {
			return null;
		}
		LocalVariableGen tracerVar = methodGen.addLocalVariable("$tracer", Type.getType(ExecutionTracer.class),
				insnList.getStart(), insnList.getEnd());
		InstructionList newInsns = new InstructionList();
		newInsns.append(new INVOKESTATIC(executionTracer_getTracer_idx));
		newInsns.append(new ASTORE(tracerVar.getIndex()));
		/* update */
		InstructionHandle insertPos = insnList.insert(startInsn, newInsns);
		updateTargeters(startInsn, insertPos);
		newInsns.dispose();
		return tracerVar;
	}

	private List<InstructionHandle> extractRWInstructions(List<InstructionHandle> insnList) {
		List<InstructionHandle> rwInsns = new ArrayList<>();
		for (InstructionHandle insnHandler : insnList) {
			Instruction insn = insnHandler.getInstruction();
			if ((insn instanceof FieldInstruction)
					|| (insn instanceof ArrayInstruction)
					|| (insn instanceof LocalVariableInstruction)) {
				rwInsns.add(insnHandler);
			}
		}
		return rwInsns;
	}
	
	private Map<LineNumberGen, List<InstructionHandle>> getLineNoInsnsMap(MethodGen methodGen, ConstantPoolGen constPool) {
		Map<LineNumberGen, List<InstructionHandle>> result = new HashMap<>();
		InstructionList insnList = methodGen.getInstructionList();
		for (LineNumberGen lineGen : methodGen.getLineNumbers()) {
			List<InstructionHandle> lineInsns = findCorrespondingInstructions(insnList,
					methodGen.getLineNumberTable(constPool), lineGen.getSourceLine());
			result.put(lineGen, lineInsns);
		}
		return result;
	}
	
	protected List<InstructionHandle> findCorrespondingInstructions(InstructionList list, LineNumberTable lineTable,
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
	
	private void setMethodIndex(ConstantPoolGen cpg) {
		// microbat/instrumentation/trace/data/ExecutionTracer getTracer(()Lmicrobat/instrumentation/trace/data/ExecutionTracer;);
		executionTracer_getTracer_idx = cpg.addMethodref("microbat/instrumentation/trace/data/ExecutionTracer", "getTracer", "()Lmicrobat/instrumentation/trace/data/ExecutionTracer;");
		// microbat/instrumentation/trace/data/ExecutionTracer hitLine((I)V);
		executionTracer_hitLine_idx = cpg.addMethodref("microbat/instrumentation/trace/data/ExecutionTracer", "hitLine", "(I)V");
		// microbat/instrumentation/trace/data/ExecutionTracer writeField((Ljava/lang/Object;)V);
		executionTracer_writeField_idx = cpg.addMethodref("microbat/instrumentation/trace/data/ExecutionTracer", "writeField", "(Ljava/lang/Object;)V");
		// microbat/instrumentation/trace/data/ExecutionTracer writeLocalVar((Ljava/lang/Object;I)V);
		executionTracer_writeLocalVar_idx = cpg.addMethodref("microbat/instrumentation/trace/data/ExecutionTracer", "writeLocalVar", "(Ljava/lang/Object;I)V");
	}
	
	private static void updateTargeters(InstructionHandle oldPos, InstructionHandle newPos) {
		InstructionTargeter[] itList = oldPos.getTargeters();
		if (itList != null) {
			for (InstructionTargeter it : itList) {
				it.updateTarget(oldPos, newPos);
			}
		}
	}
}
