package microbat.instrumentation;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class OperandRetrievingTransfomer implements ClassFileTransformer {

	// public static String tempVariableName = "microbat_return_value";
	public static String returnVariableValue = "microbat_return_value";

	private boolean isExcluded(String className) {
		className = className.replace("/", ".");
		for (String excl : Excludes.defaultLibExcludes) {
			String prefix = excl.replace("*", "");
			if (className.startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (isExcluded(className)) {
			return classfileBuffer;
		}

		if(!className.contains("CategoryPlot")){
			return classfileBuffer;
		}
		
//		System.out.println(className);

		ClassPool pool = ClassPool.getDefault();
		CtClass compiledClass;
		try {
			compiledClass = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
			System.out.println("ccccccccccccccccclass name: " + compiledClass.getName());
			
			System.out.println(compiledClass.getDeclaredMethods());
			for (CtMethod method : compiledClass.getDeclaredMethods()) {
//				System.out.println("method: " + method);
				CtClass objectClass = ClassPool.getDefault().get("java.lang.Object");
				final String methodName = method.getName();
				if (!method.isEmpty()) {
					
					if(
							true
//							methodName.startsWith("c") 
//							&& 
//							(methodName.startsWith("calculate") 
//							|| methodName.startsWith("clear")
//							&& !methodName.startsWith("config")
//							&& !methodName.startsWith("clear")
//									)
							){
						method.addLocalVariable(returnVariableValue, objectClass);
						method.instrument(new ExprEditor(){
							public void edit(final MethodCall call) throws CannotCompileException {
								try {
									
									CtClass returnClass = call.getMethod().getReturnType();
									CtClass declaringClass = call.getMethod().getDeclaringClass();
									if(isExcluded(declaringClass.getName())){
										return;
									}
									
									if(!returnClass.equals(CtClass.voidType)){
										String callerName = call.getMethodName();
										String sig = call.getSignature();
										System.out.println(methodName + " calls: " + callerName + sig);
										System.out.println("return type: " + returnClass.getName());
										
										if (returnClass.isPrimitive()) {
											call.replace("{"  
													+ "$_ = $proceed($$);"
													+ "String microbat_tmp_string = String.valueOf($_ );" 
													+ returnVariableValue + " = microbat_tmp_string;"
//													+ "System.out.println(" + returnVariableValue + ");"
													+ "}");
										} else {
											call.replace("{" 
													+ "$_ = $proceed($$);" 
													+ returnVariableValue + " = $_ ;"
//													+ "System.out.println(" + returnVariableValue + ");"
													+ "}");
										}
									}
								} catch (NotFoundException e) {
									e.printStackTrace();
								}
							}
						});
					}
					
					
				}

			}

			return compiledClass.toBytecode();
		} catch (NotFoundException | CannotCompileException | IOException e) {
//			e.printStackTrace();
		}

//		System.out.println("end ");
		return classfileBuffer;
	}

}
