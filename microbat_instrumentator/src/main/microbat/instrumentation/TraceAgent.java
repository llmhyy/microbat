package microbat.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.tree.VariableHeightLayoutCache;

import microbat.instrumentation.filter.CodeRangeUserFilter;
import microbat.instrumentation.filter.GlobalFilterChecker;
import microbat.instrumentation.filter.OverLongMethodFilter;
import microbat.instrumentation.instr.TraceTransformer;
import microbat.instrumentation.runtime.ExecutionTracer;
import microbat.instrumentation.runtime.IExecutionTracer;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.sql.Recorder;
import sav.strategies.dto.AppJavaClassPath;

public class TraceAgent extends Agent {
	private AgentParams agentParams;
//	private StopTimer timer;

	public TraceAgent(CommandLine cmd) {
		this.agentParams = AgentParams.initFrom(cmd);
	}

	public void startup0(long vmStartupTime, long agentPreStartup) {
//		timer = new StopTimer("Trace Construction");
//		timer.newPoint("Execution");
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
//		timer.newPoint("Building trace dependencies");
		// FIXME -mutithread LINYUN [3]
		// LLT: only trace of main thread is recorded.
		List<IExecutionTracer> tracers = ExecutionTracer.getAllThreadStore();

		int size = tracers.size();
		List<Trace> traceList = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {

			ExecutionTracer tracer = (ExecutionTracer) tracers.get(i);

			Trace trace = tracer.getTrace();
			trace.setThreadId(tracer.getThreadId());
			trace.setThreadName(tracer.getThreadName());
			trace.setMain(ExecutionTracer.getMainThreadStore().equals(tracer));

			constructTrace(trace);
			
			addConditionResult(trace);
			changeRedefinedVarID(trace);
			matchArrayElementName(trace);
			traceList.add(trace);
		}

//		timer.newPoint("Saving trace");
		Recorder.create(agentParams).store(traceList);
//		AgentLogger.debug(timer.getResultString());
	}

	//FIXME this method can be handled in an asynchronized way
	public void constructTrace(Trace trace) {
		GlobalFilterChecker.addFilterInfo(trace);

		StepMismatchChecker.logNormalSteps(trace);
		ExecutionTracer.dispose(); // clear cache
		long t1 = System.currentTimeMillis();
		AgentLogger.debug("create VirtualDataRelation....");
		createVirtualDataRelation(trace);
		long t2 = System.currentTimeMillis();
		AgentLogger.debug("time for createVirtualDataRelation: " + (t2 - t1) / 1000);

		// TODO Xuezhi we need to comment the code to build control dependencies here.
		t1 = System.currentTimeMillis();
		AgentLogger.debug("construct ControlDomianceRelation....");
		trace.constructControlDomianceRelation();
		t2 = System.currentTimeMillis();

		// trace.constructLoopParentRelation();

	}

	private void createVirtualDataRelation(Trace trace) {
		for (int i = 0; i < trace.size(); i++) {
			int order = i + 1;
			TraceNode currentNode = trace.getTraceNode(order);
			if (order < trace.size()) {
				TraceNode nextNode = trace.getTraceNode(order + 1);
				currentNode.setStepInNext(nextNode);
				nextNode.setStepInPrevious(currentNode);
			} else if (order == trace.size()) {
				if (order > 1) {
					TraceNode prevNode = trace.getTraceNode(order - 1);
					currentNode.setStepInPrevious(prevNode);
				}
			}

			TraceNode previousStepOver = currentNode.getStepOverPrevious();
			if (previousStepOver != null
					&& previousStepOver.getClassCanonicalName().equals(currentNode.getClassCanonicalName())
					&& Math.abs(previousStepOver.getLineNumber() - currentNode.getLineNumber()) <= 0) {
				for (VarValue readVar : previousStepOver.getReadVariables()) {
					if (!currentNode.containReadVariable(readVar)) {
						currentNode.addReadVariable(readVar);
					}
				}
			} 
			
			if (currentNode.getInvocationParent() != null && !currentNode.getPassParameters().isEmpty()) {
				TraceNode invocationParent = currentNode.getInvocationParent();
				TraceNode firstChild = invocationParent.getInvocationChildren().get(0);
				if (firstChild.getOrder() == currentNode.getOrder()) {
					for (VarValue value : currentNode.getPassParameters()) {
						invocationParent.addWrittenVariable(value);							
					}
				}
			}

			if (currentNode.getInvocationParent() != null && !currentNode.getReturnedVariables().isEmpty()) {
				TraceNode invocationParent = currentNode.getInvocationParent();
				TraceNode returnStep = invocationParent.getStepOverNext();

				if (returnStep == null) {
					returnStep = currentNode.getStepInNext();
				}

				if (returnStep != null) {
					for (VarValue value : currentNode.getReturnedVariables()) {
						currentNode.addWrittenVariable(value);
						returnStep.addReadVariable(value);
					}
				}
			}
			
			if(currentNode.getInvokingMethod() != null && currentNode.getStepOverNext() != null) {
				if(currentNode.getStepOverNext().getOrder() != currentNode.getStepInNext().getOrder()) {
					currentNode.getStepOverNext().getReadVariables().addAll(currentNode.getReadVariables());												
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
	public TraceTransformer getTransformer0() {
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
	public boolean isInstrumentationActive0() {
		return !ExecutionTracer.isShutdown();
	}
	
	/**
	 * Insert condition result into branch node.
	 * Condition is naively defined as follow: <br><br>
	 * 
	 * The condition result is true when the stepOverNext node is the next line of code.
	 * Otherwise, the condition result is false.
	 * 
	 * @param trace Target trace
	 */
	private void addConditionResult(final Trace trace) {
		for (TraceNode node : trace.getExecutionList()) {
			if (node.isBranch()) {
				TraceNode stepOverNext = node.getStepOverNext();
				boolean conditionResult = stepOverNext == null ? false : node.getLineNumber()+1 == stepOverNext.getLineNumber();
				node.insertConditionResult(conditionResult);
			}
		}
	}
	
	private void changeRedefinedVarID(Trace trace) {
		Map<String, String> mapping = new HashMap<>();
		for (TraceNode node : trace.getExecutionList()) {
			for (VarValue readVar : node.getReadVariables()) {
				String varID = readVar.getVarID();
				if (!mapping.containsKey(varID)) {
					mapping.put(varID, varID);
				} else {
					String newID = mapping.get(varID);
					readVar.setVarID(newID);
				}
			}
			
			for (VarValue writeVar : node.getWrittenVariables()) {
				String varID = writeVar.getVarID();
				if (mapping.containsKey(varID)) {
					String newID = writeVar.getVarID() + "-" + node.getOrder();
					mapping.put(varID, newID);
					writeVar.setVarID(newID);
				}
			}
		}
	}
	
	private void matchArrayElementName(final Trace trace) {
		/*
		 * Element variables' name is the address of the array it belongs to,
		 * which is not readable to human, this function will replace the
		 * address by the name of array variable.
		 */

		// First, store the name of all variables that have children
		Map<String, String> parentNameMap = new HashMap<>();
		for (TraceNode node : trace.getExecutionList()) {
			List<VarValue> variables = new ArrayList<>();
			variables.addAll(node.getReadVariables());
			variables.addAll(node.getWrittenVariables());
			for (VarValue var : variables) {
				if (!var.getChildren().isEmpty()) {
					parentNameMap.put(var.getAliasVarID(),  var.getVarName());
				}
			}
		}
		
		// Second, for every element in array, replace the name
		for (TraceNode node : trace.getExecutionList()) {
			List<VarValue> variables = new ArrayList<>();
			variables.addAll(node.getReadVariables());
			variables.addAll(node.getWrittenVariables());
			for (VarValue var : variables) {
				if (var.isElementOfArray()) {
					// If the variable is element of array, then it must have one parent
					final VarValue parent = var.getParents().get(0);
					final String aliasID = parent.getVarID();
					if (parentNameMap.containsKey(aliasID)) {
						final String parentName = parentNameMap.get(aliasID);
						final String indexStr = this.extractIndexFromName(var.getVarName());
						final String newVarName = parentName + "[" + indexStr + "]";
						var.getVariable().setName(newVarName);
					}
				}
			}
		}
		
		// Last, also change the element variable of the array variable children
		for (TraceNode node : trace.getExecutionList()) {
			List<VarValue> variables = new ArrayList<>();
			variables.addAll(node.getReadVariables());
			variables.addAll(node.getWrittenVariables());
			for (VarValue var : variables) {
				if (!var.getChildren().isEmpty()) {
					for (VarValue child : var.getChildren()) {
						if (child.isElementOfArray()) {
							final String indexStr = this.extractIndexFromName(child.getVarName());
							final String newVarName = var.getVarName() + "[" + indexStr + "]";
							child.getVariable().setName(newVarName);
						}
					}
				}
			}
		}
	}
	
	private String extractIndexFromName(final String name) {
        // Define the pattern for matching text within square brackets
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");

        // Create a matcher with the input string
        Matcher matcher = pattern.matcher(name);

        // Find and print all matches
        String indexStr = "";
        while (matcher.find()) {
            indexStr = matcher.group(1); // Group 1 contains the text within brackets
        }
        
        return indexStr;
	}
}
