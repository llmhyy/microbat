package microbat.codeanalysis.runtime;

import static sav.strategies.junit.SavJunitRunner.ENTER_TC_BKP;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.internal.VoidValueImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
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
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;

import microbat.codeanalysis.ast.LocalVariableScope;
import microbat.codeanalysis.ast.VariableScopeParser;
import microbat.codeanalysis.runtime.jpda.expr.ExpressionParser;
import microbat.codeanalysis.runtime.jpda.expr.ParseException;
import microbat.codeanalysis.runtime.variable.VariableValueExtractor;
import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.StringValue;
import microbat.model.value.VarValue;
import microbat.model.value.VirtualValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Param;
import microbat.model.variable.Variable;
import microbat.model.variable.VirtualVar;
import microbat.util.BreakpointUtils;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import microbat.util.PrimitiveUtils;
import microbat.util.Settings;
import sav.common.core.SavException;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.SignatureUtils;
import sav.strategies.dto.AppJavaClassPath;

/**
 * @author Yun Lin
 * 
 *         This class origins from three classes written by LLT, i.e.,
 *         BreakpointDebugger, JunitDebugger, and TestcaseExecutor.
 * 
 */
@SuppressWarnings("restriction")
public class ProgramExecutor0 extends Executor {
	public static final long DEFAULT_TIMEOUT = -1;

	/**
	 * fundamental fields for debugging
	 */
	/**
	 * the class patterns indicating the classes into which I will not step to
	 * get the runtime values
	 */
	private AppJavaClassPath appPath;
	
	/** maps from a given class name to its contained breakpoints */
	private Map<String, List<BreakPoint>> brkpsMap;

	/**
	 * for recording execution trace
	 */
	private Trace trace;

	public ProgramExecutor0() {}

	/**
	 * Executing the program, each time of the execution, we catch a JVM event
	 * (e.g., step event, class preparing event, method entry event, etc.).
	 * Generally, we collect the runtime program states in some interesting step
	 * event, and record these steps and their corresponding program states in a
	 * trace node.
	 * 
	 * <br>
	 * <br>
	 * Note that the trace node can form a tree-structure in terms of method
	 * invocation relations and loop relations.
	 * 
	 * <br>
	 * <br>
	 * See the field <code>trace</code> in this class.
	 * 
	 * <br>
	 * The parameter of executionOrderList is used to make sure the trace is recorded as executionOrderList shows.
	 * Different from when we record the executionOrderList, we will listen to method entry/exist event, which can
	 * introduces some steps which executionOrderList does not record. In such case, we just focus on the part of trace
	 * recorded by executionOrderList.
	 * 
	 * @param runningStatements
	 * @throws SavException
	 */
	public void run(List<BreakPoint> runningStatements, List<BreakPoint> executionOrderList, IProgressMonitor monitor, int stepNum, boolean isTestcaseEvaluation) throws SavException, TimeoutException {
		this.trace = new Trace(appPath);
		List<String> exlcudes = MicroBatUtil.extractExcludeFiles("", appPath.getExternalLibPaths());
		this.addLibExcludeList(exlcudes);
		this.brkpsMap = BreakpointUtils.initBrkpsMap(runningStatements);
		assignMethodSignature(runningStatements, executionOrderList);

		List<PointWrapper> wrapperList = convertToPointWrapperList(executionOrderList);
		
		VirtualMachine vm = null;
		try {
			vm = constructTrace(monitor, wrapperList, this.appPath, stepNum, isTestcaseEvaluation);				
		} finally {
			if(vm != null){
				vm.exit(0);
			}
			System.out.println();
			System.out.println("JVM is ended.");
		}

	}

	private void assignMethodSignature(List<BreakPoint> runningStatements, List<BreakPoint> executionOrderList) {
		HashMap<String, BreakPoint> map = new HashMap<>();
		for(BreakPoint point: runningStatements) {
			map.put(point.getDeclaringCompilationUnitName()+point.getLineNumber(), point);
		}
		
		for(BreakPoint execution: executionOrderList) {
			BreakPoint point = map.get(execution.getDeclaringCompilationUnitName()+execution.getLineNumber());
			execution.setMethodSign(point.getMethodSign());
		}
	}

	private List<PointWrapper> convertToPointWrapperList(List<BreakPoint> executionOrderList) {
		List<PointWrapper> list = new ArrayList<>();
		for(BreakPoint point: executionOrderList) {
			PointWrapper pWrapper = new PointWrapper(point);
			list.add(pWrapper);
		}
		return list;
	}

	class PointWrapper{
		BreakPoint point;
		boolean isHit = false;
		
		public PointWrapper(BreakPoint point) {
			this.point = point;
		}

		public BreakPoint getPoint() {
			return point;
		}

		public void setPoint(BreakPoint point) {
			this.point = point;
		}

		public boolean isHit() {
			return isHit;
		}

		public void setHit(boolean isHit) {
			this.isHit = isHit;
		}

		@Override
		public String toString() {
			return "PointWrapper [point=" + point + ", isHit=" + isHit + "]";
		}
		
		public int getLineNumber() {
			return this.point.getLineNumber();
		}
	}
	
	/**
	 * The parameter of executionOrderList is used to make sure the trace is recorded as executionOrderList shows.
	 * Different from when we record the executionOrderList, we will listen to method entry/exist event, which can
	 * introduces some steps which executionOrderList does not record. In such case, we just focus on the part of trace
	 * recorded by executionOrderList.
	 * 
	 * @param monitor
	 * @param executionOrderList
	 * @param appClassPath
	 * @param vm
	 * @param stepNum
	 * @param isTestcaseEvaluation
	 * @throws SavException
	 * @throws TimeoutException
	 */
	private VirtualMachine constructTrace(IProgressMonitor monitor, List<PointWrapper> executionOrderList, AppJavaClassPath appClassPath, 
			int stepNum, boolean isTestcaseEvaluation) throws SavException, TimeoutException {
		
		/** start debugger */
		VirtualMachine vm = new VMStarter(this.appPath).start();
		
		EventRequestManager erm = vm.eventRequestManager();

		/** add class watch, otherwise, I cannot catch the registered event */
		addClassWatch(erm);

		EventQueue eventQueue = vm.eventQueue();

		boolean stop = false;
		boolean eventTimeout = false;
		Map<String, BreakPoint> locBrpMap = new HashMap<String, BreakPoint>();

		/**
		 * This variable aims to record the last executed stepping point. If
		 * this variable is not null, then the next time we listen a step event,
		 * the values collected then are considered the aftermath of latest
		 * recorded trace node.
		 */
		BreakPoint lastSteppingInPoint = null;

		/**
		 * record the method entrance and exit so that I can build a
		 * tree-structure for trace node.
		 */
		Stack<TraceNode> methodNodeStack = new Stack<>();
		// Stack<Method> methodStack = new Stack<>();
		Stack<String> methodSignatureStack = new Stack<>();

		/**
		 * this variable is used to build step-over relation between trace
		 * nodes.
		 */
		TraceNode methodNodeJustPopedOut = null;
		
		/** this variable is used to handle exception case. */
		Location caughtLocationForJustException = null;

		cancel: 
		while (!stop && !eventTimeout) {
			EventSet eventSet;
			try {
				eventSet = eventQueue.remove(TIME_OUT);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			if (eventSet == null) {
				System.out.println("Time out! Cannot get event set!");
				System.out.println("method entry/exit enabled: " + this.methodEntryRequest.isEnabled() 
				+ ", " + this.methodExitRequest.isEnabled());
				eventTimeout = true;
				break;
			}

			for (Event event : eventSet) {
				if (event instanceof VMStartEvent) {
					System.out.println("JVM is started...");

					ThreadReference thread = ((VMStartEvent) event).thread();
					addStepWatch(erm, thread);
					addMethodWatch(erm);
					addExceptionWatch(erm);
					addThreadStartWatch(erm);
					
					if(isTestcaseEvaluation){
						disableAllStepRequests();
					}
					else {
						this.methodEntryRequest.disable();
						this.methodExitRequest.disable();
						excludeJUnitLibs();
					}
					
				}
				else if (event instanceof ThreadStartEvent) {
					ThreadReference threadReference = ((ThreadStartEvent) event).thread();
					if(!threadReference.name().equals("main") && !threadReference.name().equals("DestroyJavaVM")) {
						addStepWatch(erm, threadReference);
//						excludeJUnitLibs();		
						System.currentTimeMillis();
					}
				}
				if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
					stop = true;
//					break;
				} 
				else if (event instanceof ClassPrepareEvent) {
					parseBreakpoints(vm, (ClassPrepareEvent) event, locBrpMap);
				} 
				else if (event instanceof StepEvent) {
					ThreadReference thread = ((StepEvent) event).thread();
					Location currentLocation = ((StepEvent) event).location();
					
					TraceNode latestNode = this.trace.getLastestNode();
					if(latestNode!=null) {
						TraceNode popupNode = buildMethodStructure(latestNode, executionOrderList, methodNodeStack, methodSignatureStack);
						if(popupNode!=null) {
							methodNodeJustPopedOut = popupNode;
						}
					}
					
					boolean isContextChange = false;
					if (lastSteppingInPoint != null) {
						/**
						 * collect the variable values after executing previous step
						 */
						collectValueOfPreviousStep(lastSteppingInPoint, thread, currentLocation);
						
						/**
						 * Parsing the written variables of last step.
						 */
						isContextChange = checkContext(lastSteppingInPoint, currentLocation);
						if (!isContextChange) {
							parseReadWrittenVariableInThisStep(thread, currentLocation,
									this.trace.getLastestNode(), this.trace.getStepVariableTable(), Variable.WRITTEN);
						}

						lastSteppingInPoint = null;
					}

					BreakPoint bkp = locBrpMap.get(currentLocation.toString());
					BreakPoint supposedBkp = null; 
					if(trace.size()<executionOrderList.size()) {
						supposedBkp = executionOrderList.get(trace.size()).getPoint();
					}
					/**
					 * This step is an interesting step (sliced statement) in
					 * our debugging process
					 */
					if (bkp != null /*&& bkp.equals(supposedBkp)*/) {
						BreakPointValue bkpVal = retrieveValue(this.trace.getLastestNode(), isContextChange, bkp, thread, currentLocation);
						
						TraceNode node = recordTrace(bkp, bkpVal);
						
						/**
						 * pop up method after an exception is caught.
						 */
						if (caughtLocationForJustException != null) {
							methodNodeJustPopedOut = popUpMethodCausedByException(methodNodeStack, methodSignatureStack,
									methodNodeJustPopedOut, caughtLocationForJustException);
							caughtLocationForJustException = null;
						}

						/**
						 * Build parent-child relation between trace nodes.
						 */
						if (!methodNodeStack.isEmpty()) {
							TraceNode parentInvocationNode = methodNodeStack.peek();
							parentInvocationNode.addInvocationChild(node);
							node.setInvocationParent(parentInvocationNode);
						}

						/**
						 * set step over previous/next node when this step just
						 * come back from a method invocation ( i.e.,
						 * lastestPopedOutMethodNode != null).
						 */
						if (node != null && methodNodeJustPopedOut != null) {
							methodNodeJustPopedOut.setStepOverNext(node);
							methodNodeJustPopedOut.setAfterStepOverState(node.getProgramState());
							node.setStepOverPrevious(methodNodeJustPopedOut);

							methodNodeJustPopedOut = null;
						}

						parseReadWrittenVariableInThisStep(thread, currentLocation, node,
								this.trace.getStepVariableTable(), Variable.READ);

						/**
						 * create virtual variable for return statement
						 */
						if (this.trace.size() > 1) {
							TraceNode lastestNode = this.trace.getExectionList().get(this.trace.size() - 2);
							if (lastestNode.getBreakPoint().isReturnStatement()) {
								//TODO get the written virtual variable of previous node
								createVirutalVariableForReturnStatement(thread, node, lastestNode, null);
							}
						}

						lastSteppingInPoint = bkp;
						
						monitor.worked(1);
						printProgress(trace.size(), stepNum);
					} 

					if (monitor.isCanceled() || this.trace.getExectionList().size() >= Settings.stepLimit) {
						stop = true;
						break cancel;
					}
				} 
				else if (event instanceof MethodEntryEvent) {
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
					ExceptionEvent ee = (ExceptionEvent) event;
					Location catchLocation = ee.catchLocation();
					TraceNode lastNode = this.trace.getLastestNode();
					if(lastNode != null){
						lastNode.setException(true);
						
						if (catchLocation == null) {
							stop = true;
						} else {
							caughtLocationForJustException = ee.catchLocation();
						}						
					}
				}
				
			}

			eventSet.resume();
		}
		
		return vm;
	}

	private TraceNode popUpMethodCausedByException(Stack<TraceNode> methodNodeStack, Stack<String> methodSignatureStack,
			TraceNode methodNodeJustPopedOut, Location caughtLocationForJustException) {
		if (!methodNodeStack.isEmpty()) {
			TraceNode invocationNode = this.trace.findLastestExceptionNode();
			boolean isInvocationEnvironmentContainingLocation = isInvocationEnvironmentContainingLocation(
					invocationNode, caughtLocationForJustException);
			while (!isInvocationEnvironmentContainingLocation) {
				if (!methodNodeStack.isEmpty()) {
					invocationNode = methodNodeStack.pop();
					methodNodeJustPopedOut = invocationNode;
					methodSignatureStack.pop();

					isInvocationEnvironmentContainingLocation = isInvocationEnvironmentContainingLocation(
							invocationNode, caughtLocationForJustException);
				} else {
					break;
				}
			}
		}
		return methodNodeJustPopedOut;
	}

	private BreakPointValue retrieveValue(TraceNode lastestNode, boolean isContextChange, BreakPoint bkp,
			ThreadReference thread, Location currentLocation) throws SavException {
		BreakPointValue bkpVal = null;
		if(this.trace.getLastestNode() != null && !isContextChange){
			bkpVal = this.trace.getLastestNode().getAfterStepInState();
		}
		else{
			bkpVal = extractValuesAtLocation(bkp, thread, currentLocation);
		}
		
		return bkpVal;
	}

	private TraceNode buildMethodStructure(TraceNode latestNode, List<PointWrapper> executionOrderList, Stack<TraceNode> methodNodeStack,
			Stack<String> methodSignatureStack) {
		if (latestNode.getOrder()==56) {
			System.currentTimeMillis();
		}
		
		PointWrapper nextPoint = getNextPoint(executionOrderList);
		if(nextPoint==null) {
			return null;
		}
		
		if (isInSameMethod(latestNode.getBreakPoint(), nextPoint.getPoint())) {
			return null;
		}
		
		if(isSurroundingMethodDifferentFromTheNext(latestNode, executionOrderList)) {
			if(isValidExitingMethod(latestNode, executionOrderList)) {
				if (!methodNodeStack.isEmpty()) {
					int index = -1;
					for(int i=methodNodeStack.size()-1; i>=0; i--) {
						TraceNode node = methodNodeStack.get(i);
						if(isInSameMethod(node.getBreakPoint(), nextPoint.getPoint())) {
							index = i;
							break;
						}
					}
					
					if(index != -1) {
						TraceNode node = null;
						int popNum = methodSignatureStack.size() - index;
						for(int i=0; i<popNum; i++) {
							node = methodNodeStack.pop();
							methodSignatureStack.pop();
//							methodNodeJustPopedOut = node;
//							lastestReturnedValue = mee.returnValue();
						}
						return node;
					}
					
					if(latestNode.isReturnNode()) {
						//TODO keep the return value
					}
					
					return null;
				}
			}
			/**
			 * entering a method
			 */
			else {
				methodNodeStack.push(latestNode);
				methodSignatureStack.push(latestNode.getMethodSign());
				//TODO
//				parseWrittenParameterVariableForMethodInvocation(frame, declaringCompilationUnit,
//						methodLocationLine, paramList, lastestNode);
			}
		}
		
		return null;
	}
	
	private boolean isValidExitingMethod(TraceNode latestNode, List<PointWrapper> executionOrderList) {
		if(latestNode.getOrder()==101) {
			System.currentTimeMillis();
		}
		
		if(isEndOfMethod(latestNode)) {
			return true;
		}
		
		PointWrapper nextPoint = getNextPoint(executionOrderList);
		if(latestNode.isReturnNode()) {
			if(nextPoint!=null) {
				if(isNextPointInvokeMethodOfLatestNode(nextPoint, latestNode)) {
					return true;
				}
				
				if(!isNextPointTheFirstStatementOfAMethod(nextPoint)) {
					return true;
				}
			}
			else {
				return true;				
			}
		}
		
		return false;
	}

	private boolean isNextPointTheFirstStatementOfAMethod(PointWrapper nextPoint) {
		CompilationUnit cUnit = JavaUtil.findCompilationUnitInProject(nextPoint.getPoint().getDeclaringCompilationUnitName(), appPath);
		MethodFinder finder = new MethodFinder(cUnit, nextPoint.getPoint().getLineNumber());
		cUnit.accept(finder);
		
		MethodDeclaration methodDeclaration = finder.candidate;
		if (methodDeclaration!=null) {
			@SuppressWarnings("rawtypes")
			List list = methodDeclaration.getBody().statements();
			if(list != null && !list.isEmpty()) {
				ASTNode node = (ASTNode) list.get(0);
				int startLine = cUnit.getLineNumber(methodDeclaration.getStartPosition());
				int endLine = cUnit.getLineNumber(node.getStartPosition()+node.getLength());
				return startLine<=nextPoint.getPoint().getLineNumber() && nextPoint.getPoint().getLineNumber()<=endLine;
			}
			
		}
		
		return true;
	}

	
	class MethodInvocationFinder extends ASTVisitor{
		CompilationUnit cUnit;
		MethodDeclaration assumedInvokedMethod;
		int invocationLine;
		
		boolean isInvoked = false;
		
		public MethodInvocationFinder(MethodDeclaration assumedInvokedMethod, 
				CompilationUnit cUnit, int invocationLine) {
			super();
			this.assumedInvokedMethod = assumedInvokedMethod;
			this.cUnit = cUnit;
			this.invocationLine = invocationLine;
		}
		
		@Override
		public boolean visit(MethodDeclaration methodDeclaration) {
			int start = cUnit.getLineNumber(methodDeclaration.getStartPosition());
			int end = cUnit.getLineNumber(methodDeclaration.getStartPosition()+methodDeclaration.getLength());
			if(start<=invocationLine&&invocationLine<=end) {
				return true;
			}
			return false;
		}

		@Override
		public boolean visit(MethodInvocation invocation) {
			int start = cUnit.getLineNumber(invocation.getStartPosition());
			int end = cUnit.getLineNumber(invocation.getStartPosition()+invocation.getLength());
			if(start<=invocationLine&&invocationLine<=end) {
				String methodName = assumedInvokedMethod.getName().getIdentifier();
				int paramNum = assumedInvokedMethod.parameters().size();
				if (invocation.getName().toString().equals(methodName) &&
						paramNum==invocation.arguments().size()) {
					isInvoked = true;
					return false;
				}
			}
			
			return false;
		}
	}
	
	private boolean isNextPointInvokeMethodOfLatestNode(PointWrapper nextPoint, TraceNode latestNode) {
		if (latestNode.getOrder()==30) {
			System.currentTimeMillis();
		}
		
		CompilationUnit assumedInvokingUnit = JavaUtil.findCompilationUnitInProject(nextPoint.getPoint().getDeclaringCompilationUnitName(), appPath);
		if (nextPoint.getPoint()!=null) {
			CompilationUnit invokedUnit = JavaUtil.findCompilationUnitInProject(latestNode.getDeclaringCompilationUnitName(), appPath);
			MethodFinder finder = new MethodFinder(invokedUnit, latestNode.getLineNumber());
			invokedUnit.accept(finder);
			MethodDeclaration assumedInvokedMethod = finder.candidate;
			
			if (assumedInvokedMethod!=null) {
				MethodInvocationFinder invocationFinder = 
						new MethodInvocationFinder(assumedInvokedMethod, assumedInvokingUnit, nextPoint.getLineNumber());
				assumedInvokingUnit.accept(invocationFinder);
				return invocationFinder.isInvoked;					
			}
		}
		
		return false;
	}

	class MethodEndChecker extends ASTVisitor{
		CompilationUnit cu;
		TraceNode latestNode;
		
		boolean isMethodEnd = false;
		
		public MethodEndChecker(CompilationUnit cu, TraceNode latestNode) {
			super();
			this.cu = cu;
			this.latestNode = latestNode;
		}

		public boolean visit(MethodDeclaration mDeclaration) {
			int methodStart = cu.getLineNumber(mDeclaration.getStartPosition());
			int methodEnd = cu.getLineNumber(mDeclaration.getStartPosition()+mDeclaration.getLength());
			if(methodStart<=latestNode.getLineNumber() && latestNode.getLineNumber()<=methodEnd) {
				if (methodEnd==latestNode.getLineNumber()) {
					isMethodEnd = true;
					return false;
				}
				
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	private boolean isEndOfMethod(TraceNode latestNode) {
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(latestNode.getDeclaringCompilationUnitName(), appPath);
		MethodEndChecker checker = new MethodEndChecker(cu, latestNode);
		cu.accept(checker);
		
		return checker.isMethodEnd;
	}

	private boolean isSurroundingMethodDifferentFromTheNext(TraceNode latestNode, List<PointWrapper> executionOrderList) {
		BreakPoint nextPoint = getNextPoint(executionOrderList).getPoint();
		if(nextPoint==null) {
			return true;
		}
		
		String surroundingMethod = latestNode.getMethodSign();
		String nextSurrondingMethod = nextPoint.getMethodSign();
		
		if(surroundingMethod==null && nextSurrondingMethod==null) {
			if (nextPoint.getDeclaringCompilationUnitName().equals(latestNode.getDeclaringCompilationUnitName())) {
				return false;
			}
			else {
				return true;
			}
		}
		else if(surroundingMethod!=null && nextSurrondingMethod!=null) {
			return !surroundingMethod.equals(nextSurrondingMethod);
		}
		
		return true;
	}

	private boolean isInSameMethod(BreakPoint point0, BreakPoint point1) {
		if(point0==null || point1==null) {
			return false;
		}
		
		
		String latestMethod = point0.getMethodSign();
		String pointMethod = point1.getMethodSign();
		
		if(latestMethod==null && pointMethod==null) {
			return point0.getClassCanonicalName().equals(point1.getClassCanonicalName());
		}
		else if(latestMethod!=null && pointMethod!=null) {
			return point0.getMethodSign().equals(point1.getMethodSign());			
		}
		
		return false;
	}

	private PointWrapper findCorrespondingPointWrapper(TraceNode lastestNode, List<PointWrapper> executionOrderList) {
		if (lastestNode==null) {
			return null;
		}
		
		return executionOrderList.get(lastestNode.getOrder()-1);
	}

	private PointWrapper getNextPoint(List<PointWrapper> executionOrderList) {
		int index = trace.getExectionList().size();
		if(index >= executionOrderList.size()) {
			return null;
		}
		
		return executionOrderList.get(index);
	}

	private boolean isInterestedMethod(Location location, PointWrapper lastSteppingInPoint) {
		
		if(lastSteppingInPoint!=null) {
			if(location.declaringType().toString().equals(lastSteppingInPoint.getPoint().getClassCanonicalName()) 
					&& location.lineNumber()==lastSteppingInPoint.getPoint().getLineNumber()) {
				return true;
			}			
		}
		
		return false;
	}

//	private boolean isInterestedMethod(Method method, Map<String, List<BreakPoint>> brkpsMap) {
//		String className = method.declaringType().name();
//		List<BreakPoint> recordedLines = brkpsMap.get(className);
//		
//		if(recordedLines!=null && !recordedLines.isEmpty()){
//			try {
//				List<Location> methodLocations = method.allLineLocations();
//				
//				for(Location location: methodLocations){
//					int locationLine = location.lineNumber();
//					
//					for(BreakPoint point: recordedLines){
//						if(point.getLineNumber()==locationLine){
//							return true;
//						}
//					}
//				}
//			} catch (AbsentInformationException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		return false;
//	}

	private void printProgress(int size, int stepNum) {
		double progress = ((double)size)/stepNum;
		
		double preProgr = 0;
		if(size == 1){
			System.out.print("progress: ");
		}
		else{
			preProgr = ((double)(size-1))/stepNum;
		}
		
		int prog = (int)(progress*100);
		int preP = (int)(preProgr*100);
		
		int diff = prog - preP;
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i<diff; i++){
			buffer.append("=");
		}
		System.out.print(buffer.toString());
		
		int[] percentiles = {10, 20, 30, 40, 50, 60, 70, 80, 90};
		for(int i=0; i<percentiles.length; i++){
			int percentile = percentiles[i];
			if(preP<percentile && percentile<=prog){
				System.out.print(prog+"%");
			}
		}
	}

	public String trimGenericType(String complexInvokedMethodSig) {
		String simpleSig = complexInvokedMethodSig.replaceAll("<[^<|^>]*>", "");
		return simpleSig;
	}

	public List<Param> findParamList(MethodDeclaration invokedMethod) {
		List<Param> paramList = new ArrayList<>();
		for (Object obj : invokedMethod.parameters()) {
			if (obj instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
				String paramName = svd.getName().getIdentifier();
				String paramType = svd.getType().toString();

				Param param = new Param(paramType, paramName);
				paramList.add(param);
			}
		}

		return paramList;
	}

	private List<Param> parseParamList(Method method) {
		List<Param> paramList = new ArrayList<>();

		try {
			for (LocalVariable variable : method.arguments()) {
				Param param = new Param(variable.typeName(), variable.name());
				paramList.add(param);
			}
		} catch (AbsentInformationException e) {
			e.printStackTrace();
		}

		return paramList;
	}

	private String createSignature(Method method) {
		String className = method.declaringType().name();
		String methodName = method.name();
		String methodSig = method.signature();

		String sig = JavaUtil.createSignature(className, methodName, methodSig);

		return sig;
	}

	private boolean isLocationInRunningStatement(Location location, Map<String, BreakPoint> locationMap) {
		String key = location.toString();
		return locationMap.containsKey(key);
	}

	class MethodFinder extends ASTVisitor {
		CompilationUnit cu;
		int lineNumber;

		MethodDeclaration candidate;

		public MethodFinder(CompilationUnit cu, int lineNumber) {
			this.cu = cu;
			this.lineNumber = lineNumber;
		}

		public boolean visit(MethodDeclaration md) {
			int startLine = cu.getLineNumber(md.getStartPosition());
			int endLine = cu.getLineNumber(md.getStartPosition() + md.getLength());

			if (startLine <= lineNumber && endLine >= lineNumber) {
				if (candidate == null) {
					candidate = md;
				} else {
					int candStartLine = cu.getLineNumber(candidate.getStartPosition());
					int candEndLine = cu.getLineNumber(candidate.getStartPosition() + candidate.getLength());

					if (startLine >= candStartLine && endLine <= candEndLine) {
						candidate = md;
					}
				}
				return true;
			}

			return false;
		}
	}

	private boolean isInvocationEnvironmentContainingLocation(TraceNode methodNode,
			Location caughtLocationForJustException) {
		String qualifiedName = methodNode.getBreakPoint().getDeclaringCompilationUnitName();
		int lineNumber = methodNode.getLineNumber();

		try {
			String path = caughtLocationForJustException.sourcePath();
			path = path.substring(0, path.indexOf(".java"));
			path = path.replace(File.separatorChar, '.');

			if (qualifiedName.equals(path)) {
				CompilationUnit cu = JavaUtil.findCompilationUnitInProject(qualifiedName, appPath);
				if (cu != null) {
					MethodFinder mFinder = new MethodFinder(cu, lineNumber);
					cu.accept(mFinder);
					MethodDeclaration md = mFinder.candidate;

					if (md != null) {
						int mdStartLine = cu.getLineNumber(md.getStartPosition());
						int mdEndLine = cu.getLineNumber(md.getStartPosition() + md.getLength());

						int caughtLine = caughtLocationForJustException.lineNumber();
						if (caughtLine >= mdStartLine && caughtLine <= mdEndLine) {
							return true;
						}
					}
				}

			}
		} catch (AbsentInformationException e) {
			e.printStackTrace();
		}

		return false;
	}

	private void addExceptionWatch(EventRequestManager erm) {

		setExceptionRequest(erm.createExceptionRequest(null, true, true));
		getExceptionRequest().setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		for (String ex : libExcludes) {
			getExceptionRequest().addClassExclusionFilter(ex);
		}
		// request.addClassFilter("java.io.FileNotFoundException");
		getExceptionRequest().enable();
	}

//	private void addStepWatch(EventRequestManager erm, Event event) {
//		stepRequest = erm.createStepRequest(((VMStartEvent) event).thread(), StepRequest.STEP_LINE,
//				StepRequest.STEP_INTO);
//		stepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
//		for (String ex : libExcludes) {
//			stepRequest.addClassExclusionFilter(ex);
//		}
//		stepRequest.enable();
//	}

	/**
	 * when the last interesting stepping statement is a return statement,
	 * create a virtual variable.
	 */
	private void createVirutalVariableForReturnStatement(ThreadReference thread, TraceNode node, TraceNode returnNode,
			Value returnedValue) {

		if (returnedValue instanceof VoidValueImpl) {
			return;
		}

		String returnedType;
		String returnedStringValue;
		if (returnedValue == null) {
			returnedType = VirtualVar.VIRTUAL_TYPE;
			returnedStringValue = "null";
		} else {
			String type = returnedValue.type().toString();

			if (type.contains(".")) {
				type = type.substring(type.lastIndexOf(".") + 1, type.length());
			}

			returnedType = type;
			returnedStringValue = returnedValue.toString();
			if (returnedValue instanceof StringReference) {
				returnedStringValue = returnedStringValue.substring(1, returnedStringValue.length() - 1);
			} else if (returnedValue instanceof ArrayReference) {
				returnedStringValue = JavaUtil.retrieveStringValueOfArray((ArrayReference) returnedValue);
			} else if (returnedValue instanceof ObjectReference) {
//				returnedStringValue = JavaUtil.retrieveToStringValue(thread, (ObjectReference) returnedValue, this);
				returnedStringValue = JavaUtil.retrieveToStringValue((ObjectReference)returnedValue, 
						Settings.referenceFieldLayerInString, thread);
			}

		}

		String virID = VirtualVar.VIRTUAL_PREFIX + returnNode.getOrder();
		
		String virName = VirtualVar.VIRTUAL_PREFIX + getDeclaringMethod(returnNode);
		
		VirtualVar var = new VirtualVar(virName, returnedType);
		var.setVarID(virID);

		Map<String, StepVariableRelationEntry> map = this.trace.getStepVariableTable();
		StepVariableRelationEntry entry = new StepVariableRelationEntry(var.getVarID());
		entry.addAliasVariable(var);
		entry.addProducer(returnNode);
		entry.addConsumer(node);

		VarValue varValue = new VirtualValue(false, var);
		// String stringValue = "(return from " +
		// lastestNode.getBreakPoint().getMethodName() + "(...))";
		varValue.setStringValue(returnedStringValue);

		returnNode.addWrittenVariable(varValue);
		node.addReadVariable(varValue);

		map.put(var.getVarID(), entry);
	}

	class MethodNameRetriever extends ASTVisitor{
		MethodDeclaration innerMostMethod = null;
		
		CompilationUnit cu;
		int lineNumber;
		
		public MethodNameRetriever(CompilationUnit cu, int lineNumber){
			this.cu = cu;
			this.lineNumber = lineNumber;
		}
		
		public boolean visit(MethodDeclaration md){
			int start = cu.getLineNumber(md.getStartPosition());
			int end = cu.getLineNumber(md.getStartPosition()+md.getLength());
			
			if(start <= lineNumber && lineNumber <= end){
				if(innerMostMethod == null){
					innerMostMethod = md;
				}
				else{
					if(isMoreInner(md, innerMostMethod)){
						innerMostMethod = md;
					}
				}
				return true;
			}
			else{
				return false;
			}
		}

		private boolean isMoreInner(MethodDeclaration oldMD, MethodDeclaration newMD) {
			int oldStart = cu.getLineNumber(oldMD.getStartPosition());
			int oldEnd = cu.getLineNumber(oldMD.getStartPosition()+oldMD.getLength());
			
			int newStart = cu.getLineNumber(newMD.getStartPosition());
			int newEnd = cu.getLineNumber(newMD.getStartPosition()+newMD.getLength());
			
			if(oldStart<=newStart && newEnd<=oldEnd){
				return true;
			}
			else{
				return false;					
			}
		}
	}
	
	private String getDeclaringMethod(TraceNode returnNode) {
		int lineNumber = returnNode.getLineNumber();
		String compilationUnitName = returnNode.getDeclaringCompilationUnitName();
		final CompilationUnit cu = JavaUtil.findCompilationUnitInProject(compilationUnitName, appPath);
		MethodNameRetriever retriever = new MethodNameRetriever(cu, lineNumber);
		cu.accept(retriever);
		
		MethodDeclaration md = retriever.innerMostMethod;
		
		if(md != null){
			String methodName = md.getName().getIdentifier();
			return methodName;
		}
		else{
			return compilationUnitName.substring(compilationUnitName.lastIndexOf(".")+1, compilationUnitName.length());
		}
	}

	/**
	 * build the written relations between method invocation
	 */
	private void parseWrittenParameterVariableForMethodInvocation(StackFrame frame, String methodDeclaringCompilationUnit,
			int methodLocationLine, List<Param> paramList, TraceNode lastestNode) {
		
		for (Param param : paramList) {

			if (frame == null) {
				return;
			}

			Value value = JavaUtil.retriveExpression(frame, param.getName());
			LocalVar localVar = new LocalVar(param.getName(), param.getType(),
					lastestNode.getDeclaringCompilationUnitName(), lastestNode.getLineNumber());
			
			if (!(value instanceof ObjectReference) || value == null) {
				VariableScopeParser parser = new VariableScopeParser();
				LocalVariableScope scope = parser.parseMethodScope(methodDeclaringCompilationUnit, methodLocationLine,
						localVar.getName(), appPath);
				String varID;
				if (scope != null) {
					varID = Variable.concanateLocalVarID(methodDeclaringCompilationUnit, localVar.getName(), scope.getStartLine(),
							scope.getEndLine());
					String definingNodeOrder = this.trace.findDefiningNodeOrder(Variable.WRITTEN, lastestNode, varID);
					varID = varID + ":" + definingNodeOrder;
					localVar.setVarID(varID);
				} else {
					System.err.println("cannot find the method when parsing parameter scope of " + localVar +
							methodDeclaringCompilationUnit + "(line " + methodLocationLine +") ");
					System.currentTimeMillis();
				}
			}
			else{
				ObjectReference ref = (ObjectReference)value;
				String varID = String.valueOf(ref.uniqueID());
				String definingNodeOrder = this.trace.findDefiningNodeOrder(Variable.WRITTEN, lastestNode, varID);
				varID = varID + ":" + definingNodeOrder;
				localVar.setVarID(varID);
			}

			StepVariableRelationEntry entry = this.trace.getStepVariableTable().get(localVar.getVarID());
			if (entry == null && localVar.getVarID() != null) {
				entry = new StepVariableRelationEntry(localVar.getVarID());
				this.trace.getStepVariableTable().put(localVar.getVarID(), entry);
			}
			
			if(entry != null) {
				entry.addAliasVariable(localVar);
				entry.addProducer(lastestNode);
				
				VarValue varValue = null;
				if (PrimitiveUtils.isPrimitiveType(param.getType())) {
					if(value != null){
						varValue = new PrimitiveValue(value.toString(), false, localVar);
					}
					
				} else {
					varValue = new ReferenceValue(true, false, localVar);
				}
				
				if(varValue != null && varValue.getVarID()!=null){
					lastestNode.addWrittenVariable(varValue);					
				}
			}
		}
	}

	private boolean checkContext(BreakPoint lastSteppingPoint, Location loc) {
		String methodSign1 = lastSteppingPoint.getMethodSign();
		if(methodSign1 == null){
			return true;
		}
		methodSign1 = methodSign1.substring(methodSign1.lastIndexOf(".") + 1, methodSign1.length());			

		String methodSign2 = loc.method().signature();
		methodSign2 = loc.method().name() + methodSign2;
		

		String class1 = loc.declaringType().signature();
		class1 = SignatureUtils.signatureToName(class1);
		String class2 = lastSteppingPoint.getClassCanonicalName();

		if (methodSign1.equals(methodSign2) && class1.equals(class2)) {
			return false;
		} else {
			return true;
		}
	}

//	private MethodEntryRequest methodEntryRequest;
//	private MethodExitRequest methodExitRequset;

	/**
	 * add method enter and exit event
	 */
	private void addMethodWatch(EventRequestManager erm) {
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

	/** add watch requests **/
	private final void addClassWatch(EventRequestManager erm) {
		/* class watch request for breakpoint */
		for (String className : brkpsMap.keySet()) {
			addClassWatch(erm, className);
		}
		/* class watch request for junitRunner start point */
		addClassWatch(erm, ENTER_TC_BKP.getClassCanonicalName());
	}

//	private ClassPrepareRequest classPrepareRequest;
	private final void addClassWatch(EventRequestManager erm, String className) {
		setClassPrepareRequest(erm.createClassPrepareRequest());
		getClassPrepareRequest().addClassFilter(className);
		getClassPrepareRequest().setEnabled(true);
	}

	private void parseBreakpoints(VirtualMachine vm, ClassPrepareEvent classPrepEvent, Map<String, BreakPoint> locBrpMap) {
		ReferenceType refType = classPrepEvent.referenceType();
		List<BreakPoint> brkpList = CollectionUtils.initIfEmpty(brkpsMap.get(refType.name()));
		for (BreakPoint brkp : brkpList) {
			Location location = checkBreakpoint(vm, refType, brkp.getLineNumber());
			if (location != null) {
				locBrpMap.put(location.toString(), brkp);
			} else {
				System.err.println("Cannot add break point " + brkp);
			}
		}
	}

	private final Location checkBreakpoint(VirtualMachine vm, ReferenceType refType, int lineNumber) {
		List<Location> locations;
		try {
			locations = refType.locationsOfLine(lineNumber);
		} catch (AbsentInformationException e) {
			e.printStackTrace();
			return null;
		}
		if (!locations.isEmpty()) {
			Location location = locations.get(0);
			return location;
		}
		return null;
	}

	private VarValue generateVarValue(StackFrame frame, Variable var0, TraceNode node, String accessType) {
		VarValue varValue = null;
		/**
		 * Note that the read/written variables in breakpoint should be
		 * different when set them into different trace node.
		 */
		Variable var = var0.clone();
		String varName = var.getName();

		try {
			ExpressionValue expValue = retriveExpression(frame, varName, node.getBreakPoint());

			// Field f = frame.thisObject().referenceType().allFields().get(0);
			// frame.thisObject().getValue(f);
			if (expValue != null) {
				Value value = expValue.value;

				if (value == null) {
					System.currentTimeMillis();
				}

				if (value instanceof ObjectReference) {
					ObjectReference objRef = (ObjectReference) value;
					String varID = String.valueOf(objRef.uniqueID());

					String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType, node, varID);
					varID = varID + ":" + definingNodeOrder;
					var.setVarID(varID);

					if (value.type().toString().equals("java.lang.String")) {
						String strValue = value.toString();
						strValue = strValue.substring(1, strValue.length() - 1);
						varValue = new StringValue(strValue, false, var);
					} else {
						String strValue = "$Unknown$";
						if (objRef instanceof ArrayReference) {
							ArrayReference arrayValue = (ArrayReference) objRef;
							strValue = JavaUtil.retrieveStringValueOfArray(arrayValue);
						} else {
//							strValue = JavaUtil.retrieveToStringValue(frame.thread(), objRef, this);
							strValue = JavaUtil.retrieveToStringValue(objRef, 
									Settings.referenceFieldLayerInString, frame.thread());
						}

						varValue = new ReferenceValue(false, false, var);
						((ReferenceValue) varValue).setStringValue(strValue);
						varValue.setChildren(null);
					}
				} else {
					if (var instanceof LocalVar) {
						LocalVariableScope scope = Settings.localVariableScopes.findScope(var.getName(), node
								.getBreakPoint().getLineNumber(), node.getBreakPoint().getDeclaringCompilationUnitName());
						String varID;
						if (scope != null) {
							varID = Variable.concanateLocalVarID(node.getBreakPoint().getDeclaringCompilationUnitName(),
									var.getName(), scope.getStartLine(), scope.getEndLine());
							String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType, node, varID);
							varID = varID + ":" + definingNodeOrder;
						}
						/**
						 * it means that an implicit "this" variable is visited.
						 * 
						 */
						else if (var.getName().equals("this")) {
							varID = String.valueOf(frame.thisObject().uniqueID());
						} else {
							System.err.println("the local variable " + var.getName()
									+ " cannot find its scope to generate its id");
							return null;
						}
						var.setVarID(varID);
					} else {
						Value parentValue = expValue.parentValue;
						ObjectReference objRef = (ObjectReference) parentValue;
						
						if(objRef==null){
							objRef = frame.thisObject();
						}

						if (var instanceof FieldVar) {
							
							String varID = null;
							if(((FieldVar)var).isStatic()){
								varID = var.getName();
							}
							else{
								varID = Variable.concanateFieldVarID(String.valueOf(objRef.uniqueID()),
										var.getSimpleName());								
							}
							String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType, node, varID);
							varID = varID + ":" + definingNodeOrder;
							var.setVarID(varID);
						} 
						else if (var instanceof ArrayElementVar) {
							String index = var.getSimpleName();
							ExpressionValue indexValue = retriveExpression(frame, index, node.getBreakPoint());
							String indexValueString = indexValue.value.toString();
							String varID = Variable.concanateArrayElementVarID(String.valueOf(objRef.uniqueID()),
									indexValueString);
							
							System.currentTimeMillis();
							String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType, node, varID);
							varID = varID + ":" + definingNodeOrder;
							// String varID = String.valueOf(objRef.uniqueID())
							// + "[" + indexValueString + "]";

							var.setVarID(varID);
						}
					}

					String content = (value == null) ? null : value.toString();
					varValue = new PrimitiveValue(content, false, var);
					// ((PrimitiveValue)varValue).setStrVal(expValue.messageValue.toString());
				}

				return varValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private StackFrame findFrame(ThreadReference thread, Location location) {
		StackFrame frame = null;
		try {
			for (StackFrame f : thread.frames()) {
				if (f.location().equals(location)) {
					frame = f;
					break;
				}
			}
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		}

		return frame;
	}

	private void parseReadWrittenVariableInThisStep(ThreadReference thread, Location location, TraceNode node,
			Map<String, StepVariableRelationEntry> stepVariableTable, String action) {

//		try {
//			thread.frames();
//		} catch (IncompatibleThreadStateException e) {
//			e.printStackTrace();
//		}

		StackFrame frame = findFrame(thread, location);
		if (frame == null) {
			System.err.println("get a null frame from thread!");
			return;
		}

		synchronized (frame) {
			if (action.equals(Variable.READ)) {
				processReadVariable(node, stepVariableTable, frame);
			} else if (action.equals(Variable.WRITTEN)) {
				processWrittenVariable(node, stepVariableTable, frame);
			}
		}
	}

	private void processReadVariable(TraceNode node, Map<String, StepVariableRelationEntry> stepVariableTable,
			StackFrame frame) {
		
		List<Variable> readVariables = node.getBreakPoint().getReadVariables();
		for (Variable readVar : readVariables) {
			if (node.getOrder() == 18) {
				System.currentTimeMillis();
			}

			VarValue varValue = generateVarValue(frame, readVar, node, Variable.READ);

			System.currentTimeMillis();
			if (varValue != null) {
				node.addReadVariable(varValue);
				String varID = varValue.getVarID();

				StepVariableRelationEntry entry = stepVariableTable.get(varID);
				if (entry == null) {
					entry = new StepVariableRelationEntry(varID);
					stepVariableTable.put(varID, entry);
				}
				entry.addAliasVariable(readVar);

				// if(varID.contains("213") && node.getOrder() == 221){
				// System.currentTimeMillis();
				// }

				entry.addConsumer(node);
			}
		}
	}

	private void processWrittenVariable(TraceNode node, Map<String, StepVariableRelationEntry> stepVariableTable,
			StackFrame frame) {
		if (node.getOrder() == 285) {
			System.currentTimeMillis();
		}
		
		List<Variable> writtenVariables = node.getBreakPoint().getWrittenVariables();
		for (Variable writtenVar : writtenVariables) {
			VarValue varValue = generateVarValue(frame, writtenVar, node, Variable.WRITTEN);

			if (varValue != null) {
				node.addWrittenVariable(varValue);
				String varID = varValue.getVarID();

				StepVariableRelationEntry entry = stepVariableTable.get(varID);
				if (entry == null) {
					entry = new StepVariableRelationEntry(varID);
					stepVariableTable.put(varID, entry);
				}
				entry.addAliasVariable(writtenVar);

				entry.addProducer(node);
			}
		}
	}

	private ExpressionValue retriveExpression(final StackFrame frame0, String expression, BreakPoint point) {
		ThreadReference thread = frame0.thread();
		final StackFrame frame = findFrame(thread, frame0.location());

		ExpressionParser.GetFrame frameGetter = new ExpressionParser.GetFrame() {
			@Override
			public StackFrame get() throws IncompatibleThreadStateException {
				return frame;

			}
		};

		ExpressionValue eValue = null;

//		boolean classPrepare = getClassPrepareRequest().isEnabled();
//		boolean step = getStepRequest().isEnabled();
//		boolean methodEntry = getMethodEntryRequest().isEnabled();
//		boolean methodExit = getMethodExitRequset().isEnabled();
//		boolean exception = getExceptionRequest().isEnabled();
//		
//		getClassPrepareRequest().disable();
//		getStepRequest().disable();
//		getMethodEntryRequest().disable();
//		getMethodExitRequset().disable();
//		getExceptionRequest().disable();
		
		try {
			ExpressionParser.clear();

			CompilationUnit cu = JavaUtil.findCompilationUnitInProject(point.getDeclaringCompilationUnitName(), appPath);
			ExpressionParser.setParameters(cu, point.getLineNumber());
			
			if(expression.contains("(")) {
				
			}
			else {
				Value val = ExpressionParser.evaluate(expression, frame.virtualMachine(), frameGetter);
				
				eValue = new ExpressionValue(val, ExpressionParser.parentValue, null);
			}

			System.currentTimeMillis();

		} catch (ParseException e) {
//			e.printStackTrace();
		} catch (InvocationException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		} catch (ClassNotLoadedException e) {
			e.printStackTrace();
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		} catch(Exception e){
			//e.printStackTrace();
		}finally{
//			getClassPrepareRequest().setEnabled(classPrepare);
//			getStepRequest().setEnabled(step);
//			getMethodEntryRequest().setEnabled(methodEntry);
//			getMethodExitRequset().setEnabled(methodExit);
//			getExceptionRequest().setEnabled(exception);
		}

		return eValue;
	}

	private void collectValueOfPreviousStep(BreakPoint lastSteppingInPoint, ThreadReference thread, Location loc)
			throws SavException {

		BreakPoint current = new BreakPoint(lastSteppingInPoint.getClassCanonicalName(), lastSteppingInPoint.getDeclaringCompilationUnitName(),
				lastSteppingInPoint.getLineNumber());

		BreakPointValue bkpVal = extractValuesAtLocation(current, thread, loc);

		int len = trace.getExectionList().size();
		TraceNode node = trace.getExectionList().get(len - 1);
		node.setAfterStepInState(bkpVal);

	}

	private TraceNode recordTrace(BreakPoint bkp, BreakPointValue bkpVal) {
		int order = trace.size() + 1;
		TraceNode node = new TraceNode(bkp, bkpVal, order);

		TraceNode stepInPrevious = null;
		if (order >= 2) {
			stepInPrevious = trace.getExectionList().get(order - 2);
		}

		node.setStepInPrevious(stepInPrevious);
		if (stepInPrevious != null) {
			stepInPrevious.setStepInNext(node);
		}

		trace.addTraceNode(node);

		return node;
	}

	private BreakPointValue extractValuesAtLocation(BreakPoint bkp, ThreadReference thread, Location loc)
			throws SavException {
		if (Settings.isRecordSnapshot) {
			try {
				//TODO null=>this
				VariableValueExtractor extractor = new VariableValueExtractor(bkp, thread, loc, null);
				BreakPointValue bpValue = extractor.extractValue();
				return bpValue;

			} catch (IncompatibleThreadStateException e) {
				e.printStackTrace();
			} catch (AbsentInformationException e) {
				e.printStackTrace();
			}
			return null;

		} else {
			return new BreakPointValue("");
		}
	}

	public Trace getTrace() {
		int len = this.trace.size();
		if(len != 0){
			TraceNode lastNode = this.trace.getExectionList().get(len - 1);
			if (lastNode.getAfterState() == null) {
				BreakPointValue previousState = lastNode.getProgramState();
				lastNode.setAfterStepInState(previousState);
			}
		}
		return trace;
	}

	public AppJavaClassPath getConfig() {
		return appPath;
	}

	public void setConfig(AppJavaClassPath config) {
		this.appPath = config;
	}

	public StepRequest getStepRequest(ThreadReference thread) {
		for(StepRequest stepRequest: this.stepRequestList) {
			if (stepRequest.thread().equals(thread)) {
				return stepRequest;				
			}
		}
		
		return null;
	}

	public MethodEntryRequest getMethodEntryRequest() {
		return methodEntryRequest;
	}

	public void setMethodEntryRequest(MethodEntryRequest methodEntryRequest) {
		this.methodEntryRequest = methodEntryRequest;
	}

	public MethodExitRequest getMethodExitRequset() {
		return methodExitRequest;
	}

	public void setMethodExitRequset(MethodExitRequest methodExitRequset) {
		this.methodExitRequest = methodExitRequset;
	}

	public ClassPrepareRequest getClassPrepareRequest() {
		return classPrepareRequest;
	}

	public void setClassPrepareRequest(ClassPrepareRequest classPrepareRequest) {
		this.classPrepareRequest = classPrepareRequest;
	}

	public ExceptionRequest getExceptionRequest() {
		return exceptionRequest;
	}

	public void setExceptionRequest(ExceptionRequest exceptionRequest) {
		this.exceptionRequest = exceptionRequest;
	}

	class ExpressionValue {
		Value value;
		/**
		 * used to decide the memory address, this value must be an
		 * ObjectReference.
		 */
		Value parentValue;

		public ExpressionValue(Value value, Value parentValue, Value messageValue) {
			this.value = value;
			this.parentValue = parentValue;

		}

	}
}
