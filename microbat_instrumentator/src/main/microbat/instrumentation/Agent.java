package microbat.instrumentation;

import microbat.instrumentation.filter.FilterChecker;
import microbat.instrumentation.output.TraceOutputWriter;
import microbat.instrumentation.output.file.TraceFileRecorder;
import microbat.instrumentation.output.tcp.TcpConnector;
import microbat.instrumentation.precheck.TraceMeasurement;
import microbat.instrumentation.runtime.ExecutionTracer;
import microbat.instrumentation.runtime.IExecutionTracer;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.Variable;
import microbat.sql.TraceRecorder;
import sav.common.core.utils.StopTimer;
import sav.strategies.dto.AppJavaClassPath;

public class Agent {
	private AgentParams agentParams;
	private static String programMsg = "";
	public static boolean isPrecheck;
	
	public Agent(String agentArgs) {
		agentParams = AgentParams.parse(agentArgs);
	}

	public void startup() {
		final StopTimer timer = new StopTimer("Trace Construction");
		timer.newPoint("Execution");
		/* init filter */
		AppJavaClassPath appPath = agentParams.initAppClassPath();
		FilterChecker.setup(appPath, agentParams.getIncludesExpression(), agentParams.getExcludesExpression());
		ExecutionTracer.appJavaClassPath = appPath;
		ExecutionTracer.variableLayer = agentParams.getVariableLayer();
		ExecutionTracer.stepLimit = agentParams.getStepLimit();
		isPrecheck = agentParams.isPrecheck();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					shutdown(timer);	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void shutdown(StopTimer timer) throws Exception {
		ExecutionTracer.shutdown();
		/* collect trace & store */
		System.out.println("Building trace dependencies ...");
		timer.newPoint("Building trace dependencies");
		IExecutionTracer tracer = ExecutionTracer.getMainThreadStore();
	
		Trace trace = ((ExecutionTracer) tracer).getTrace();
		
//		List<ClassLocation> locs = new ArrayList<>();
//		for (TraceNode node : trace.getExecutionList()) {
//			BreakPoint bkp = node.getBreakPoint();
//			locs.add(new ClassLocation(bkp.getClassCanonicalName(), bkp.getMethodSign(), bkp.getLineNumber()));
//		}
//		FileUtils.writeFile("E:/lyly/WorkingFolder/step_run.txt", locs.toString());
//		System.out.println("Trace size = " + trace.getExecutionList().size());
		
		createVirtualDataRelation(trace);
		trace.constructControlDomianceRelation();
//		trace.constructLoopParentRelation();
		timer.newPoint("Saving trace");
		writeOutput(trace);
		System.out.println(timer.getResultString());
	}

	private void writeOutput(Trace trace) throws Exception {
		System.out.println("Saving trace...");
		if (agentParams.getDumpFile() != null) {
			TraceFileRecorder traceRecorder = new TraceFileRecorder(agentParams.getDumpFile());
			traceRecorder.writeTrace(programMsg, trace, false);
		} else if (agentParams.getTcpPort() != -1) {
			TcpConnector tcpConnector = new TcpConnector(agentParams.getTcpPort());
			TraceOutputWriter traceWriter = tcpConnector.connect();
			traceWriter.writeString(programMsg);
			traceWriter.writeTrace(trace);
			traceWriter.flush();
			Thread.sleep(10000l);
			tcpConnector.close();
		} else {
			TraceRecorder traceRecorder = new TraceRecorder();
			traceRecorder.storeTrace(trace );
		}
		System.out.println("Trace saved.");
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
			if(previousStepOver!=null && previousStepOver.getBreakPoint().equals(currentNode.getBreakPoint())){
				for(VarValue readVar: previousStepOver.getReadVariables()){
					if(!currentNode.getReadVariables().contains(readVar)){
						currentNode.addReadVariable(readVar);
					}
				}
			}
			
			if(currentNode.getInvocationParent()!=null && !currentNode.getPassParameters().isEmpty()){
				TraceNode invocationParent = currentNode.getInvocationParent();
				TraceNode firstChild = invocationParent.getInvocationChildren().get(0);
				if(firstChild.getOrder()==currentNode.getOrder()){
					for(VarValue value: currentNode.getPassParameters()){
						String varID = value.getVarID();
						StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
						if(entry==null){
							entry = new StepVariableRelationEntry(varID);
						}
						entry.addProducer(invocationParent);
						trace.getStepVariableTable().put(varID, entry);
					}
				}
			}
			
			if(currentNode.getInvocationParent()!=null && !currentNode.getReturnedVariables().isEmpty()){
				TraceNode invocationParent = currentNode.getInvocationParent();
				TraceNode stepOverNext = invocationParent.getStepOverNext();
				System.currentTimeMillis();
				if(stepOverNext!=null){
					for(VarValue value: currentNode.getReturnedVariables()){
						currentNode.addWrittenVariable(value);
						stepOverNext.addReadVariable(value);
						String varID = value.getVarID();
						String definingOrder = trace.findDefiningNodeOrder(Variable.WRITTEN, currentNode, varID, varID);
						varID = varID+":"+definingOrder;
						value.setVarID(varID);
						StepVariableRelationEntry entry = trace.getStepVariableTable().get(varID);
						if(entry==null){
							entry = new StepVariableRelationEntry(varID);
						}
						entry.addProducer(currentNode);
						entry.addConsumer(stepOverNext);
						trace.getStepVariableTable().put(varID, entry);
					}
				}
			}
		}
	}
	
	public static void _exitProgram(String programMsg) {
		Agent.programMsg = programMsg;
		Runtime.getRuntime().exit(1); // force program to exit to avoid getting stuck by background running threads.
	}
	
	public static String getProgramMsg() {
		return programMsg;
	}
	
	public static void _startTest(String junitClass, String junitMethod) {
		if (isPrecheck) {
			TraceMeasurement.startTest();
		} else {
			ExecutionTracer._start();
		}
	}
	
	public static void _finishTest(String junitClass, String junitMethod) {
		if (isPrecheck) {
			TraceMeasurement.shutdown();
		} else {
			ExecutionTracer.shutdown();
		}
	}

	public static String extrctJarPath() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public AgentParams getAgentParams() {
		return agentParams;
	}

}
