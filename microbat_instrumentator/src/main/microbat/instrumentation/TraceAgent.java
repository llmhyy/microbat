package microbat.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.List;

import microbat.instrumentation.filter.CodeRangeUserFilter;
import microbat.instrumentation.filter.GlobalFilterChecker;
import microbat.instrumentation.filter.OverLongMethodFilter;
import microbat.instrumentation.instr.TraceTransformer;
import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.output.TraceOutputWriter;
import microbat.instrumentation.output.tcp.TcpConnector;
import microbat.instrumentation.runtime.ExecutionTracer;
import microbat.instrumentation.runtime.IExecutionTracer;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.sql.Recorder;
import sav.common.core.utils.StopTimer;
import sav.strategies.dto.AppJavaClassPath;

public class TraceAgent implements IAgent {
	private AgentParams agentParams;
	private StopTimer timer;
	
	public TraceAgent(CommandLine cmd) {
		this.agentParams = AgentParams.initFrom(cmd);
	}

	public void startup(long vmStartupTime, long agentPreStartup) {
		timer = new StopTimer("Trace Construction");
		timer.newPoint("Execution");
		/* init filter */
		AppJavaClassPath appPath = agentParams.initAppClassPath();
		GlobalFilterChecker.setup(appPath, agentParams.getIncludesExpression(), agentParams.getExcludesExpression());
		ExecutionTracer.appJavaClassPath = appPath;
		ExecutionTracer.variableLayer = agentParams.getVariableLayer();
		ExecutionTracer.setStepLimit(agentParams.getStepLimit());
		if (!agentParams.isRequireMethodSplit()) {
			agentParams.getUserFilters().register(new OverLongMethodFilter(agentParams.getOverlongMethods()));
		}
		
		if (!agentParams.getCodeRanges().isEmpty()) {
			agentParams.getUserFilters().register(new CodeRangeUserFilter(agentParams.getCodeRanges()));
		}
		
		ExecutionTracer.setExpectedSteps(agentParams.getExpectedSteps());
		ExecutionTracer.avoidProxyToString = agentParams.isAvoidProxyToString();
	}

	public void shutdown() throws Exception {
		ExecutionTracer.shutdown();
		/* collect trace & store */
		AgentLogger.debug("Building trace dependencies ...");
		timer.newPoint("Building trace dependencies");
		// FIXME -mutithread LINYUN [3]
		// LLT: only trace of main thread is recorded.
		List<IExecutionTracer> tracers = ExecutionTracer.getAllThreadStore();
		
		System.currentTimeMillis();
		
		IExecutionTracer tracer = ExecutionTracer.getMainThreadStore();
	
		Trace trace = ((ExecutionTracer) tracer).getTrace();
		GlobalFilterChecker.addFilterInfo(trace);
		
		StepMismatchChecker.logNormalSteps(trace);
		ExecutionTracer.dispose(); // clear cache
		long t1 = System.currentTimeMillis();
		AgentLogger.debug("create VirtualDataRelation....");
		createVirtualDataRelation(trace);
		long t2 = System.currentTimeMillis();
		AgentLogger.debug("time for createVirtualDataRelation: "  + (t2-t1)/1000);
		
		t1 = System.currentTimeMillis();
		AgentLogger.debug("construct ControlDomianceRelation....");
		trace.constructControlDomianceRelation();
		t2 = System.currentTimeMillis();
	
//		trace.constructLoopParentRelation();
		timer.newPoint("Saving trace");
		t1 = System.currentTimeMillis();
		Recorder.create(agentParams).store(trace);
		t2 = System.currentTimeMillis();

		AgentLogger.debug(timer.getResultString());
	}

	private void writeOutput(Trace trace) throws Exception {
		AgentLogger.debug("Saving trace...");
		
		if (agentParams.getDumpFile() != null) {
			RunningInfo result = new RunningInfo();
			result.setProgramMsg(Agent.getProgramMsg());
			result.setTrace(trace);
			result.setCollectedSteps(trace.getExecutionList().size());
			result.setExpectedSteps(agentParams.getExpectedSteps());
			result.saveToFile(agentParams.getDumpFile(), false);
			AgentLogger.debug(result.toString());
		} else if (agentParams.getTcpPort() != AgentConstants.UNSPECIFIED_INT_VALUE) {
			TcpConnector tcpConnector = new TcpConnector(agentParams.getTcpPort());
			TraceOutputWriter traceWriter = tcpConnector.connect();
			traceWriter.writeString(Agent.getProgramMsg());
			traceWriter.writeTrace(trace);
			traceWriter.flush();
			Thread.sleep(10000l);
			tcpConnector.close();
		} 
		
		AgentLogger.debug("Trace saved.");
	}
	
	private void createVirtualDataRelation(Trace trace) {
		for(int i=0; i<trace.size(); i++){
			int order = i+1;
			TraceNode currentNode = trace.getTraceNode(order);
			if(order<trace.size()){
				TraceNode nextNode = trace.getTraceNode(order+1);
				currentNode.setStepInNext(nextNode);
				nextNode.setStepInPrevious(currentNode);
			}
			else if(order==trace.size()){
				if(order>1){
					TraceNode prevNode = trace.getTraceNode(order-1);
					currentNode.setStepInPrevious(prevNode);					
				}
			}
			
			TraceNode previousStepOver = currentNode.getStepOverPrevious();
			if(previousStepOver!=null && 
					previousStepOver.getClassCanonicalName().equals(currentNode.getClassCanonicalName()) &&
					Math.abs(previousStepOver.getLineNumber()-currentNode.getLineNumber())<=0){
				for(VarValue readVar: previousStepOver.getReadVariables()){
					if(!currentNode.containReadVariable(readVar)){
						currentNode.addReadVariable(readVar);
					}
				}
			}
			
		}
	}
	
	public AgentParams getAgentParams() {
		return agentParams;
	}

	@Override
	public void startTest(String junitClass, String junitMethod) {
		ExecutionTracer._start();
		ExecutionTracer.appJavaClassPath.setOptionalTestClass(junitClass);
		ExecutionTracer.appJavaClassPath.setOptionalTestMethod(junitMethod);
	}

	@Override
	public void finishTest(String junitClass, String junitMethod) {
		ExecutionTracer.shutdown();
	}

	@Override
	public TraceTransformer getTransformer() {
		return new TraceTransformer(agentParams);
	}

	@Override
	public void retransformBootstrapClasses(Instrumentation instrumentation, Class<?>[] retransformableClasses)
			throws Exception {
		instrumentation.retransformClasses(retransformableClasses);
	}

	@Override
	public void exitTest(String testResultMsg, String junitClass, String junitMethod, long threadId) {
		// do nothing, not used.
	}

	@Override
	public boolean isInstrumentationActive() {
		return !ExecutionTracer.isShutdown();
	}
}
