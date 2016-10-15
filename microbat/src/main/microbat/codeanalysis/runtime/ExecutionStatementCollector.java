package microbat.codeanalysis.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.StepRequest;

import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.model.BreakPoint;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

@SuppressWarnings("restriction")
public class ExecutionStatementCollector extends Executor{
	
	private boolean isOverLong = false;
	
	public List<BreakPoint> collectBreakPoints(AppJavaClassPath appClassPath, boolean isTestcaseEvaluation){
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
							System.out.println("start collecting execution");
							
							ThreadReference thread = ((VMStartEvent) event).thread();
							addStepWatch(erm, thread);
							addExceptionWatch(erm);
							
							if(isTestcaseEvaluation){
//								this.stepRequest.disable();
								addMethodWatch(erm);								
							}
							
						}
						else if(event instanceof VMDeathEvent
							|| event instanceof VMDisconnectEvent){
							connected = false;
						}
						else if(event instanceof StepEvent){
							StepEvent sEvent = (StepEvent)event;
							Location location = sEvent.location();
							
//							String path = location.sourcePath();
//							path = path.substring(0, path.indexOf(".java"));
//							path = path.replace(File.separator, ".");
//							
//							System.out.println(location);
							
							int lineNumber = location.lineNumber();
							
							String path = location.sourcePath();
							String declaringCompilationUnit = path.replace(".java", "");
							declaringCompilationUnit = declaringCompilationUnit.replace(File.separatorChar, '.');
							
							
							BreakPoint breakPoint = new BreakPoint(location.declaringType().name(), declaringCompilationUnit, lineNumber);
//							System.out.println(breakPoint);

							if(!isInTestRunner(breakPoint) && !pointList.contains(breakPoint)){
								pointList.add(breakPoint);							
							}
							
							steps++;
							if(steps >= Settings.stepLimit){
								this.setOverLong(true);
								connected = false;
							}
						}
						else if(event instanceof MethodEntryEvent){
							Method method = ((MethodEntryEvent) event).method();
							
							String declaringTypeName = method.declaringType().name();
							String methodName = method.name();
							if((declaringTypeName.equals("junit.framework.TestResult") && methodName.equals("run")) ||
									(declaringTypeName.equals("org.junit.runners.BlockJUnit4ClassRunner")) && methodName.equals("methodBlock")){
								this.stepRequest.enable();
								this.methodEntryRequest.disable();
							}
						}
						else if(event instanceof ExceptionEvent){
							System.currentTimeMillis();
						}
					}
					
					eventSet.resume();
				}
				else{
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
	
	private StepRequest stepRequest;
	
	protected void addStepWatch(EventRequestManager erm, ThreadReference threadReference) {
		stepRequest = erm.createStepRequest(threadReference,  StepRequest.STEP_LINE, StepRequest.STEP_INTO);
		stepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		
		for(String ex: stepWatchExcludes){
			stepRequest.addClassExclusionFilter(ex);
		}
		stepRequest.enable();
	}
	
	private MethodEntryRequest methodEntryRequest;
	/**
	 * add method enter and exit event
	 */
	private void addMethodWatch(EventRequestManager erm) {
		methodEntryRequest = erm.createMethodEntryRequest();
		
		String[] stepWatchExcludes = { "java.*", "java.lang.*", "javax.*", "sun.*", "com.sun.*"};
		
		for (String classPattern : stepWatchExcludes) {
			methodEntryRequest.addClassExclusionFilter(classPattern);
		}
		methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		methodEntryRequest.enable();
	}
	
	/** add watch requests **/
	protected void addClassWatch(EventRequestManager erm) {
		ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
//		classPrepareRequest.addClassFilter("com.Main");
		for(String ex: stepWatchExcludes){
			classPrepareRequest.addClassExclusionFilter(ex);
		}
		classPrepareRequest.setEnabled(true);
	}
	
	protected void addExceptionWatch(EventRequestManager erm) {
		
		ExceptionRequest request = erm.createExceptionRequest(null, true, true);
		request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		for(String ex: stepWatchExcludes){
			request.addClassExclusionFilter(ex);
		}
		request.enable();
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
}
