package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

public class ByteCodeVisitor extends EmptyVisitor {
	@SuppressWarnings("rawtypes")
	protected List<InstructionHandle> findCorrespondingInstructions(int lineNumber, Code code) {
		List<InstructionHandle> correspondingInstructions = new ArrayList<>();

		InstructionList list = new InstructionList(code.getCode());
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			InstructionHandle insHandle = (InstructionHandle) iter.next();
			int instructionLine = code.getLineNumberTable().getSourceLine(insHandle.getPosition());

			if (instructionLine == lineNumber) {
				correspondingInstructions.add(insHandle);
			}
		}

		return correspondingInstructions;
	}

	protected JavaClass javaClass;

	public void setJavaClass(JavaClass clazz) {
		this.javaClass = clazz;
	}

	public JavaClass getJavaClass() {
		return javaClass;
	}
}
