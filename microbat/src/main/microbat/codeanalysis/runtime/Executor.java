package microbat.codeanalysis.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadStartRequest;

/**
 * This class is used to locate all the executor classes in this project
 * @author "linyun"
 *
 */
@SuppressWarnings("restriction")
public abstract class Executor {
	
	protected int steps = 0;
	
	public static int TIME_OUT = 100000;
	
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
		
		for(int i=0; i<stepRequestList.size(); i++) {
			StepRequest stepRequest = stepRequestList.get(i);
			stepRequest.setEnabled(stepSwitch.get(i));
		}
		methodEntryRequest.setEnabled(methodEntrySwtich);
		methodExitRequest.setEnabled(methodExistSwtich);
		classPrepareRequest.setEnabled(classPrepareSwtich);
		exceptionRequest.setEnabled(exceptionSwtich);
	}
}
