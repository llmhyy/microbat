package microbat.codeanalysis.bytecode;

import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;

import microbat.model.BreakPoint;
import sav.strategies.dto.AppJavaClassPath;

public class CallGraph {
	private AppJavaClassPath appPath;
	
	private Map<String, MethodNode> methodMaps = new HashMap<>();
	
	public CallGraph(AppJavaClassPath appPath){
		this.appPath = appPath;
	}
	
	public MethodNode findOrCreateMethodNode(BreakPoint location) {
		String methodSign = location.getMethodSign();
		if(methodSign!=null){
			
			MethodNode node = methodMaps.get(methodSign);
			if(node==null){
				Method method = findByteCodeMethod(location);
				if(method!=null){
					node = new MethodNode(methodSign, method);
					methodMaps.put(methodSign, node);
					
					appendCallGraphRootAt(node);
				}
			}
			
			return node;
		}
		
		return null;
	}
	
	private void appendCallGraphRootAt(MethodNode node) {
		Method method = node.getMethod();
		if(method.getCode()==null){
			return;
		}
		
		ConstantPoolGen cpGen = new ConstantPoolGen(method.getConstantPool());
		
		InstructionList insList = new InstructionList(method.getCode().getCode());
		for(InstructionHandle handle: insList.getInstructionHandles()){
			Instruction ins = handle.getInstruction();
			if(ins instanceof InvokeInstruction){
				InvokeInstruction invokeIns = (InvokeInstruction)ins;

				String methodSignature = invokeIns.getMethodName(cpGen)+invokeIns.getSignature(cpGen);
				String className = invokeIns.getClassName(cpGen);
				
				if(!className.contains("[")){
					ByteCodeMethodFinder finder = new MethodFinderBySignature(methodSignature);
					ByteCodeParser.parse(className, finder, appPath);
					
					Method calleeMethod = finder.getMethod();
					if(calleeMethod==null && finder.javaClass.isClass()){
						MethodAndClass mc = checkSuperClass((MethodFinderBySignature)finder, appPath);
						calleeMethod = mc.method;
						className = mc.clazz;
					}
					
					if(calleeMethod != null){
						String calleeSignature = className + "#" + calleeMethod.getName() + calleeMethod.getSignature();
						
						if(!methodMaps.containsKey(calleeSignature)){
							MethodNode calleeNode = new MethodNode(calleeSignature, calleeMethod);
							methodMaps.put(calleeSignature, calleeNode);
							
							appendCallGraphRootAt(calleeNode);
						}
					}
					
				}
				
			}
		}
	}
	
	class MethodAndClass{
		Method method;
		String clazz;
		public MethodAndClass(Method method, String clazz) {
			super();
			this.method = method;
			this.clazz = clazz;
		}
		
	}
	
	private MethodAndClass checkSuperClass(MethodFinderBySignature finder, AppJavaClassPath appPath2) {
		JavaClass clazz = finder.javaClass;
		while(clazz!=null){
			try {
				clazz = clazz.getSuperClass();
				
				ByteCodeParser.parse(clazz.getClassName(), finder, appPath);
				Method method = finder.getMethod();
				if(method!=null){
					return new MethodAndClass(method, clazz.getClassName());
				}
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				break;
			}
		}
		
		
		return null;
	}

	private Map<String, Method> locationMethodMap = new HashMap<>();
	private Method findByteCodeMethod(BreakPoint point) {
		String className = point.getClassCanonicalName();
		int lineNumber = point.getLineNumber();
		
		String locationID = className + "$" + lineNumber;
		Method method = locationMethodMap.get(locationID);
		if (method == null) {
			ByteCodeMethodFinder finder = new MethodFinderByLine(point);
			ByteCodeParser.parse(className, finder, appPath);
			method = finder.getMethod();
			locationMethodMap.put(locationID, method);
		}
		
		return method;
	}


}
