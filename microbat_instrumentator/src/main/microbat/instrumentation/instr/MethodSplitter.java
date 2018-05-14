package microbat.instrumentation.instr;

import org.apache.bcel.Const;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class MethodSplitter {
	private ClassGen classGen;
	private ConstantPoolGen constPool;
	
	public MethodSplitter(ClassGen classGen, ConstantPoolGen constPool) {
		this.classGen = classGen;
		this.constPool = constPool;
	}
	
	/**
	 * LLT: 
	 * methodGen in this case is over-long one, so be careful when working on the object since
	 * some methods will throw exception when being triggered (methods which requires dumping out bytecode).
	 * for ex: getByteCode(), getMethod()
	 */
	public GeneratedMethods splitMethod(MethodGen methodGen) {
		MethodGen modifiedMethod = methodGen;
		GeneratedMethods methods = new GeneratedMethods(modifiedMethod);
		
		
		methods.setRootMethod(modifiedMethod);
		return methods;
	}
	
	private MethodGen createMethod(Type returnType, Type[] arg_types, InstructionList instrnList) {
		int access_flags = Const.ACC_PRIVATE;
		String[] arg_name = null;
		String methodName = "$$TODO_METHODNAME";
		String className = classGen.getClassName();
		MethodGen methodgen = new MethodGen(access_flags, returnType, arg_types, arg_name, methodName, className,
				instrnList, constPool);
		return methodgen;
	}
}
