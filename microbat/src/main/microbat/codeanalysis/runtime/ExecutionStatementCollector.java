package microbat.codeanalysis.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.model.BreakPoint;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.StepRequest;

@SuppressWarnings("restriction")
public class ExecutionStatementCollector extends Executor{
	
	private boolean isOverLong = false;
	
	public List<BreakPoint> collectBreakPoints(AppJavaClassPath appClassPath){
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
							
							int lineNumber = location.lineNumber();
							BreakPoint breakPoint = new BreakPoint(location.declaringType().name(), lineNumber);
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
			} /*catch (AbsentInformationException e) {
				e.printStackTrace();
			}*/
			
			
		}
		
		if(vm != null){
			vm.exit(0);
			vm.dispose();
		}
		
		System.out.println("There are totally " + steps + " steps in this execution.");
		
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
		StepRequest sr = erm.createStepRequest(threadReference,  StepRequest.STEP_LINE, StepRequest.STEP_INTO);
		sr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		for(String ex: stepWatchExcludes){
			sr.addClassExclusionFilter(ex);
		}
		sr.enable();
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
