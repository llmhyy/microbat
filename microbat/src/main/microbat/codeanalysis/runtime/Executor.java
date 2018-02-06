package microbat.codeanalysis.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadStartRequest;

import microbat.codeanalysis.runtime.jpda.expr.ExpressionParser;
import microbat.codeanalysis.runtime.jpda.expr.ExpressionParser.GetFrame;
import microbat.codeanalysis.runtime.jpda.expr.ParseException;
import microbat.preference.AnalysisScopePreference;
import microbat.util.FilterUtils;
import microbat.util.FilterUtils.ExtJarPackagesContainer;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.AppJavaClassPath;

/**
 * This class is used to locate all the executor classes in this project
 * @author "linyun"
 *
 */
@SuppressWarnings("restriction")
public abstract class Executor {
	
	protected int steps = 0;
	
	public int TIME_OUT = 30000;
	
	protected List<StepRequest> stepRequestList = new ArrayList<>();
	protected MethodEntryRequest methodEntryRequest;
	protected MethodExitRequest methodExitRequest;
	protected ClassPrepareRequest classPrepareRequest;
	protected ExceptionRequest exceptionRequest;
	protected ThreadStartRequest threadStartRequest;
	protected BreakpointRequest breakpointRequest;
	
	private static String[] defaultLibExcludes = { 
			"java.awt.*",
			"java.applet.*", 
			"java.lang.*",
			"java.beans.*", 
			"java.io.*", 
			"java.math.*", 
			"java.net.*", 
			"java.nio.*", 
			"java.rmi.*",
			"java.security.*", 
			"java.sql.*", 
			"java.text.*", 
			"java.util.*",
			"javax.*", 
			"sun.*", 
			"com.sun.*", 
			"com.oracle.*",
			"org.ietf.*",
			"org.omg.*",
			"org.jcp.*",
			"org.w3c.*",
			"org.xml.*",
			"sunw.*",
			"org.junit.*", "junit.*", "junit.framework.*", "org.hamcrest.*", "org.hamcrest.core.*", "org.hamcrest.internal.*",
			"jdk.*", "jdk.internal.*", "org.GNOME.Accessibility.*"
			};
	
	
	private static String[] libExcludes = { 
			"java.awt.*",
			"java.applet.*", 
			"java.lang.*",
			"java.beans.*", 
			"java.io.*", 
			"java.math.*", 
			"java.net.*", 
			"java.nio.*", 
			"java.rmi.*",
			"java.security.*", 
			"java.sql.*", 
			"java.text.*", 
			"java.util.Properties*",
			"java.util.concurrent.*", 
			"java.util.concurrent.*", 
			"java.util.function.*", 
			"java.util.jar.*", 
			"java.util.logging.*", 
			"java.util.prefs.*", 
			"java.util.regex.*", 
			"java.util.spi.*",
			"java.util.stream.*", 
			"java.util.zip.*", 
//			"java.util.*",
			"javax.*", 
			"sun.*", 
			"com.sun.*", 
			"com.oracle.*",
			"org.ietf.*",
			"org.omg.*",
			"org.jcp.*",
			"org.w3c.*",
			"org.xml.*",
			"sunw.*",
			/*"org.junit.*", "junit.*", "junit.framework.*", "org.hamcrest.*", "org.hamcrest.core.*", "org.hamcrest.internal.*",*/
			"jdk.*", "jdk.internal.*", "org.GNOME.Accessibility.*"
			};
	
	protected String[] junitExcludes = { 
			"org.junit.*", "junit.*", "junit.framework.*", "org.hamcrest.*", "org.hamcrest.core.*", "org.hamcrest.internal.*"
			};
	
	public static String[] libIncludes = {"java.util.*"};
//	public static String[] libIncludes = {"java.util.*\\"};
	
	private static List<String> microbatLibs = new ArrayList<>();
	
	static{
		microbatLibs.add("microbat.instrumentation.*");
		microbatLibs.add("javassist.*");
		microbatLibs.add("org.apache.bcel.*");
		
		
		String[] excludePatterns = Executor.deriveLibExcludePatterns();
		Executor.setLibExcludes(excludePatterns);
		String[] includePatterns = AnalysisScopePreference.getIncludedLibs();
		for(int i=0; i<includePatterns.length; i++){
			String includePattern = includePatterns[i];
			includePatterns[i] = includePattern.replace("\\", "");
		}
		
		Executor.libIncludes = includePatterns;
	}
	
	protected class Range{
		String className;
		int startLine;
		int endLine;
		
		public Range(String className, int startLine, int endLine) {
			super();
			this.className = className;
			this.startLine = startLine;
			this.endLine = endLine;
		}
	}
	
	class RangeFinder extends ASTVisitor{
		Range range;
		
		String className;
		String methodName;
		CompilationUnit cu;
		public RangeFinder(String className, String methodName, CompilationUnit cu) {
			this.className = className;
			this.methodName = methodName;
			this.cu = cu;
		}
		
		@Override
		public boolean visit(MethodDeclaration md){
			if(md.getName().getIdentifier().equals(methodName)){
				int startLine = cu.getLineNumber(md.getStartPosition());
				int endLine = cu.getLineNumber(md.getStartPosition()+md.getLength());
				
				range = new Range(className, startLine, endLine);
				return false;
			}
			else{
				return false;
			}
		}

	}
	
	protected Range getStartRange(AppJavaClassPath appClassPath){
		String className;
		String methodName;
		if(appClassPath.getOptionalTestClass()==null){
			className = appClassPath.getLaunchClass();
			methodName = "main";
		}
		else{
			className = appClassPath.getOptionalTestClass();
			methodName = appClassPath.getOptionalTestMethod();
		}
		
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(className, appClassPath);
		
		RangeFinder finder = new RangeFinder(className, methodName, cu);
		cu.accept(finder);
		
		if(finder.range==null) {
			TypeDeclaration type = (TypeDeclaration) cu.types().get(0);
			String typeName = type.getSuperclassType().toString();
			
			String fullName = null;
			for(Object obj: cu.imports()) {
				if(obj instanceof ImportDeclaration) {
					ImportDeclaration del = (ImportDeclaration)obj;
					String identifier = del.getName().getFullyQualifiedName();
					if(identifier.contains(typeName)) {
						fullName = identifier;
						break;
					}
				}
			}
			
			if(fullName==null) {
				fullName = cu.getPackage().getName().getFullyQualifiedName() + "." + typeName;
			}
			
			CompilationUnit parentCU = JavaUtil.findCompilationUnitInProject(fullName, appClassPath);
			RangeFinder parentFinder = new RangeFinder(fullName, methodName, parentCU);
			parentCU.accept(parentFinder);
			return parentFinder.range;
			
		}
		
		return finder.range;
	}
	
	
	protected void addStartBreakPointWatch(EventRequestManager erm, ReferenceType refType, 
			Range range) {
		if(breakpointRequest!=null && breakpointRequest.isEnabled()){
			return;
		}
		
		if (range.className.equals(refType.name())) {
			List<Location> listOfLocations;
			try {
				int count = range.startLine;
				listOfLocations = refType.locationsOfLine(count++);
				while (listOfLocations.size() == 0 && count<range.endLine) {
					listOfLocations = refType.locationsOfLine(count++);
				}
				
				Location loc = (Location)listOfLocations.get(0);
				breakpointRequest = erm.createBreakpointRequest(loc);
				breakpointRequest.setEnabled(true);
				
			} catch (AbsentInformationException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected String createEventLog(Event event) {
		String eventName = event.toString();
		if(event instanceof MethodEntryEvent){
			Method method = ((MethodEntryEvent) event).method();
			String className = method.declaringType().name();
			String methodName = method.name();
			String methodSig = method.signature();
			String location = ((MethodEntryEvent) event).location().toString();
			
			return eventName + ":" + className + "#" + methodName + methodSig + "(" + location + ")";  
		}
		else if (event instanceof MethodExitEvent) {
			Method method = ((MethodExitEvent) event).method();
			String className = method.declaringType().name();
			String methodName = method.name();
			String methodSig = method.signature();
			String location = ((MethodExitEvent) event).location().toString();
			
			return eventName + ":" + className + "#" + methodName + methodSig + "(" + location + ")"; 
		}
		else if(event instanceof StepEvent) {
			String location = ((StepEvent) event).location().toString();
			return eventName + ":" + "(" + location + ")";
		}
		else if(event instanceof ClassPrepareEvent){
			return eventName + ":" +  ((ClassPrepareEvent)event).referenceType().name();
		}
		
		return eventName;
	}
	
	/**
	 * This method derive more detailed libExcludes with libIncludes, for example, 
	 * when libExcludes has a pattern as java.* while libIncludes has a pattern as java.util.*,
	 * then the method can split java.* into a list including java.awt.*, java.lang.*, ..., except
	 * java.util.*. 
	 *
	 * @return
	 */
	public static String[] deriveLibExcludePatterns() {
		String[] excludedLibs = AnalysisScopePreference.getExcludedLibs();
		String[] includedLibs = AnalysisScopePreference.getIncludedLibs();
		Set<String> expandedExcludes;
		ExtJarPackagesContainer pkgsContainer = new ExtJarPackagesContainer(
				MicroBatUtil.getRtJarPathInDefinedJavaHome(), true);
		expandedExcludes = FilterUtils.deriveLibExcludePatterns(pkgsContainer, excludedLibs, includedLibs);
		return StringUtils.sortAlphanumericStrings(new ArrayList<>(expandedExcludes)).toArray(new String[0]);
	}
	
	protected boolean isInIncludedLibrary(Location currentLocation) {
		String typeName = currentLocation.declaringType().name();
		for(String expr: libIncludes){
			expr = expr.replace("*", "");
			if(typeName.contains(expr)){
				return true;
			}
		}
		
		return false;
	}
	
	public void addLibExcludeList(List<String> excludeList) {
		List<String> existingList = new ArrayList<>();
		for (int i = 0; i < getLibExcludes().length; i++) {
			existingList.add(getLibExcludes()[i]);
		}
		
		for (Iterator<String> iterator = excludeList.iterator(); iterator.hasNext();) {
			String excludeString = (String) iterator.next();
			excludeString = excludeString.replace("/", ".");
			if (!existingList.contains(excludeString)) {
				existingList.add(excludeString);
			}
		}
		
		setLibExcludes(existingList.toArray(new String[0]));
	}
	
	protected void enableAllStepRequests() {
		for(StepRequest stepRequest: stepRequestList) {
			stepRequest.enable();
		}
	}
	
	protected void disableAllStepRequests() {
		for(StepRequest stepRequest: stepRequestList) {
			stepRequest.disable();
		}
	}
	
	protected void addStepWatch(EventRequestManager erm, ThreadReference threadReference) {
		StepRequest stepRequest = erm.createStepRequest(threadReference, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
		stepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);

		for (String ex : getLibExcludes()) {
			stepRequest.addClassExclusionFilter(ex);
		}
		
		stepRequest.enable();
		this.stepRequestList.add(stepRequest);
	}
	
	/** add watch requests **/
	protected void addClassWatch(EventRequestManager erm) {
		classPrepareRequest = erm.createClassPrepareRequest();
		for (String ex : defaultLibExcludes) {
			classPrepareRequest.addClassExclusionFilter(ex);
		}
		
		for(String ex: Executor.getLibExcludes()){
			classPrepareRequest.addClassExclusionFilter(ex);
		}
		
		classPrepareRequest.setEnabled(true);
	}
	
	/**
	 * add method enter and exit event
	 */
	protected void addMethodWatch(EventRequestManager erm) {
		methodEntryRequest = erm.createMethodEntryRequest();
		for (String classPattern : getLibExcludes()) {
			methodEntryRequest.addClassExclusionFilter(classPattern);
		}
		methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		methodEntryRequest.enable();

		methodExitRequest = erm.createMethodExitRequest();
		for (String classPattern : getLibExcludes()) {
			methodExitRequest.addClassExclusionFilter(classPattern);
		}
		methodExitRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		methodExitRequest.enable();
	}
	
	protected void addExceptionWatch(EventRequestManager erm) {
		exceptionRequest = erm.createExceptionRequest(null, true, true);
		exceptionRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		for (String ex : getLibExcludes()) {
			exceptionRequest.addClassExclusionFilter(ex);
		}
		exceptionRequest.enable();
	}
	
	protected boolean hasValidThreadName(ThreadReference thread) {
		return !thread.name().equals("main") && !thread.name().equals("DestroyJavaVM")
				&& !thread.name().startsWith("Thread") 
//				&& !thread.name().equals("Attach Listener")
//				&& !thread.name().contains("RMI TCP")
				;
	}
	
	protected void addThreadStartWatch(EventRequestManager erm) {
		threadStartRequest = erm.createThreadStartRequest();
		threadStartRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		threadStartRequest.enable();
	}
	
	protected void excludeJUnitLibs() {
		List<Boolean> stepSwitch = new ArrayList<>();
		for(int i=0; i<stepRequestList.size(); i++) {
			StepRequest stepRequest = stepRequestList.get(i);
			stepSwitch.add(stepRequest.isEnabled());
			stepRequest.disable();
		}
		
		boolean classPrepareSwtich = classPrepareRequest.isEnabled();
		classPrepareRequest.disable();
		boolean exceptionSwtich = exceptionRequest.isEnabled();
		exceptionRequest.disable();
		
		for(String junitString: junitExcludes) {
			for(StepRequest stepRequest: stepRequestList) {
				stepRequest.addClassExclusionFilter(junitString);
			}
			
			classPrepareRequest.addClassExclusionFilter(junitString);
			exceptionRequest.addClassExclusionFilter(junitString);
		}
		
		classPrepareRequest.setEnabled(classPrepareSwtich);
		exceptionRequest.setEnabled(exceptionSwtich);
		for(int i=0; i<stepRequestList.size(); i++) {
			StepRequest stepRequest = stepRequestList.get(i);
			try {
				stepRequest.setEnabled(stepSwitch.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
	}
	
	protected Value retrieveComplicatedExpressionValue(String expr, VirtualMachine vm, GetFrame frameGetter) {
		List<Boolean> stepSwitch = new ArrayList<>();
		for(int i=0; i<stepRequestList.size(); i++) {
			StepRequest stepRequest = stepRequestList.get(i);
			stepSwitch.add(stepRequest.isEnabled());
			stepRequest.disable();
		}
		
//		boolean methodEntrySwtich = methodEntryRequest.isEnabled();
//		methodEntryRequest.disable();
//		boolean methodExistSwtich = methodExitRequest.isEnabled();
//		methodExitRequest.disable();
		boolean classPrepareSwtich = classPrepareRequest.isEnabled();
		classPrepareRequest.disable();
		boolean exceptionSwtich = exceptionRequest.isEnabled();
		exceptionRequest.disable();
		boolean threadSwith = threadStartRequest.isEnabled();
		threadStartRequest.disable();
		
		Value val = null;
		try {
			val = ExpressionParser.evaluate(expr, vm, frameGetter);
		} catch (ParseException e1) {
			//e1.printStackTrace();
		} catch (InvocationException e1) {
			e1.printStackTrace();
		} catch (InvalidTypeException e1) {
			e1.printStackTrace();
		} catch (ClassNotLoadedException e1) {
			e1.printStackTrace();
		} catch (IncompatibleThreadStateException e1) {
			e1.printStackTrace();
		} 
		
//		methodEntryRequest.setEnabled(methodEntrySwtich);
//		methodExitRequest.setEnabled(methodExistSwtich);
		classPrepareRequest.setEnabled(classPrepareSwtich);
		exceptionRequest.setEnabled(exceptionSwtich);
		threadStartRequest.setEnabled(threadSwith);
		for(int i=0; i<stepRequestList.size(); i++) {
			StepRequest stepRequest = stepRequestList.get(i);
			try {
				stepRequest.setEnabled(stepSwitch.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
		
		return val;
	}
	
	/**
	 * When analyzing a JUnit test case, we will not record the trace, until the target test
	 * case is loaded. In order to see whether the target test case is loaded, there is specific
	 * JUnit method is called. This method returns whether such the specific JUnit method
	 * has been called. 
	 * 
	 * @param declareType
	 * @param methodName
	 * @return
	 */
	protected boolean isTagJUnitCall(String declareType, String methodName) {
		return 
			(declareType.contains("junit.framework.TestResult") && methodName.equals("startTest")) ||
			(declareType.contains("org.junit.internal.runners.model.ReflectiveCallable") && methodName.equals("run"));
	}

	public static String[] getLibExcludes() {
		return libExcludes;
	}
	
	public static void setLibExcludes(String[] libExcludes) {
		List<String> libList = new ArrayList<>();
		for(String lib: libExcludes){
			libList.add(lib);
		}
		
		for(String microbatLib: microbatLibs){
			if(!libList.contains(microbatLib)){
				libList.add(microbatLib);
			}
		}
		
		Executor.libExcludes = libList.toArray(new String[0]);
	}


	public static String[] getDefaultLibExcludes() {
		return defaultLibExcludes;
	}

}
