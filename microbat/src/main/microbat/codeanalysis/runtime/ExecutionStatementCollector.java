package microbat.codeanalysis.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

@SuppressWarnings("restriction")
public class ExecutionStatementCollector extends Executor {

	private boolean isOverLong = false;
	private List<BreakPoint> executionOrderList = new ArrayList<>();

	public List<BreakPoint> collectBreakPoints(AppJavaClassPath appClassPath, boolean isTestcaseEvaluation) {
		List<String> exlcudes = MicroBatUtil.extractExcludeFiles("", appClassPath.getExternalLibPaths());
		this.addLibExcludeList(exlcudes);

		steps = 0;
		List<BreakPoint> pointList = new ArrayList<>();

		VirtualMachine vm = new VMStarter(appClassPath).start();

		EventRequestManager erm = vm.eventRequestManager();
		addClassWatch(erm);

		EventQueue queue = vm.eventQueue();

		boolean connected = true;

		while (connected) {
			try {
				EventSet eventSet = queue.remove(TIME_OUT);
				if (eventSet != null) {
					for (Event event : eventSet) {
						if (event instanceof VMStartEvent) {
							System.out.println("start collecting execution...");

							ThreadReference thread = ((VMStartEvent) event).thread();
							addStepWatch(erm, thread);
							addExceptionWatch(erm);
							addMethodWatch(erm);
							addThreadStartWatch(erm);

							if (isTestcaseEvaluation) {
								disableAllStepRequests();
							} else {
								this.methodEntryRequest.disable();
								this.methodExitRequest.disable();
//								excludeJUnitLibs();
							}

						} 
						else if (event instanceof ThreadStartEvent) {
							ThreadReference threadReference = ((ThreadStartEvent) event).thread();
							if(hasValidThreadName(threadReference)) {
								addStepWatch(erm, threadReference);
								excludeJUnitLibs();		
								System.currentTimeMillis();
							}
						}
						else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
							connected = false;
						} else if (event instanceof ClassPrepareEvent) {
//							ClassPrepareEvent classPrepEvent = (ClassPrepareEvent) event;
//							ReferenceType refType = classPrepEvent.referenceType();
//							if(refType.sourceName().contains("RemoveUnusedVars")) {
//								System.currentTimeMillis();
//							}
//							addBreakPointWatch(erm, refType, anchorPoint);
						} else if (event instanceof StepEvent) {
							StepEvent sEvent = (StepEvent) event;
							Location location = sEvent.location();

							int lineNumber = location.lineNumber();

							String path = location.sourcePath();
							String declaringCompilationUnit = path.replace(".java", "");
							declaringCompilationUnit = declaringCompilationUnit.replace(File.separatorChar, '.');

							BreakPoint breakPoint = new BreakPoint(location.declaringType().name(),
									declaringCompilationUnit, lineNumber);
							// System.out.println(breakPoint);
							if(breakPoint.getDeclaringCompilationUnitName().contains("RemoveUnusedVars")) {
								System.currentTimeMillis();
							}

							if (!isInTestRunner(breakPoint)) {
								if (!pointList.contains(breakPoint)) {
									pointList.add(breakPoint);
								}

								this.executionOrderList.add(breakPoint);
								steps++;
							}

							if (steps >= Settings.stepLimit) {
								this.setOverLong(true);
								connected = false;
							}
						} else if (event instanceof MethodEntryEvent) {
							Method method = ((MethodEntryEvent) event).method();

							// System.out.println(method.declaringType().name() + "." + method.name());

							if (isTestcaseEvaluation) {
								String declaringTypeName = method.declaringType().name();
								// if(appClassPath.getOptionalTestClass().equals(declaringTypeName)){
								if (declaringTypeName.contains("junit.framework.TestResult")
										&& method.name().equals("startTest")) {
									enableAllStepRequests();
									this.methodEntryRequest.disable();
									this.methodExitRequest.disable();

									excludeJUnitLibs();
								}
							}
						} 
						else if (event instanceof ExceptionEvent) {
							System.currentTimeMillis();
						} 
					}

					eventSet.resume();
				} else {
					System.out.println("JVM time out when collecting statement");
					vm.exit(0);
					vm.dispose();
					connected = false;
				}

			} catch (InterruptedException e) {
				connected = false;
				e.printStackTrace();
			} catch (AbsentInformationException e) {
				e.printStackTrace();
			}

		}

		if (vm != null) {
			vm.exit(0);
			vm.dispose();
		}

		// System.out.println("There are totally " + steps + " steps in this
		// execution.");

		return pointList;
	}


	protected boolean isInTestRunner(BreakPoint breakPoint) {
		String className = breakPoint.getDeclaringCompilationUnitName();
		if (className.equals(TestCaseAnalyzer.TEST_RUNNER)) {
			return true;
		}
		return false;
	}

	protected void addBreakPointWatch(EventRequestManager erm, ReferenceType refType, List<ClassLocation> anchorPoint) {
		for (Iterator<ClassLocation> iterator = anchorPoint.iterator(); iterator.hasNext();) {
			ClassLocation classLocation = (ClassLocation) iterator.next();
			System.out.println(refType.name());
			if (classLocation.getClassCanonicalName().equals(refType.name())) {
				List<Location> listOfLocations;
				try {
					listOfLocations = refType.locationsOfLine(classLocation.getLineNumber());
					if (listOfLocations.size() == 0) {
						System.out.println("No element in the list of locations ");
						return;
					}
					Location loc = (Location)listOfLocations.get(0);
					breakpointRequest = erm.createBreakpointRequest(loc);
					breakpointRequest.setEnabled(true);
				} catch (AbsentInformationException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	/**
	 * add method enter and exit event
	 */
	private void addMethodWatch(EventRequestManager erm) {
		methodEntryRequest = erm.createMethodEntryRequest();
		// String[] stepWatchExcludes = { "java.*", "java.lang.*", "javax.*", "sun.*",
		// "com.sun.*"};
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

	/** add watch requests **/
	protected void addClassWatch(EventRequestManager erm) {
		classPrepareRequest = erm.createClassPrepareRequest();
		for (String ex : libExcludes) {
			classPrepareRequest.addClassExclusionFilter(ex);
		}
		classPrepareRequest.setEnabled(true);

	}

	protected void addExceptionWatch(EventRequestManager erm) {
		exceptionRequest = erm.createExceptionRequest(null, true, true);
		exceptionRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		for (String ex : libExcludes) {
			exceptionRequest.addClassExclusionFilter(ex);
		}
		exceptionRequest.enable();
	}

	public int getStepNum() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public boolean isOverLong() {
		return isOverLong;
	}

	public void setOverLong(boolean isOverLong) {
		this.isOverLong = isOverLong;
	}

	public List<BreakPoint> getExecutionOrderList() {
		return executionOrderList;
	}

	public void setExecutionOrderList(List<BreakPoint> executionOrderList) {
		this.executionOrderList = executionOrderList;
	}

}
