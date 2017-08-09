package microbat.codeanalysis.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;

/**
 * This class is used to locate all the executor classes in this project
 * @author "linyun"
 *
 */
@SuppressWarnings("restriction")
public abstract class Executor {
	
	protected int steps = 0;
	
	public static int TIME_OUT = 100000;
	
	protected StepRequest stepRequest;
	protected MethodEntryRequest methodEntryRequest;
	protected MethodExitRequest methodExitRequest;
	protected ClassPrepareRequest classPrepareRequest;
	protected ExceptionRequest exceptionRequest;
	
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
	
	protected void excludeJUnitLibs(boolean step, boolean methodEntry, boolean methodExit, boolean classPrepare, boolean exception) {
		stepRequest.disable();
		methodEntryRequest.disable();
		methodExitRequest.disable();
		classPrepareRequest.disable();
		exceptionRequest.disable();
		
		for(String junitString: junitExcludes) {
			stepRequest.addClassExclusionFilter(junitString);
			methodEntryRequest.addClassExclusionFilter(junitString);
			methodExitRequest.addClassExclusionFilter(junitString);
			classPrepareRequest.addClassExclusionFilter(junitString);
			exceptionRequest.addClassExclusionFilter(junitString);
		}
		
		stepRequest.setEnabled(step);
		methodEntryRequest.setEnabled(methodEntry);
		methodExitRequest.setEnabled(methodExit);
		classPrepareRequest.setEnabled(classPrepare);
		exceptionRequest.setEnabled(exception);
	}
}
