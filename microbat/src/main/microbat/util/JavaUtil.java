package microbat.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.VirtualMachine;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import microbat.codeanalysis.ast.MethodDeclarationFinder;
import microbat.codeanalysis.ast.MethodInvocationFinder;
import microbat.codeanalysis.runtime.ProgramExecutor;
import microbat.codeanalysis.runtime.jpda.expr.ExpressionParser;
import microbat.codeanalysis.runtime.jpda.expr.ParseException;
import microbat.model.trace.TraceNode;

@SuppressWarnings("restriction")
public class JavaUtil {
	private static final String TO_STRING_SIGN= "()Ljava/lang/String;";
	private static final String TO_STRING_NAME= "toString";
	
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public static CompilationUnit parseCompilationUnit(String file){
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(false);
		
		try {
			String text = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
			
			parser.setSource(text.toCharArray());
			
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);		
			return cu;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	
	
	public static String retrieveStringValueOfArray(ArrayReference arrayValue) {
		String stringValue;
		List<Value> list = new ArrayList<>();
		if(arrayValue.length() > 0){
			list = arrayValue.getValues(0, arrayValue.length()); 
		}
		StringBuffer buffer = new StringBuffer();
		for(Value v: list){
			if(v != null){
				buffer.append(v.toString()+ ",") ;					
			}
		}
		stringValue = buffer.toString();
		return stringValue;
	}
	
	public static String retrieveToStringValue(ThreadReference thread,
			ObjectReference objValue, ProgramExecutor executor) throws TimeoutException {
		
		ReferenceType type = (ReferenceType) objValue.type();
		Method method = type.methodsByName(TO_STRING_NAME, TO_STRING_SIGN).get(0);
		
		if(type.toString().equals("org.apache.commons.math.exception.NonMonotonousSequenceException")){
			System.currentTimeMillis();
		}
		
		boolean classPrepare = executor.getClassPrepareRequest().isEnabled();
		boolean step = executor.getStepRequest().isEnabled();
		boolean methodEntry = executor.getMethodEntryRequest().isEnabled();
		boolean methodExit = executor.getMethodExitRequset().isEnabled();
		boolean exception = executor.getExceptionRequest().isEnabled();
		
		executor.getClassPrepareRequest().disable();
		executor.getStepRequest().disable();
		executor.getMethodEntryRequest().disable();
		executor.getMethodExitRequset().disable();
		executor.getExceptionRequest().disable();
		
		//((VirtualMachine)thread.virtualMachine()).setRequestTimeout(5000);
		
		Value messageValue = null;
		try {
			messageValue = objValue.invokeMethod(thread, method, 
					new ArrayList<Value>(), ObjectReference.INVOKE_SINGLE_THREADED);
		} catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException
				| InvocationException e) {
			//e.printStackTrace();
		}
		
		executor.getClassPrepareRequest().setEnabled(classPrepare);
		executor.getStepRequest().setEnabled(step);
		executor.getMethodEntryRequest().setEnabled(methodEntry);;
		executor.getMethodExitRequset().setEnabled(methodExit);;
		executor.getExceptionRequest().setEnabled(exception);;
		
		String stringValue = (messageValue != null) ? messageValue.toString() : "null";
		return stringValue;
	}
	
	public static Value retriveExpression(final StackFrame frame, String expression){
		ExpressionParser.GetFrame frameGetter = new ExpressionParser.GetFrame() {
            @Override
            public StackFrame get()
                throws IncompatibleThreadStateException
            {
            	return frame;
                
            }
        };
        
        try {
        	
        	Value val = ExpressionParser.evaluate(expression, frame.virtualMachine(), frameGetter);
        	return val;        		
			
		} catch (ParseException e) {
			//e.printStackTrace();
		} catch (InvocationException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		} catch (ClassNotLoadedException e) {
			e.printStackTrace();
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		} catch (Exception e){
			System.out.println("Cannot parse " + expression);
			e.printStackTrace();
		}
        
        return null;
	}
	
	/**
	 * generate signature such as methodName(java.lang.String)L
	 * @param md
	 * @return
	 */
	public static String generateMethodSignature(IMethodBinding mBinding){
//		IMethodBinding mBinding = md.resolveBinding();
		
		String returnType = mBinding.getReturnType().getKey();
		
		String methodName = mBinding.getName();
		
		List<String> paramTypes = new ArrayList<>();
		for(ITypeBinding tBinding: mBinding.getParameterTypes()){
			String paramType = tBinding.getKey();
			paramTypes.add(paramType);
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(methodName);
		buffer.append("(");
		for(String pType: paramTypes){
			buffer.append(pType);
			//buffer.append(";");
		}
		
		buffer.append(")");
		buffer.append(returnType);
//		
//		String sign = buffer.toString();
//		if(sign.contains(";")){
//			sign = sign.substring(0, sign.lastIndexOf(";")-1);			
//		}
//		sign = sign + ")" + returnType;
		
		String sign = buffer.toString();
		
		return sign;
	}
	
	public static String getFullNameOfCompilationUnit(CompilationUnit cu){
		
		String packageName = "";
		if(cu.getPackage() != null){
			packageName = cu.getPackage().getName().toString();
		}
		AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) cu.types().get(0);
		String typeName = typeDeclaration.getName().getIdentifier();
		
		if(packageName.length() == 0){
			return typeName;
		}
		else{
			return packageName + "." + typeName; 			
		}
		
	}
	
	
	public static CompilationUnit findCompilationUnitInProject(String qualifiedName){
		CompilationUnit cu = Settings.compilationUnitMap.get(qualifiedName);
		if(null == cu){
			try{
				ICompilationUnit icu = findICompilationUnitInProject(qualifiedName);
				cu = convertICompilationUnitToASTNode(icu);	
				Settings.compilationUnitMap.put(qualifiedName, cu);
				return cu;
			}
			catch(IllegalStateException e){
				e.printStackTrace();
			} 
		}
		
		return cu;
	} 
	
	public static CompilationUnit findNonCacheCompilationUnitInProject(String qualifiedName){
		ICompilationUnit icu = findNonCacheICompilationUnitInProject(qualifiedName);
		CompilationUnit cu = convertICompilationUnitToASTNode(icu);
		
		return cu;
	}
	
	public static ICompilationUnit findICompilationUnitInProject(String qualifiedName){
		ICompilationUnit icu = Settings.iCompilationUnitMap.get(qualifiedName);
		if(null == icu){
			IJavaProject project = JavaCore.create(getSpecificJavaProjectInWorkspace());
			try {
				IType type = project.findType(qualifiedName);
				if(type != null){
					icu = type.getCompilationUnit();
					Settings.iCompilationUnitMap.put(qualifiedName, icu);
				}
				
			} catch (JavaModelException e1) {
				e1.printStackTrace();
			}
		}
		
		return icu;
	}
	
	public static ICompilationUnit findNonCacheICompilationUnitInProject(String qualifiedName) {
		IJavaProject project = JavaCore.create(getSpecificJavaProjectInWorkspace());
		try {
			IType type = project.findType(qualifiedName);
			if(type != null){
				return  type.getCompilationUnit();
			}
			
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	
	public static IPackageFragmentRoot findTestPackageRootInProject(){
		IJavaProject project = JavaCore.create(getSpecificJavaProjectInWorkspace());
		try {
			for(IPackageFragmentRoot packageFragmentRoot: project.getPackageFragmentRoots()){
				if(!(packageFragmentRoot instanceof JarPackageFragmentRoot) && packageFragmentRoot.getResource().toString().contains("test")){
					
					return packageFragmentRoot;
//					IPackageFragment packageFrag = packageFragmentRoot.getPackageFragment(packageName);
//					
//					String fragName = packageFrag.getElementName();
//					if(packageFrag.exists() && fragName.equals(packageName)){
//						return packageFrag;
//					}
					
				}
			}
			
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public static CompilationUnit convertICompilationUnitToASTNode(ICompilationUnit iunit){
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setSource(iunit);
		
		CompilationUnit cu = null;
		try{
			cu = (CompilationUnit) parser.createAST(null);		
			return cu;
		}
		catch(java.lang.IllegalStateException e){
			return null;
		}
	}
	
	public static IProject getSpecificJavaProjectInWorkspace(){
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		
		for(int i=0; i<projects.length; i++){
			if(Settings.projectName.equals(projects[i].getName())){
				return projects[i];
				//return JavaCore.create(projects[i]);
			}
		}
		
		return null;
	}

	public static boolean isTheLocationHeadOfClass(String sourceName, int lineNumber) {
		CompilationUnit cu = findCompilationUnitInProject(sourceName);
		AbstractTypeDeclaration type = (AbstractTypeDeclaration) cu.types().get(0);
		int headLine = cu.getLineNumber(type.getName().getStartPosition());
		
		return headLine==lineNumber;
	}

	public static boolean isCompatibleMethodSignature(String thisSig, String thatSig) {
		if(thatSig.equals(thisSig)){
			return true;
		}
		
		String thisClassName = thisSig.substring(0, thisSig.indexOf("#"));
		String thisMethodSig = thisSig.substring(thisSig.indexOf("#")+1, thisSig.length());
		
		String thatClassName = thatSig.substring(0, thatSig.indexOf("#"));
		String thatMethodSig = thatSig.substring(thatSig.indexOf("#")+1, thatSig.length());
		
		if(thisMethodSig.equals(thatMethodSig)){
			CompilationUnit thisCU = JavaUtil.findCompilationUnitInProject(thisClassName);
			CompilationUnit thatCU = JavaUtil.findCompilationUnitInProject(thatClassName);
			
			if(thisCU==null || thatCU==null){
				return true;
			}
			
			AbstractTypeDeclaration thisType = (AbstractTypeDeclaration) thisCU.types().get(0);
			AbstractTypeDeclaration thatType = (AbstractTypeDeclaration) thatCU.types().get(0);
			
			ITypeBinding thisTypeBinding = thisType.resolveBinding();
			ITypeBinding thatTypeBinding = thatType.resolveBinding();
			
			boolean isSame = thisTypeBinding.getQualifiedName().equals(thatTypeBinding.getQualifiedName());
			
			if(isSame){
				return true;
			}
			else{
				boolean isCom1 = thisTypeBinding.isSubTypeCompatible(thatTypeBinding);
				boolean isCom2 = thatTypeBinding.isSubTypeCompatible(thisTypeBinding);
				
				return isCom1 || isCom2;				
			}
		}
		
		return false;
	}
	
	/**
	 * If the prevNode is the invocation parent of postNode, this method return the method binding of the
	 * corresponding method.
	 *  
	 * @param prevNode
	 * @param postNode
	 * @return
	 */
	public static MethodDeclaration checkInvocationParentRelation(TraceNode prevNode, TraceNode postNode){
		List<IMethodBinding> methodInvocationBindings = findMethodInvocations(prevNode);
		if(!methodInvocationBindings.isEmpty()){
			MethodDeclaration md = findMethodDeclaration(postNode);
			IMethodBinding methodDeclarationBinding = md.resolveBinding();
			
			if(canFindCompatibleSig(methodInvocationBindings, methodDeclarationBinding)){
				//return methodDeclarationBinding;
				return md;
			}
		}
		
		System.currentTimeMillis();
		return null;
	}

	private static List<IMethodBinding> findMethodInvocations(TraceNode prevNode) {
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(prevNode.getClassCanonicalName());
		
		MethodInvocationFinder finder = new MethodInvocationFinder(cu, prevNode.getLineNumber());
		cu.accept(finder);
		
		List<IMethodBinding> methodInvocations = new ArrayList<>();
		
		List<MethodInvocation> invocations = finder.getInvocations();
		for(MethodInvocation invocation: invocations){
			IMethodBinding mBinding = invocation.resolveMethodBinding();
			
			methodInvocations.add(mBinding);
			
		}
		
		return methodInvocations;
	}

	private static MethodDeclaration findMethodDeclaration(TraceNode postNode) {
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(postNode.getClassCanonicalName());
		
		MethodDeclarationFinder finder = new MethodDeclarationFinder(cu, postNode.getLineNumber());
		cu.accept(finder);
		
		MethodDeclaration md = finder.getMethod();
		
		
		return md;
	}
	
	public static String convertFullSignature(IMethodBinding binding){
		
		String className = binding.getDeclaringClass().getBinaryName();
		String methodSig = generateMethodSignature(binding);
		
		return className + "#" + methodSig;
	}

	private static boolean canFindCompatibleSig(
			List<IMethodBinding> methodInvocationBindings, IMethodBinding methodDeclarationBinding) {
		
		List<String> methodInvocationSigs = new ArrayList<>();
		for(IMethodBinding binding: methodInvocationBindings){
			String sig = convertFullSignature(binding);
			methodInvocationSigs.add(sig);
		}
		String methodDeclarationSig = convertFullSignature(methodDeclarationBinding);
		
		if(methodInvocationSigs.contains(methodDeclarationSig)){
			return true;
		}
		else{
			for(String methodInvocationSig: methodInvocationSigs){
				if(isCompatibleMethodSignature(methodInvocationSig, methodDeclarationSig)){
					return true;
				}
			}
		}
		
		System.currentTimeMillis();
		
		return false;
	}
	
	public static String createSignature(String className, String methodName, String methodSig) {
		String sig = className + "#" + methodName + methodSig;
		return sig;
	}
}
