package microbat.codeanalysis.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.PackageFragment;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
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
import microbat.util.FilterUtils;
import microbat.util.JavaUtil;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.StringUtils;

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
	
	public static String[] libExcludes = { 
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
			"javax.*", "sun.*", "com.sun.*", 
			"org.omg.*",
			"com.oracle.*",
			"org.ietf.*",
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
	
	/**
	 * This method derive more detailed libExcludes with libIncludes, for example, 
	 * when libExcludes has a pattern as java.* while libIncludes has a pattern as java.util.*,
	 * then the method can split java.* into a list including java.awt.*, java.lang.*, ..., except
	 * java.util.*. 
	 *
	 * @return
	 */
	public static String[] deriveLibExcludePatterns() {
		Set<String> newExcludeCol = CollectionUtils.toHashSet(libExcludes);
		IJavaProject ijavaProject = JavaCore.create(JavaUtil.getSpecificJavaProjectInWorkspace());
		for (String incl : libIncludes) {
			Set<String> expandedExclSet = new HashSet<String>();
			for (Iterator<String> it = newExcludeCol.iterator(); it.hasNext();) {
				String excl = it.next();
				if (excl.equals(incl)) {
					it.remove();
					break;
				}
				if (FilterUtils.isSubFilter(incl, excl)) {
					it.remove();
					expandPkgFilter(ijavaProject, excl, incl, expandedExclSet);
				}
			}
			newExcludeCol.addAll(expandedExclSet);
		}
		return StringUtils.sortAlphanumericStrings(new ArrayList<>(newExcludeCol)).toArray(new String[0]);
	}
	
	/**
	 *  java.util.* : include all types and packages under java.util package
	 *  java.util.*\ : include all types only under java.util package
	 *  java.util.Arrays : include type Arrays only
	 *  java.util.Arrays\ : include type Arrays and its inner types
	 */
	private static void expandPkgFilter(IJavaProject javaProject, String pkgFilter, String incl,
			Set<String> expandedExclSet) {
		try {
			List<IPackageFragment> matches = new ArrayList<IPackageFragment>();
			String pkgName = FilterUtils.getPrefix(pkgFilter);
			/* find packages match pkgFilter */
			for (IPackageFragmentRoot pkgRoot : javaProject.getPackageFragmentRoots()) {
				IPackageFragment pkg = pkgRoot.getPackageFragment(pkgName);
				if (pkg.exists()) {
					matches.add(pkg);
				}
			}
			/* get include package */
			String[] inclFrags;
			String inclTypeSimpleName = null;
			String subTypePrefix = null;
			int typeSimpleNameSIdx = incl.lastIndexOf(".");
			inclFrags = StringUtils.dotSplit(incl.substring(0, typeSimpleNameSIdx));
			if (incl.endsWith("*\\")) {
				inclTypeSimpleName = "";
			} else if (!incl.endsWith("*")) {
				if (!incl.endsWith("\\")) {
					inclTypeSimpleName = incl.substring(typeSimpleNameSIdx + 1);
					subTypePrefix = inclTypeSimpleName + "$";
				} else {
					inclTypeSimpleName = incl.substring(typeSimpleNameSIdx + 1, incl.length() - 1);
				}
			} 
			/* build up exclude list */
			for (IPackageFragment pkg : matches) {
				/* add exclude packages */
				for (IJavaElement otherPkg : ((IPackageFragmentRoot) pkg.getParent()).getChildren()) {
					String[] otherPkgFrags = ((PackageFragment) otherPkg).names;
					int i = 0;
					for (i = 0; i < inclFrags.length && i < otherPkgFrags.length; i++) {
						if (!inclFrags[i].equals(otherPkgFrags[i])) {
							break;
						}
					}
					if (i == otherPkgFrags.length) {
						/* ignore include package's parent */
						continue;
					}
					if (i < inclFrags.length) {
						expandedExclSet.add(FilterUtils.toPkgFilterText(otherPkgFrags, 0, i));
					} else if (i == inclFrags.length && i < otherPkgFrags.length  && inclTypeSimpleName != null) {
						/* add sub-package of include package */
						expandedExclSet.add(FilterUtils.toPkgFilterText(otherPkgFrags, 0, i));
					} 
				}
				/* add exclude types */
				if (inclTypeSimpleName != null) {
					for (IJavaElement child : pkg.getChildren()) {
						if (child.getElementType() == IJavaElement.CLASS_FILE) {
							String typeSimpleName = child.getElementName().replace(".class", "");
							if (inclTypeSimpleName.isEmpty() || inclTypeSimpleName.equals(typeSimpleName)
									|| (subTypePrefix != null && typeSimpleName.startsWith(subTypePrefix))) {
								continue;
							}
							expandedExclSet.add(StringUtils.dotJoin(pkgName, typeSimpleName));
						}
					}
				}
			}
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
	
	/** add watch requests **/
	protected void addClassWatch(EventRequestManager erm) {
		classPrepareRequest = erm.createClassPrepareRequest();
		for (String ex : libExcludes) {
			classPrepareRequest.addClassExclusionFilter(ex);
		}
		classPrepareRequest.setEnabled(true);
	}
	
	/**
	 * add method enter and exit event
	 */
	protected void addMethodWatch(EventRequestManager erm) {
		methodEntryRequest = erm.createMethodEntryRequest();
		for (String classPattern : libExcludes) {
			methodEntryRequest.addClassExclusionFilter(classPattern);
		}
		methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		methodEntryRequest.enable();

		methodExitRequest = erm.createMethodExitRequest();
		for (String classPattern : libExcludes) {
			methodExitRequest.addClassExclusionFilter(classPattern);
		}
		methodExitRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		methodExitRequest.enable();
	}
	
	protected void addExceptionWatch(EventRequestManager erm) {
		exceptionRequest = erm.createExceptionRequest(null, true, true);
		exceptionRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		for (String ex : libExcludes) {
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
		
		methodEntryRequest.setEnabled(methodEntrySwtich);
		methodExitRequest.setEnabled(methodExistSwtich);
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
}
