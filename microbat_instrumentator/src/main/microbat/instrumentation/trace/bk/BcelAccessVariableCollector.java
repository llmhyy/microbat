package microbat.instrumentation.trace.bk;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import microbat.codeanalysis.bytecode.LineNumberVisitor0;
import microbat.instrumentation.trace.model.AccessVariableInfo;

public class BcelAccessVariableCollector implements IAccessVariableCollector {
	private String className;
	private LineNumberVisitor0 visitor;
	
	public BcelAccessVariableCollector(String className) {
		this.className = className;
	}

	@Override
	public void collectVariable(CodeIterator iterator, int pos, ConstPool constPool, AccessVariableInfo lineInfo) {
		lineInfo.addPc(pos);
		InstructionPrinter printer = new InstructionPrinter(System.out);
		System.out.println(printer.instructionString(iterator, pos, constPool));
		System.out.println(String.format("line=%d, pc=%d", lineInfo.getLineNo(), pos));
		collectVariables(lineInfo);
	}

	public void collectVariables(AccessVariableInfo lineInfo) {
		if (visitor != null && visitor.getLineNumber() == lineInfo.getLineNo()) {
			return; // visit already;
		}
		
		ClassPath classPath = new ClassPath(ClassPath.getClassPath());
		Repository.setRepository(SyntheticRepository.getInstance(classPath));
		JavaClass clazz;
		try {
			clazz = Repository.lookupClass(className);
			LineNumberVisitor0 visitor = new LineNumberVisitor0(lineInfo.getLineNo(), className);
			clazz.accept(new DescendingVisitor(clazz, visitor));
			visitor.setJavaClass(clazz);
			lineInfo.setReadVars(visitor.getReadVars());
			lineInfo.setWrittenVars(visitor.getWrittenVars());
			lineInfo.setReturnVar(visitor.getReturnedVar());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
