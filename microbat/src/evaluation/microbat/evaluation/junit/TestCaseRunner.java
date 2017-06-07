package microbat.evaluation.junit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import microbat.codeanalysis.runtime.ExecutionStatementCollector;
import microbat.codeanalysis.runtime.VMStarter;
import microbat.model.BreakPoint;
import microbat.util.JTestUtil;
import microbat.util.JavaUtil;
import microbat.util.Settings;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import sav.strategies.dto.AppJavaClassPath;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.BooleanType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

@SuppressWarnings("restriction")
public class TestCaseRunner extends ExecutionStatementCollector{
	
	private static final int FINISH_LINE_NO_IN_TEST_RUNNER = 47;

	private boolean isPassingTest = false;
	private boolean hasCompilationError = false;
	
	/**
	 * check whether it is a passing test case and having compilation error, however,
	 * it cannot check whether it is over long.
	 * @param appClassPath
	 */
	public void checkValidity(AppJavaClassPath appClassPath){
		VirtualMachine vm = new VMStarter(appClassPath).start();
		
		EventRequestManager erm = vm.eventRequestManager(); 
		addClassWatch(erm);
		
		EventQueue queue = vm.eventQueue();
		boolean connected = true;
		
		while(connected){
			try {
				EventSet eventSet = queue.remove(10000);
				if(eventSet != null){
					for(Event event: eventSet){
						if(event instanceof VMStartEvent){
//							ThreadReference thread = ((VMStartEvent) event).thread();
//							addStepWatch(erm, thread);
//							addExceptionWatch(erm);
						}
						else if(event instanceof VMDeathEvent
							|| event instanceof VMDisconnectEvent){
							connected = false;
						}
						else if(event instanceof ClassPrepareEvent){
							ClassPrepareEvent cEvent = (ClassPrepareEvent)event;
							ReferenceType refType = cEvent.referenceType();
							String className = refType.toString();
							if(className.equals("microbat.evaluation.junit.MicroBatTestRunner")){
								List<Location> locations = refType.locationsOfLine(FINISH_LINE_NO_IN_TEST_RUNNER);
								if(!locations.isEmpty()){
									Location loc = locations.get(0);
									addBreakPointWatch(erm, ((ClassPrepareEvent) event).thread(), loc);
								}
							}
						}
						else if(event instanceof BreakpointEvent){
							BreakpointEvent bEvent = (BreakpointEvent)event;
							Location location = bEvent.location();
							
							checkTestCaseSucessfulness(((BreakpointEvent) event).thread(), location);
						}
						else if(event instanceof ExceptionEvent){
							System.currentTimeMillis();
						}
					}
					
					eventSet.resume();
				}
				else{
					connected = false;
//					vm.dispose();
//					vm.exit(0);
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
	}
	
	protected void addBreakPointWatch(EventRequestManager erm, ThreadReference threadReference, Location loc) {
		BreakpointRequest request = erm.createBreakpointRequest(loc);
		request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		request.enable();
	}
	
	private void checkTestCaseSucessfulness(ThreadReference thread, Location location) {
		StackFrame currentFrame = null;
		try {
			for (StackFrame frame : thread.frames()) {
				if (frame.location().equals(location)) {
					currentFrame = frame;
					break;
				}
			}
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		}
		
		if(currentFrame != null){
			ReferenceType refTpe = currentFrame.thisObject().referenceType();
			
			for(Field field: refTpe.allFields()){
				if(field.name().equals("successful")){
					Value value = currentFrame.thisObject().getValue(field);
					if(value.type() instanceof BooleanType){
						BooleanValue booleanValue = (BooleanValue)value;
						this.isPassingTest = booleanValue.booleanValue();
					}
				}
				
				if(field.name().equals("failureMessage")){
					Value value = currentFrame.thisObject().getValue(field);
					if(value != null){
						String message = value.toString();
						if(message.contains("Unresolved compilation problem:")){
							this.setHasCompilationError(true);
						}
								
					}
					
				}
			}
		}
	}

	

	protected HashMap<BreakPoint, Boolean> pointInTestRunnerMap = new HashMap<>(); 
	
	

	private void appendStepWatchExcludes(AppJavaClassPath appClassPath) {
		List<String> exList = new ArrayList<>();
		for(String ex: stepWatchExcludes){
			exList.add(ex);
		}
		
		if(appClassPath.getOptionalTestClass() != null){
			exList.add(appClassPath.getOptionalTestClass());			
		}
		
		this.stepWatchExcludes = exList.toArray(new String[0]);
	}

	public boolean isPassingTest() {
		return isPassingTest;
	}

	public void setPassingTest(boolean isPassingTest) {
		this.isPassingTest = isPassingTest;
	}

	public boolean hasCompilationError() {
		return hasCompilationError;
	}

	public void setHasCompilationError(boolean hasCompilationError) {
		this.hasCompilationError = hasCompilationError;
	}
}
