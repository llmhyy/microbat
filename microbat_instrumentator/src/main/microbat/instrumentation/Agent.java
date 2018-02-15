package microbat.instrumentation;

import microbat.instrumentation.trace.data.ExecutionTracer;
import microbat.instrumentation.trace.data.FilterChecker;
import microbat.instrumentation.trace.data.IExecutionTracer;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.sql.TraceRecorder;
import sav.strategies.dto.AppJavaClassPath;

public class Agent {
	private AgentParams agentParams;
	
	public Agent(String agentArgs) {
		agentParams = AgentParams.parse(agentArgs);
	}

	public void startup() {
		/* init filter */
		AppJavaClassPath appPath = new AppJavaClassPath();
		appPath.setLaunchClass(agentParams.getLaunchClass());
		appPath.setJavaHome(agentParams.getJavaHome());
		for(String cp: agentParams.getClassPaths()){
			appPath.addClasspath(cp);
		}
		appPath.setWorkingDirectory(agentParams.getWorkingDirectory());
		FilterChecker.setup(appPath);
		ExecutionTracer.appJavaClassPath = appPath;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					shutdown();	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void shutdown() throws Exception {
		ExecutionTracer.shutdown();
		/* collect trace & store */
		System.out.println("Recording trace...");
		IExecutionTracer tracer = ExecutionTracer.getMainThreadStore();
		TraceRecorder traceRecorder = new TraceRecorder();
		Trace trace = ((ExecutionTracer) tracer).getTrace();
		
		createVirtualDataRelation(trace);
		trace.constructControlDomianceRelation();
//		trace.constructLoopParentRelation();
		
		traceRecorder.storeTrace(trace );
		System.out.println("Finish recording.");
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
				TraceNode prevNode = trace.getTraceNode(order-1);
				currentNode.setStepInPrevious(prevNode);
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

	public static String extrctJarPath() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public AgentParams getAgentParams() {
		return agentParams;
	}

}
