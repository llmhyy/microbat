package microbat.codeanalysis.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
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

/**
 * This class is used to locate all the executor classes in this project
 * @author "linyun"
 *
 */
@SuppressWarnings("restriction")
public abstract class Executor {
	
	protected int steps = 0;
	
	public static int TIME_OUT = 30000;
	
	protected List<StepRequest> stepRequestList = new ArrayList<>();
	protected MethodEntryRequest methodEntryRequest;
	protected MethodExitRequest methodExitRequest;
	protected ClassPrepareRequest classPrepareRequest;
	protected ExceptionRequest exceptionRequest;
	protected ThreadStartRequest threadStartRequest;
	protected BreakpointRequest breakpointRequest;
	
	protected String[] libExcludes = { "java.*", "java.lang.*", "javax.*", "sun.*", "com.sun.*", 
			/*"org.junit.*", "junit.*", "junit.framework.*", "org.hamcrest.*", "org.hamcrest.core.*", "org.hamcrest.internal.*",*/
			"jdk.*", "jdk.internal.*", "org.GNOME.Accessibility.*"};
	
	protected String[] junitExcludes = { 
			"org.junit.*", "junit.*", "junit.framework.*", "org.hamcrest.*", "org.hamcrest.core.*", "org.hamcrest.internal.*"
			};
	
	public void addLibExcludeList(List<String> excludeList) {
		List<String> existingList = new ArrayList<>();
		for (int i = 0; i < libExcludes.length; i++) {
			existingList.add(libExcludes[i]);
		}
		
		for (Iterator<String> iterator = excludeList.iterator(); iterator.hasNext();) {
			String excludeString = (String) iterator.next();
			if (!existingList.contains(excludeString)) {
				existingList.add(excludeString);
			}
		}
		
		libExcludes = existingList.toArray(new String[0]);
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

		for (String ex : libExcludes) {
			stepRequest.addClassExclusionFilter(ex);
		}

		stepRequest.enable();
		this.stepRequestList.add(stepRequest);
	}
	
	protected boolean hasValidThreadName(ThreadReference thread) {
		return !thread.name().equals("main") && !thread.name().equals("DestroyJavaVM")
				&& !thread.name().startsWith("Thread");
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
		
		boolean methodEntrySwtich = methodEntryRequest.isEnabled();
		methodEntryRequest.disable();
		boolean methodExistSwtich = methodExitRequest.isEnabled();
		methodExitRequest.disable();
		boolean classPrepareSwtich = classPrepareRequest.isEnabled();
		classPrepareRequest.disable();
		boolean exceptionSwtich = exceptionRequest.isEnabled();
		exceptionRequest.disable();
		
		for(String junitString: junitExcludes) {
			for(StepRequest stepRequest: stepRequestList) {
				stepRequest.addClassExclusionFilter(junitString);
			}
			
			methodEntryRequest.addClassExclusionFilter(junitString);
			methodExitRequest.addClassExclusionFilter(junitString);
			classPrepareRequest.addClassExclusionFilter(junitString);
			exceptionRequest.addClassExclusionFilter(junitString);
		}
		
		methodEntryRequest.setEnabled(methodEntrySwtich);
		methodExitRequest.setEnabled(methodExistSwtich);
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
		
		boolean methodEntrySwtich = methodEntryRequest.isEnabled();
		methodEntryRequest.disable();
		boolean methodExistSwtich = methodExitRequest.isEnabled();
		methodExitRequest.disable();
		boolean classPrepareSwtich = classPrepareRequest.isEnabled();
		classPrepareRequest.disable();
		boolean exceptionSwtich = exceptionRequest.isEnabled();
		exceptionRequest.disable();
		
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
		
		methodEntryRequest.setEnabled(methodEntrySwtich);
		methodExitRequest.setEnabled(methodExistSwtich);
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
}
