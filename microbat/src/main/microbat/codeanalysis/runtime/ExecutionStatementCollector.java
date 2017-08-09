package microbat.codeanalysis.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.model.BreakPoint;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

@SuppressWarnings("restriction")
public class ExecutionStatementCollector extends Executor{
	
	private boolean isOverLong = false;
	private List<BreakPoint> executionOrderList = new ArrayList<>();
	
	public List<BreakPoint> collectBreakPoints(AppJavaClassPath appClassPath, boolean isTestcaseEvaluation){
		List<String> exlcudes = MicroBatUtil.extractExcludeFiles("", appClassPath.getExternalLibPaths());
		this.addLibExcludeList(exlcudes);
		
		steps = 0;
		List<BreakPoint> pointList = new ArrayList<>();
		
		VirtualMachine vm = new VMStarter(appClassPath).start();
		
		EventRequestManager erm = vm.eventRequestManager(); 
		addClassWatch(erm);
		
		EventQueue queue = vm.eventQueue();
		
		boolean connected = true;
		
		while(connected){
			try {
				EventSet eventSet = queue.remove(TIME_OUT);
				if(eventSet != null){
					for(Event event: eventSet){
						if(event instanceof VMStartEvent){
							System.out.println("start collecting execution...");
							
							ThreadReference thread = ((VMStartEvent) event).thread();
							addStepWatch(erm, thread);
							addExceptionWatch(erm);
							addMethodWatch(erm);								
							
							if(isTestcaseEvaluation){
								this.stepRequest.disable();
							}
							else {
								this.methodEntryRequest.disable();
								this.methodExitRequest.disable();
								excludeJUnitLibs(this.stepRequest.isEnabled(), this.methodEntryRequest.isEnabled(), this.methodExitRequest.isEnabled(),
										this.classPrepareRequest.isEnabled(), this.exceptionRequest.isEnabled());
							}
							
						}
						else if(event instanceof VMDeathEvent
							|| event instanceof VMDisconnectEvent){
							connected = false;
						}
						else if(event instanceof ClassPrepareEvent){
						}
						else if(event instanceof StepEvent){
							StepEvent sEvent = (StepEvent)event;
							Location location = sEvent.location();
							
							int lineNumber = location.lineNumber();
							
							String path = location.sourcePath();
							String declaringCompilationUnit = path.replace(".java", "");
							declaringCompilationUnit = declaringCompilationUnit.replace(File.separatorChar, '.');
							
							
							BreakPoint breakPoint = new BreakPoint(location.declaringType().name(), declaringCompilationUnit, lineNumber);
//							System.out.println(breakPoint);

							if(!isInTestRunner(breakPoint)){
								if(!pointList.contains(breakPoint)) {
									pointList.add(breakPoint);																
								}
								
								this.executionOrderList.add(breakPoint);
								steps++;
							}
							
							if(steps >= Settings.stepLimit){
								this.setOverLong(true);
								connected = false;
							}
						}
						else if(event instanceof MethodEntryEvent){
							Method method = ((MethodEntryEvent) event).method();
							
//							System.out.println(method.declaringType().name() + "." + method.name());
							
							if(isTestcaseEvaluation){
								String declaringTypeName = method.declaringType().name();
								//if(appClassPath.getOptionalTestClass().equals(declaringTypeName)){
								if(declaringTypeName.contains("junit.framework.TestResult") && method.name().equals("startTest")) {
									this.stepRequest.enable();
									this.methodEntryRequest.disable();
									this.methodExitRequest.disable();
									
									excludeJUnitLibs(this.stepRequest.isEnabled(), this.methodEntryRequest.isEnabled(), this.methodExitRequest.isEnabled(),
											this.classPrepareRequest.isEnabled(), this.exceptionRequest.isEnabled());
								}
							}
						}
						else if(event instanceof ExceptionEvent){
							System.currentTimeMillis();
						}
					}
					
					eventSet.resume();
				}
				else{
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
		
		if(vm != null){
			vm.exit(0);
			vm.dispose();
		}
		
//		System.out.println("There are totally " + steps + " steps in this execution.");
		
		return pointList;
	}
	
	protected boolean isInTestRunner(BreakPoint breakPoint){
		String className = breakPoint.getDeclaringCompilationUnitName();
		if(className.equals(TestCaseAnalyzer.TEST_RUNNER)){
			return true;
		}
		return false;
	}
	
	
	protected void addStepWatch(EventRequestManager erm, ThreadReference threadReference) {
		stepRequest = erm.createStepRequest(threadReference,  StepRequest.STEP_LINE, StepRequest.STEP_INTO);
		stepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		
//		String[] stepWatchExcludes = { "java.*", "java.lang.*", "javax.*", "sun.*", "com.sun.*"};
		for(String ex: libExcludes){
			stepRequest.addClassExclusionFilter(ex);
		}
		
		stepRequest.enable();
	}
	
	/**
	 * add method enter and exit event
	 */
	private void addMethodWatch(EventRequestManager erm) {
		methodEntryRequest = erm.createMethodEntryRequest();
//		String[] stepWatchExcludes = { "java.*", "java.lang.*", "javax.*", "sun.*", "com.sun.*"};
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
		for(String ex: libExcludes){
			classPrepareRequest.addClassExclusionFilter(ex);
		}
		classPrepareRequest.setEnabled(true);
		
	}
	
	
	protected void addExceptionWatch(EventRequestManager erm) {
		exceptionRequest = erm.createExceptionRequest(null, true, true);
		exceptionRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		for(String ex: libExcludes){
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
