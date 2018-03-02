package microbat.mutation.trace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LocalVariableInstruction;

import microbat.codeanalysis.bytecode.ByteCodeVisitor;
import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.model.ClassLocation;

/**
 * @author Yun Lin
 *
 */
public class LineVisitor extends ByteCodeVisitor {
	private List<ClassLocation> inputLocations;
	private Set<ClassLocation> result = new HashSet<>();
	
	public LineVisitor(List<ClassLocation> inputLocations) {
		super();
		this.inputLocations = inputLocations;
	}

	
	public void visitMethod(Method method){
		Code code = method.getCode();
		if(code == null){
			return;
		}
		List<ClassLocation> includedBreakPoints = findIncludeBreakPoints(code);
		if(includedBreakPoints.isEmpty()){
			return;
		}
		
		CFG cfg = new CFGConstructor().buildCFGWithControlDomiance(code);
		if (code != null) {
			for (ClassLocation location : inputLocations) {
				List<InstructionHandle> correspondingInstructions = findCorrespondingInstructions(
						location.getLineNumber(), code);
				if (correspondingInstructions.isEmpty()) {
					continue;
				}
				List<InstructionHandle> writeInsns = containWriteInstructions(correspondingInstructions);
				for (InstructionHandle insHandle : writeInsns) {
					CFGNode cfgNode = cfg.findNode(insHandle.getPosition());
					for (CFGNode postDominatee : cfgNode.getPostDominatee()) {
						int instructionLine = code.getLineNumberTable()
								.getSourceLine(postDominatee.getInstructionHandle().getPosition());
						ClassLocation newLoc = new ClassLocation(location.getClassCanonicalName(), location.getMethodSign(), 
								instructionLine);
						result.add(newLoc);
					}
				}
			}
		}
	}
	
	private List<InstructionHandle> containWriteInstructions(List<InstructionHandle> correspondingInstructions) {
		List<InstructionHandle> writeInsns = new ArrayList<>();
		for (InstructionHandle insnHandler : correspondingInstructions) {
			Instruction insn = insnHandler.getInstruction();
			if (insn instanceof FieldInstruction && 
					existIn(insn.getOpcode(), Const.PUTFIELD, Const.PUTSTATIC)) {
				writeInsns.add(insnHandler);
				
			} else if (insn instanceof ArrayInstruction && 
					existIn(insn.getOpcode(), Const.AASTORE, Const.FASTORE, Const.LASTORE,
							Const.CASTORE, Const.IASTORE, Const.BASTORE, Const.SASTORE, Const.DASTORE)) {
				writeInsns.add(insnHandler);
			
			} else if (insn instanceof LocalVariableInstruction && 
					existIn(((LocalVariableInstruction) insn).getCanonicalTag(), Const.FSTORE, Const.IINC, Const.DSTORE, Const.ASTORE,
							Const.ISTORE, Const.LSTORE)) {
				writeInsns.add(insnHandler);
			}
		}
		return writeInsns;
	}

	protected static boolean existIn(short opCode, short... checkOpCodes) {
		for (short checkOpCode : checkOpCodes) {
			if (opCode == checkOpCode) {
				return true;
			}
		}
		return false;
	}

	private List<ClassLocation> findIncludeBreakPoints(Code code) {
		List<ClassLocation> includedPoints = new ArrayList<>();
		LineNumber[] table = code.getLineNumberTable().getLineNumberTable();

		int startLine = table[0].getLineNumber() - 1;
		int endLine = table[table.length - 1].getLineNumber();
		for (ClassLocation point : inputLocations) {
			if (startLine <= point.getLineNumber() && point.getLineNumber() <= endLine) {
				includedPoints.add(point);
			}
		}
		return includedPoints;
	}

	public Set<ClassLocation> getResult() {
		return result;
	}
	
}
