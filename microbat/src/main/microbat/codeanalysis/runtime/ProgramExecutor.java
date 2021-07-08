package microbat.codeanalysis.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
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
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;

import microbat.Activator;
import microbat.codeanalysis.ast.LocalVariableScope;
import microbat.codeanalysis.ast.VariableScopeParser;
import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.codeanalysis.bytecode.InstructionVisitor;
import microbat.codeanalysis.bytecode.SingleLineByteCodeVisitor;
import microbat.codeanalysis.runtime.herustic.HeuristicIgnoringFieldRule;
import microbat.codeanalysis.runtime.jpda.expr.ExpressionParser;
import microbat.codeanalysis.runtime.jpda.expr.ParseException;
import microbat.codeanalysis.runtime.variable.VariableValueExtractor;
import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.ClassLocation;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.ArrayValue;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.StringValue;
import microbat.model.value.VarValue;
import microbat.model.value.VirtualValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.ConstantVar;
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
 *         This class is deprecated, please check the InstrumentationExecutor class.
 * 
 */
@SuppressWarnings("restriction")
@Deprecated
public class ProgramExecutor extends Executor {
	public static final long DEFAULT_TIMEOUT = -1;
	public static String returnVariableValue = "microbat_return_value";
	public static String returnVariableType = "microbat_return_type";
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

	public ProgramExecutor() {
	}
	
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
	 * The parameter of executionOrderList is used to make sure the trace is
	 * recorded as executionOrderList shows. Different from when we record the
	 * executionOrderList, we will listen to method entry/exist event, which can
	 * introduces some steps which executionOrderList does not record. In such
	 * case, we just focus on the part of trace recorded by executionOrderList.
	 * 
	 * @param runningStatements
	 * @throws SavException
	 */
	public void run(List<BreakPoint> runningStatements, List<BreakPoint> executionOrderList, IProgressMonitor monitor,
			int stepNum, boolean isTestcaseEvaluation) throws SavException, TimeoutException {
		this.trace = new Trace(appPath);

		List<String> classScope = parseScope(runningStatements);
		List<LocalVariableScope> lvsList = parseLocalVariables(classScope, this.appPath);
		this.trace.getLocalVariableScopes().setVariableScopes(lvsList);

		List<String> exlcudes = MicroBatUtil.extractExcludeFiles("", appPath.getExternalLibPaths());
		this.addLibExcludeList(exlcudes);
		this.brkpsMap = BreakpointUtils.initBrkpsMap(runningStatements);

		long t1 = System.currentTimeMillis();
		
		VirtualMachine vm = null;
		try {
			vm = constructTrace(monitor, executionOrderList, this.appPath, stepNum, isTestcaseEvaluation);
		} finally {
			if (vm != null) {
				vm.exit(0);
			}
			System.out.println();
			System.out.println("JVM is ended.");
		}

		long t2 = System.currentTimeMillis();
		long time = t2 - t1;
		System.out.println("time spent on collecting trace: " + time);
		this.trace.setConstructTime((int)time);
	}

	/**
	 * This method is used to build the scope of local variables.
	 * 
	 * @param classScope
	 */
	private List<LocalVariableScope> parseLocalVariables(final List<String> classScope, AppJavaClassPath appPath) {
		VariableScopeParser vsParser = new VariableScopeParser();
		vsParser.parseLocalVariableScopes(classScope, appPath);
		List<LocalVariableScope> lvsList = vsParser.getVariableScopeList();
		return lvsList;
	}

	private List<String> parseScope(List<BreakPoint> breakpoints) {
		List<String> classes = new ArrayList<>();
		for (BreakPoint bp : breakpoints) {
			if (!classes.contains(bp.getDeclaringCompilationUnitName())) {
				classes.add(bp.getDeclaringCompilationUnitName());
			}
		}
		return classes;
	}

	class UsedVarValues {
		List<VarValue> readVariables = new ArrayList<>();
		List<VarValue> writtenVariables = new ArrayList<>();

		UsedVariable usedVar;

		VarValue returnedValue;

		public UsedVarValues(List<VarValue> readVariables, List<VarValue> writtenVariables, VarValue returnedValue,
				UsedVariable usedVar) {
			super();
			this.readVariables = readVariables;
			this.writtenVariables = writtenVariables;
			this.returnedValue = returnedValue;
			this.usedVar = usedVar;
		}
	}

	/**
	 * The parameter of executionOrderList is used to make sure the trace is
	 * recorded as executionOrderList shows. Different from when we record the
	 * executionOrderList, we will listen to method entry/exist event, which can
	 * introduces some steps which executionOrderList does not record. In such
	 * case, we just focus on the part of trace recorded by executionOrderList.
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
	private VirtualMachine constructTrace(IProgressMonitor monitor, List<BreakPoint> executionOrderList,
			AppJavaClassPath appClassPath, int stepNum, boolean isTestcaseEvaluation)
					throws SavException, TimeoutException {

		/** start debugger */
		VirtualMachine vm = new VMStarter(this.appPath, isTestcaseEvaluation).start();

		EventRequestManager erm = vm.eventRequestManager();

		/** add class watch, otherwise, I cannot catch the registered event */
		addClassWatch(erm);

		EventQueue eventQueue = vm.eventQueue();

		boolean stop = false;
		boolean eventTimeout = false;
		Map<String, BreakPoint> locBrpMap = new HashMap<String, BreakPoint>();

		/**
		 * We divide the library code into two categories: the interesting ones (e.g., 
		 * those in java.util.*) and the normal ones. We only capture the data and control
		 * dependency for our interested library code. However, it is possible that the
		 * interested library code is called by some normal library code, of course, these
		 * normal library code is called by application code. In such case, we will not
		 * capture the data/control dependencies of the library code called by normal library
		 * code.
		 * 
		 * To this end, when we detect the library code is called/accessed by some normal
		 * library code, we set the flag <code>isIndirectAccess</code> to be k++. Then, we
		 * will not further analyze the runtime variables even if the code is in our interested
		 * library.
		 */
		boolean isIndirectAccess = false;
		
		/**
		 * record the method entrance and exit so that I can build a
		 * tree-structure for trace node.
		 */
		Stack<TraceNode> methodNodeStack = new Stack<>();
		Stack<String> methodSignatureStack = new Stack<>();

		UsedVariable previousVars = null;

		/** this variable is used to handle exception case. */
		Location caughtLocationForJustException = null;
		
		Range range = getStartRange(appClassPath);

		String previousEvent = null;
		cancel: while (!stop && !eventTimeout) {
			EventSet eventSet;
			try {
				eventSet = eventQueue.remove(TIME_OUT);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			if (eventSet == null) {
				System.out.println("Time out! Cannot get event set!");
				System.out.println("The event before time out: " + previousEvent);
				eventTimeout = true;
				break;
			}

			for (Event event : eventSet) {
				previousEvent = createEventLog(event);
				if (event instanceof VMStartEvent) {
					System.out.println("JVM is started...");
					
					addThreadStartWatch(erm);

				} else if (event instanceof ThreadStartEvent) {
					ThreadReference threadReference = ((ThreadStartEvent) event).thread();
					if (hasValidThreadName(threadReference)) {
					}
				}
				if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
					stop = true;
					break;
				} else if (event instanceof ClassPrepareEvent) {
					ClassPrepareEvent cEvent = (ClassPrepareEvent)event;
					addStartBreakPointWatch(erm, cEvent.referenceType(), range);
					parseBreakpoints(vm, (ClassPrepareEvent) event, locBrpMap);						
				} else if (event instanceof StepEvent) {
					ThreadReference thread = ((StepEvent) event).thread();
					Location currentLocation = ((StepEvent) event).location();

					if (currentLocation.lineNumber() == -1) {
						continue;
					}
					
					if (isInIncludedLibrary(currentLocation)) {
						if (trace.size() > 0 && (!isIndirectAccess || !Settings.applyLibraryOptimization)) {
							UsedVarValues uVars = build3rdPartyLibraryDependency(thread, currentLocation, previousVars);
							previousVars = uVars.usedVar;
							TraceNode appendingNode = trace.getLatestNode();
							for (VarValue varValue : uVars.readVariables) {
								if (!(varValue.getVariable() instanceof LocalVar)) {
									if (!containsVar(appendingNode.getReadVariables(), varValue)) {
										appendingNode.addReadVariable(varValue);
									}
								}
							}
							for (VarValue varValue : uVars.writtenVariables) {
								if (!(varValue.getVariable() instanceof LocalVar)) {
									if (!containsVar(appendingNode.getWrittenVariables(), varValue)) {
										appendingNode.addWrittenVariable(varValue);
									}
								}
							}

							VarValue returnValue = uVars.returnedValue;
							if (returnValue != null && trace.getLatestNode()!=null) {
								trace.getLatestNode().getReturnedVariables().add(returnValue);
							}
						}
					}
					
					/**
					 * collect the variable values after executing previous step
					 */
					boolean isContextChange = false;
					if (trace.getLatestNode() != null) {
						/**
						 * Parsing the written variables of last step.
						 * 
						 * If the context changes, the value of some variables
						 * may not be retrieved. Thus, the variable ID cannot be
						 * generated. Note that the ID of a variable need
						 * parsing its heap ID which can only be accessed by
						 * runtime.
						 */
						isContextChange = checkContext(trace.getLatestNode().getBreakPoint(), currentLocation);
						if (!isContextChange) {
							processWrittenVariable(this.trace.getLatestNode(), this.trace.getStepVariableTable(), 
									thread, currentLocation);
						}
					}
					
					
					BreakPoint thisPoint = locBrpMap.get(currentLocation.toString());
					if(thisPoint==null) {
						continue;
					}
					
					/**
					 * This step is an interesting step (sliced statement) in
					 * our debugging process
					 */
					TraceNode node = recordTrace(thisPoint, null);
					node.setRuntimePC(currentLocation.codeIndex());

					TraceNode prevNode = node.getStepInPrevious();
					boolean isMethodEntry = isMethodEntry(prevNode, node);
					if(isMethodEntry) {
						if(prevNode!=null && !isIndirectAccess) {
							parseWrittenParameterVariableForMethodInvocation(thread, 
									prevNode, trace.getLatestNode());
							methodNodeStack.push(prevNode);
							methodSignatureStack.push(node.getMethodSign());
							isIndirectAccess = checkIndirectAccess(methodSignatureStack, methodNodeStack);
						}
					}
					
					TraceNode peekNode = methodNodeStack.empty() ? null : methodNodeStack.peek(); 
					boolean isMethodExit = isMethodExit(prevNode, node, peekNode);
					if(isMethodExit) {
						if(!methodNodeStack.isEmpty() && !isIndirectAccess) {
							creatRWReturnVariableForReturnStatement(prevNode, node);	
							int popCount = checkContextualReturn(node, methodNodeStack, false);
							if(popCount!=0){
								while(popCount != 0) {
									methodNodeStack.pop();
									methodSignatureStack.pop();
									popCount--;
								}								
							}
							else{
								methodNodeStack.pop();
								methodSignatureStack.pop();								
							}
							
							isIndirectAccess = checkIndirectAccess(methodSignatureStack, methodNodeStack);
						}
						
					}

					if(isIndirectAccess){
						int popCount = checkContextualReturn(node, methodNodeStack, false);
						while(popCount != 0) {
							methodNodeStack.pop();
							methodSignatureStack.pop();
							popCount--;
						}
						isIndirectAccess = checkIndirectAccess(methodSignatureStack, methodNodeStack);
//						if(isSameLocation(node, methodNodeStack.peek())){
//							methodNodeStack.pop();
//							methodSignatureStack.pop();
//							isIndirectAccess = checkIndirectAccess(methodSignatureStack, methodNodeStack);
//						}
					}
					
					/**
					 * pop up method after an exception is caught.
					 */
					if (caughtLocationForJustException != null) {
						popUpMethodCausedByException(methodNodeStack, methodSignatureStack,
								caughtLocationForJustException);
						caughtLocationForJustException = null;
					} 

					/**
					 * Build parent-child relation between trace nodes.
					 */
					if (!methodNodeStack.isEmpty()) {
						TraceNode parentInvocationNode = methodNodeStack.peek();
						if(parentInvocationNode != null) {
							parentInvocationNode.addInvocationChild(node);
							node.setInvocationParent(parentInvocationNode);								
						}
					}

					processReadVariable(node, thread, currentLocation);
					processReturnVariable(node, thread, currentLocation);
					
					appendReadVariableFromStepOver(node);
					
					monitor.worked(1);
					printProgress(trace.size(), stepNum);

					if (monitor.isCanceled() || this.trace.size() >= Settings.stepLimit
							|| this.trace.size() >= executionOrderList.size()) {
						stop = true;
						break cancel;
					}
				} 
				else if(event instanceof BreakpointEvent){
					addStepWatch(erm, ((BreakpointEvent) event).thread());
					addExceptionWatch(erm);
					excludeJUnitLibs();
				}
				else if (event instanceof ExceptionEvent) {
					ExceptionEvent ee = (ExceptionEvent) event;
					Location catchLocation = ee.catchLocation();
					TraceNode lastNode = this.trace.getLatestNode();
					if (lastNode != null) {
						lastNode.setException(true);

						if (catchLocation == null) {
							stop = true;
						} else {
							caughtLocationForJustException = ee.catchLocation();
						}
					}
				} else if (event instanceof BreakpointEvent) {
					System.currentTimeMillis();
				}
			}

			eventSet.resume();
		}

		return vm;
	}
	
	/**
	 * given a trace step, check whether any invoker step in method stack share the same
	 * location/context with this trace step.
	 * 
	 * the parameter <code>isPrecise</code> to specify whether the given trace step and
	 * some invoker in method stack share same location or context.
	 * 
	 * @param node
	 * @param methodNodeStack
	 * @param isPrecise
	 * @return
	 */
	private int checkContextualReturn(TraceNode node, Stack<TraceNode> methodNodeStack, boolean isPrecise) {
		int count = 0;
		boolean found = false;
		
		Stack<TraceNode> tempStack = new Stack<>();
		while(!methodNodeStack.isEmpty()) {
			TraceNode stackNode = methodNodeStack.pop();
			tempStack.push(stackNode);
			count++;
			if(isPrecise){
				if(isSameLocation(node, stackNode) && stackNode.getRuntimePC()<node.getRuntimePC()) {
					found = true;
					break;
				}
			}
			else{
				if(isSameContext(node, stackNode)) {
					if(isSameLocation(node, stackNode)){//in case a recursive method.
						if(node.getRuntimePC()>stackNode.getRuntimePC()){
							found = true;
							break;
						}
					}
					else{
						found = true;
						break;						
					}
				}
			}
		}
		
		if(!found) {
			count = 0;
		}
		
		while(!tempStack.isEmpty()) {
			TraceNode sNode = tempStack.pop();
			methodNodeStack.push(sNode);
		}
		
		return count;
	}

	private boolean isSameLocation(TraceNode node, TraceNode peek) {
		BreakPoint b1 = node.getBreakPoint();
		BreakPoint b2 = peek.getBreakPoint();
		
		return b1.getDeclaringCompilationUnitName().equals(b2.getDeclaringCompilationUnitName())
				&& b1.getLineNumber()==b2.getLineNumber();
	}
	
	private boolean isSameContext(TraceNode node, TraceNode peek) {
		BreakPoint b1 = node.getBreakPoint();
		BreakPoint b2 = peek.getBreakPoint();
		
		if(b1.getClassCanonicalName().equals(b2.getClassCanonicalName())) {
//			String nodeMethodName = b1.getMethodName();
//			String peekMethodName = b2.getMethodName();
			
//			if(peekMethodName.contains("<clinit>") && nodeMethodName.contains("<init>")) {
//				return true;
//			}
//			else {
//				return b1.getMethodSign().equals(b2.getMethodSign());				
//			}
			
			return b1.getMethodSign().equals(b2.getMethodSign());		
		}
		
		return false;
	}

	private void creatRWReturnVariableForReturnStatement(TraceNode prevNode, TraceNode node) {
		for(VarValue returnValue: prevNode.getReturnedVariables()) {
			prevNode.addWrittenVariable(returnValue);
			node.addReadVariable(returnValue);
			
			List<StepVariableRelationEntry> entries = constructStepVariableEntry(trace.getStepVariableTable(), returnValue);
			for(StepVariableRelationEntry entry: entries){
				entry.addProducer(prevNode);
				entry.addConsumer(node);
			}
		}
	}
	
	private boolean isPointEndOfMethod(BreakPoint point){
		SingleLineByteCodeVisitor visitor = findMethodByteCode(point);
		CFGConstructor cfgConstructor = new CFGConstructor();
		CFG cfg = cfgConstructor.constructCFG(visitor.getMethod().getCode());
		
		for(CFGNode exitNode: cfg.getExitList()) {
			for(InstructionHandle handle: visitor.getInstructionList()){
				if(handle.getPosition()==exitNode.getInstructionHandle().getPosition()){
					return true;
				}
			}
			
		}
		return false;
	}

	private boolean isPointEndOfMethod(TraceNode node) {
		BreakPoint point = node.getBreakPoint();
		SingleLineByteCodeVisitor visitor = findMethodByteCode(point);
		
		InstructionHandle lastHandle = visitor.getInstructionList().get(visitor.getInstructionList().size()-1);
		if(lastHandle.getPosition()<node.getRuntimePC()){
			return isPointEndOfMethod(point);
		}
		
		CFGConstructor cfgConstructor = new CFGConstructor();
		CFG cfg = cfgConstructor.constructCFG(visitor.getMethod().getCode());
		
		System.currentTimeMillis();
		
		List<InstructionHandle> range = new ArrayList<>();
		for(CFGNode exitNode: cfg.getExitList()) {
			if(exitNode.getInstructionHandle().getPosition()==node.getRuntimePC()) {
				return true;
			}	
			
			CFGNode cfgNode = exitNode;
			range.add(cfgNode.getInstructionHandle());
			while(cfgNode.getParents().size()==1) {
				CFGNode parent = cfgNode.getParents().get(0);
				if(parent.getChildren().size()==1 && !isParentInvokeAppMethod(parent, visitor.getMethod())) {
					if(isContains(parent.getInstructionHandle(), visitor.getInstructionList())){
						cfgNode = parent;
						range.add(cfgNode.getInstructionHandle());
					}
					else{
						break;
					}
				}
				else {
					break;
				}
			}
			for(InstructionHandle ins: range) {
				if(ins.getPosition()==node.getRuntimePC()) {
					return true;
				}						
			}
			
		}
		return false;
	}

	String[] excludes = null;
	private boolean isParentInvokeAppMethod(CFGNode parent, org.apache.bcel.classfile.Method method) {
		if(excludes == null){
			excludes = new String[Executor.getLibExcludes().length];
			for(int i=0; i<Executor.getLibExcludes().length; i++){
				String exclude = Executor.getLibExcludes()[i];
				exclude = exclude.replace("*", "");
				excludes[i] = exclude;
			}
			Arrays.sort(excludes);
		}
		
		Instruction ins = parent.getInstructionHandle().getInstruction();
		if(ins instanceof InvokeInstruction){
			InvokeInstruction invokeIns = (InvokeInstruction)ins;
			ConstantPool pool = method.getConstantPool();
			ConstantPoolGen gen = new ConstantPoolGen(pool);
			String declareType = invokeIns.getClassName(gen);
			
			int start = 0;
			int end = excludes.length-1;
			
			int prevIndex = -1;
			while(start<end){
				int mid = (start+end)/2;
				if(mid==prevIndex){
					break;
				}
				
				String exclude = excludes[mid];
				if(declareType.contains(exclude)){
					return false;
				}
				else{
					int compareResult = exclude.compareTo(declareType);
					if(compareResult<0){
						start = mid;
					}
					else{
						end = mid;	
					}
				}
				prevIndex = mid;
			}
			
//			for(String str: Executor.libExcludes){
//				String str0 = str.replace("*", "");
//				if(declareType.contains(str0)){
//					return false;
//				}
//			}
			
			return true;
		}
		
		return false;
	}

	private boolean isContains(InstructionHandle ins, List<InstructionHandle> instructionList) {
		for(InstructionHandle handle: instructionList){
			if(handle.getPosition()==ins.getPosition()){
				return true;
			}
		}
		return false;
	}

	private SingleLineByteCodeVisitor findMethodByteCode(BreakPoint point) {
		String className = point.getClassCanonicalName();
		int lineNumber = point.getLineNumber();
		
		String locationID = className + "$" + lineNumber;
		SingleLineByteCodeVisitor visitor = libraryLine2LineVisitorMap.get(locationID);
//		visitor = null;
		if (visitor == null) {
			visitor = new SingleLineByteCodeVisitor(lineNumber, className, appPath);
			ByteCodeParser.parse(className, visitor, appPath);
			libraryLine2LineVisitorMap.put(locationID, visitor);
		}
		
		return visitor;
	}
	
	private boolean isMethodEntry(TraceNode prevNode, TraceNode thisNode) {
		if(prevNode==null) {
			return true;
		}
		
		if(thisNode.getLineNumber()==1){
			return true;
		}
		
		BreakPoint point = thisNode.getBreakPoint();
		SingleLineByteCodeVisitor visitor = findMethodByteCode(point);
		
		InstructionHandle lastHandle = visitor.getInstructionList().get(visitor.getInstructionList().size()-1);
		if(lastHandle.getPosition()<thisNode.getRuntimePC()){
			return isPointStartOfMethod(point);
		}
		
		return thisNode.getRuntimePC()<=1;
	}

	private boolean isPointStartOfMethod(BreakPoint point) {
		SingleLineByteCodeVisitor visitor = findMethodByteCode(point);
		if(visitor.getJavaClass().isAnonymous()){
			if(point.getMethodName().equals("<init>")){
				return true;
			}
		}
		
		CFGConstructor cfgConstructor = new CFGConstructor();
		CFG cfg = cfgConstructor.constructCFG(visitor.getMethod().getCode());
		InstructionHandle startHandle = cfg.getStartNode().getInstructionHandle();
		
		for(InstructionHandle handle: visitor.getInstructionList()){
			if(handle.getPosition()==startHandle.getPosition()){
				return true;
			}
		}
		
		return false;
	}

	private boolean isMethodExit(TraceNode prevNode, TraceNode thisNode, TraceNode peekNode) {
		if(prevNode==null) {
			return true;
		}
		
		/**
		 * try finding a matching step before
		 */
		if(peekNode != null){
			if(peekNode.getBreakPoint().equals(thisNode.getBreakPoint()) &&
					peekNode.getRuntimePC()<thisNode.getRuntimePC()){
				return true;
			}
		}
		
		boolean isPrevNodePointEndOfMethod = isPointEndOfMethod(prevNode);
		if(!isPrevNodePointEndOfMethod) {
			return false;
		}
		
		/**
		 * try to check whether the node after it is still a return node in the same method,
		 * if yes, the method exit event for prev node should not happen.
		 */
		boolean isThisNodePointEndOfMethod = isPointEndOfMethod(thisNode);
		if(isThisNodePointEndOfMethod){
			MethodDeclaration prevMethod = getMethodByAST(prevNode.getBreakPoint());
			MethodDeclaration thisMethod = getMethodByAST(thisNode.getBreakPoint());
			if(prevNode.getClassCanonicalName().equals(thisNode.getClassCanonicalName())){
				if(prevMethod!=null && thisMethod!=null) {
					if(prevMethod.equals(thisMethod)) {
						return false;
					}
				}
//				else if(prevMethod==null && thisMethod==null) {
//					return false;
//				}
				
			}
			
			if(prevNode.getMethodSign().equals(thisNode.getMethodSign())){
				return false;
			}
		}
		
//		System.currentTimeMillis();
		
		BreakPoint prevPoint = prevNode.getBreakPoint(); 
		BreakPoint thisPoint = thisNode.getBreakPoint();
		boolean isContextDiff = isContextDiff(prevPoint, thisPoint);

		return isPrevNodePointEndOfMethod && isContextDiff;
	}
	
	private boolean isThisNodeInOverrideMethod(TraceNode thisNode, TraceNode prevNode) {
		if(thisNode.getMethodName().equals(prevNode.getMethodName())) {
			String prevClass = prevNode.getClassCanonicalName();
			
			SingleLineByteCodeVisitor v = findMethodByteCode(thisNode.getBreakPoint());
			JavaClass superClass;
			try {
				superClass = v.getJavaClass().getSuperClass();
				if(superClass!=null) {
					String name = superClass.getClassName();
					return name.equals(prevClass);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		System.currentTimeMillis();
		return false;
	}

	private Map<BreakPoint, MethodDeclaration> lineMethodMap = new HashMap<>();
	private MethodDeclaration getMethodByAST(BreakPoint point) {
		MethodDeclaration method = lineMethodMap.get(point);
		if(method==null) {
			CompilationUnit thisCU = 
					JavaUtil.findCompilationUnitInProject(point.getDeclaringCompilationUnitName(), this.appPath);
			MethodFinder thisFinder = new MethodFinder(thisCU, point.getLineNumber());
			thisCU.accept(thisFinder);
			method = thisFinder.candidate;
			lineMethodMap.put(point, method);
		}
		
		return method;
	}
	
	private boolean isContextDiff(BreakPoint thisPoint, BreakPoint thatPoint) {
		MethodDeclaration thisMethod = getMethodByAST(thisPoint);
		MethodDeclaration thatMethod = getMethodByAST(thatPoint);
		if(thisMethod==null && thatMethod==null) {
			return !thisPoint.getClassCanonicalName().equals(thatPoint.getClassCanonicalName());
		}
//		else if(thisMethod!=null && thatMethod!=null) {
//			return thisMethod.getStartPosition()!=thatMethod.getStartPosition();
//		}
		
		return true;
	}
	
	private boolean checkContext(BreakPoint lastSteppingPoint, Location loc) {
		String methodSign1 = lastSteppingPoint.getMethodSign();
		if (methodSign1 == null) {
			return true;
		}
		methodSign1 = methodSign1.substring(methodSign1.lastIndexOf("#") + 1, methodSign1.length());

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

	private HashMap<String, List<String>> invokationMap = new HashMap<>();
	private boolean isMethodInvokeRelationship(BreakPoint invokerPoint, String invokeeSignature) {
		String invokerSignature = invokerPoint.getMethodSign();
		List<String> invokees = invokationMap.get(invokerSignature);
//		List<String> invokees = null;
		if(invokees==null) {
			invokees = new ArrayList<>();
		}
		
		if(invokees.contains(invokeeSignature)) {
			return true;
		}
		else {
			SingleLineByteCodeVisitor visitor = findMethodByteCode(invokerPoint);
			List<InstructionHandle> list = visitor.getInstructionList();
			boolean isOk = findInvokingMethod(invokeeSignature, visitor, list);
			
			if(isOk) {
				invokees.add(invokeeSignature);
				invokationMap.put(invokerSignature, invokees);
			}
			
			return isOk;
		}
		
	}

	/**
	 * There is a conservative analysis for checking indirect access/invocation.
	 * More specifically, a line such as the start of a class, can be corresponded to 
	 * multiple byte code instructions or associated with many byte code level method. 
	 * It is dogmatic to infer which method can be associated with this line in the 
	 * static analysis. for now, once a line is the start of a class, we set its method 
	 * as unknown so that we can have a conservative analysis to figure out whether 
	 * two methods have invocation relationship. namely, an unknown method may invoke 
	 * any methods and be invoked by any methods.
	 * 
	 * @param methodSignatureStack
	 * @param methodNodeStack
	 * @return
	 */
	private boolean checkIndirectAccess(Stack<String> methodSignatureStack, Stack<TraceNode> methodNodeStack) {
		if(methodSignatureStack.size()<=1) {
			return false;
		}
		
		int size = methodSignatureStack.size();
		
		for(int i=0; i<size; i++) {
			
			TraceNode node = methodNodeStack.get(i);
			String method = methodSignatureStack.get(i);
			
			if(node.getBreakPoint().isStartOfClass() || method.equals(ClassLocation.UNKNOWN_METHOD_SIGN)){
				continue;
			}
			
			boolean isMethodInvokeRelationship = 
					isMethodInvokeRelationship(node.getBreakPoint(), method);
			/**
			 * reflection and static constructor
			 */
			boolean isPossibleRefection = method.contains("java.util.ResourceBundle#getBundle");
			if(!isMethodInvokeRelationship && !isPossibleRefection) {
				return true;
			}
		}
		
		return false;
	}

	private boolean findInvokingMethod(String invokedMethodSig, SingleLineByteCodeVisitor visitor,
			List<InstructionHandle> peekList) {
		String invokedMethodName = invokedMethodSig.substring(invokedMethodSig.indexOf("#")+1, invokedMethodSig.indexOf("("));
		
		for(InstructionHandle handle: peekList) {
			Instruction ins = handle.getInstruction();
			if(ins instanceof InvokeInstruction) {
				InvokeInstruction iIns = (InvokeInstruction)ins;
				ConstantPool pool = visitor.getMethod().getConstantPool();
				ConstantPoolGen gen = new ConstantPoolGen(pool);
				String methodName = iIns.getMethodName(gen);
				if(methodName.equals(invokedMethodName)) {
					return true;
				}
			}
		}
		
		return false;
	}

	private void appendReadVariableFromStepOver(TraceNode node) {
		TraceNode previousStepOverNode = node.getStepOverPrevious();
		if(previousStepOverNode!=null && node.getLineNumber()==previousStepOverNode.getLineNumber()){
			for(VarValue readVar: previousStepOverNode.getReadVariables()){
				if(!node.containSynonymousReadVar(readVar)){
					node.addReadVariable(readVar);
					List<StepVariableRelationEntry> entries = constructStepVariableEntry(trace.getStepVariableTable(), readVar);
					for(StepVariableRelationEntry entry: entries){
						entry.addConsumer(trace.getLatestNode());
					}
				}
			}
		}
		
	}

	private boolean containsVar(List<VarValue> readVariables, VarValue varValue) {
		for (VarValue value : readVariables) {
			if (value.getVariable().getVarID().equals(varValue.getVariable().getVarID())
					&& value.getVariable().getName().equals(varValue.getVariable().getName())) {
				return true;
			}
		}
		return false;
	}

	private UsedVarValues build3rdPartyLibraryDependency(ThreadReference thread, Location currentLocation,
			UsedVariable previousVars) {
		UsedVarValues uVars = parseUsedVariable(thread, currentLocation, previousVars);
		
		for (VarValue readVar : uVars.readVariables) {
			List<StepVariableRelationEntry> entries = constructStepVariableEntry(trace.getStepVariableTable(), readVar);
			for(StepVariableRelationEntry entry: entries){
				entry.addConsumer(trace.getLatestNode());
			}
		}

		for (VarValue writtenVar : uVars.writtenVariables) {
			List<VarValue> list = new ArrayList<>();
			list.add(writtenVar);
			List<VarValue> children = writtenVar.getAllDescedentChildren();
			list.addAll(children);
			
			for(VarValue v: list){
				List<StepVariableRelationEntry> entries = constructStepVariableEntry(trace.getStepVariableTable(), v);
				for(StepVariableRelationEntry entry: entries){
					entry.addProducer(trace.getLatestNode());
				}
			}
		}

		return uVars;
	}

	private HashMap<String, SingleLineByteCodeVisitor> libraryLine2LineVisitorMap = new HashMap<>();
	private HashMap<String, InstructionVisitor> libraryLine2InstructionVisitorMap = new HashMap<>();

	class UsedVariable {
		String method;
		List<Variable> readVariables = new ArrayList<>();
		List<Variable> writtenVariables = new ArrayList<>();
		Variable returnedVar;

		public UsedVariable(List<Variable> readVariables, List<Variable> writtenVariables, Variable returnedVar, String method) {
			super();
			this.readVariables = readVariables;
			this.writtenVariables = writtenVariables;
			this.returnedVar = returnedVar;
			this.method = method;
		}
	}

	private UsedVarValues parseUsedVariable(ThreadReference thread, Location currentLocation,
			UsedVariable previousVars) {
		int lineNumber = currentLocation.lineNumber();
		String className = currentLocation.declaringType().name();

		String locationID = className + "$" + lineNumber;
		SingleLineByteCodeVisitor visitor = libraryLine2LineVisitorMap.get(locationID);
		if (visitor == null) {
			visitor = new SingleLineByteCodeVisitor(lineNumber, className, appPath);
			ByteCodeParser.parse(className, visitor, appPath);
			libraryLine2LineVisitorMap.put(locationID, visitor);
		}
		
		List<Variable> readVars = visitor.getReadVars();
		List<Variable> writtenVars = visitor.getWrittenVars();
		Variable returnedVar = visitor.getReturnedVar();

		String method = currentLocation.method().name();
		UsedVariable uVars = new UsedVariable(readVars, writtenVars, returnedVar, method);

		List<Variable> nonReadArrayElements = new ArrayList<>();
		for(Variable v: uVars.readVariables){
			if(!(v instanceof ArrayElementVar)){
				nonReadArrayElements.add(v);
			}
		}
		
		List<VarValue> readVarValues = parseValue(nonReadArrayElements, thread, Variable.READ);
		List<VarValue> writtenVarValues = new ArrayList<>();
		String currentMethod = currentLocation.method().name();
		if (previousVars != null && currentMethod.equals(previousVars.method)) {
			writtenVarValues = parseValue(previousVars.writtenVariables, thread, Variable.WRITTEN);
			
			List<Variable> previousReadArrayElements = new ArrayList<>();
			for(Variable v: previousVars.readVariables){
				if(v instanceof ArrayElementVar){
					previousReadArrayElements.add(v);
				}
			}
			System.currentTimeMillis();
			List<VarValue> readArrayEleValues = parseValue(previousReadArrayElements, thread, Variable.READ);
			readVarValues.addAll(readArrayEleValues);
		}

		VarValue returnedValue = null;
		if(uVars.returnedVar!=null && !(uVars.returnedVar instanceof ConstantVar)) {
			List<Variable> vars = new ArrayList<>();
			vars.add(uVars.returnedVar);			
			List<VarValue> returnedValues = parseValue(vars, thread, Variable.WRITTEN);
			returnedValue = returnedValues.get(0);
			System.currentTimeMillis();
		}
		UsedVarValues uValues = new UsedVarValues(readVarValues, writtenVarValues, returnedValue, uVars);
		return uValues;
	}

	private List<VarValue> parseValue(List<Variable> vars, ThreadReference thread,
			String accessType) {
		List<VarValue> values = new ArrayList<>();
		if(vars.isEmpty()) {
			return values;
		}

		try {
			StackFrame frame = thread.frame(0);
			for (Variable v1 : vars) {
				/**
				 * For read variable in library, we only need to record static
				 * fields.
				 */
				if (accessType.equals(Variable.READ)) {
					if (v1 instanceof LocalVar) {
						continue;
					}
				}
				
				Variable var = v1.clone();
				
				if(var instanceof ConstantVar) {
//					ConstantVar cVar = (ConstantVar)var;
//					String varID = UUID.randomUUID().toString() + ":" + trace.getLatestNode().getOrder(); 
//					cVar.setVarID(varID);
//					PrimitiveValue pValue = new PrimitiveValue(cVar.getValue(), false, cVar);
//					values.add(pValue);
					continue;
				}
				
				ExpressionValue expValue = retriveExpression(frame, var.getName(), null);
				if (expValue == null) {
					continue;
				}

				VarValue varValue = null;
				Value value = expValue.value;

				if (value instanceof ObjectReference) {
					if (var instanceof ArrayElementVar) {
						ArrayReference ref = (ArrayReference) value;
						if(ref.length()==0){
							continue;
						}
						
						List<Value> subValues = ref.getValues();
						
						LocalVariable localVariable = frame.visibleVariableByName(Activator.tempVariableName);
						Value val = frame.getValue(localVariable);
						IntegerValue intVal = (IntegerValue) val;
						int index = intVal.value();
						if(index >= subValues.size() || index < 0){
							continue;
						}						
						
						String newVarName = var.getName() + "[" + index + "]";
						var.setName(newVarName);
						
						Value sv = subValues.get(index);
						
						String aliasVarID = Variable.concanateArrayElementVarID(String.valueOf(ref.uniqueID()),
								String.valueOf(index));
						
						if (sv instanceof ObjectReference) {
							ObjectReference obj = (ObjectReference) sv;
							String varID = String.valueOf(obj.uniqueID());
							String order = trace.findDefiningNodeOrder(accessType, trace.getLatestNode(), varID, aliasVarID);
							varID = varID + ":" + order;
							aliasVarID = aliasVarID + ":" + order;
							
							Variable subVar = var.clone();
							subVar.setAliasVarID(aliasVarID);
							
							VarValue subVarValue = new ReferenceValue(false, obj.uniqueID(), false, subVar);
							subVarValue.setVarID(varID);
							subVarValue.setStringValue("$IN_LIB");
							values.add(subVarValue);
						} else /* if(sv!=null) */ {
							String order = trace.findDefiningNodeOrder(accessType, trace.getLatestNode(), aliasVarID, var.getAliasVarID());
							aliasVarID = aliasVarID + ":" + order;
							
							Variable subVar = var.clone();
							subVar.setAliasVarID(aliasVarID);
							VarValue subVarValue = new PrimitiveValue(null, false, subVar);
							subVarValue.setVarID(aliasVarID);
							String stringValue = (sv==null)? "$IN_LIB" : sv.toString();
							subVarValue.setStringValue(stringValue);
							values.add(subVarValue);
						}
					} else {
						ObjectReference objRef = (ObjectReference) value;
						String varID = String.valueOf(objRef.uniqueID());

						String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType, trace.getLatestNode(),
								 varID, var.getAliasVarID());
						varID = varID + ":" + definingNodeOrder;
						var.setVarID(varID);

						if (value.type().toString().equals("java.lang.String")) {
							String strValue = value.toString();
							strValue = strValue.substring(1, strValue.length() - 1);
							varValue = new StringValue(strValue, false, var);
						} else {
							varValue = new ReferenceValue(false, objRef.uniqueID(), false, var);
							varValue.setStringValue("$IN_LIB");
							
							if (objRef instanceof ArrayReference) {
								ArrayReference arrayValue = (ArrayReference) objRef;
								varValue = constructArrayVarValue(arrayValue, var, frame.thread(), null, accessType, 0);
							} else {
								varValue = constructReferenceVarValue(objRef, var, frame.thread(), null, accessType, 0);
							}

							StringBuffer buffer = new StringBuffer();
							buffer.append("[");
							for (VarValue child : varValue.getChildren()) {
								buffer.append(child.getVarName() + "=" + child.getStringValue());
								buffer.append(",");
							}
							buffer.append("]");
							String content = buffer.toString();
							int len = (content.length()<100) ? content.length() : 100;
							content = content.substring(0, len);
							varValue.setStringValue(content);
							
						}
						values.add(varValue);
					}
				}
				/**
				 * its a primitive type
				 */
				else {
					/**
					 * see whether its a local variable
					 */
					if (var instanceof LocalVar) {
						// do nothing
					}
					/**
					 * It's a field or array element.
					 */
					else {
						Value parentValue = expValue.parentValue;
						ObjectReference objRef = (ObjectReference) parentValue;

						if (objRef == null) {
							objRef = null;
							try {
								objRef = frame.thisObject();
							} catch (Exception e) {
							}
							if (objRef == null) {
								return null;
							}
						}

						if (var instanceof FieldVar) {
							String varID = null;
							if (((FieldVar) var).isStatic()) {
								varID = var.getName();
							} else {
								varID = Variable.concanateFieldVarID(String.valueOf(objRef.uniqueID()),
										var.getSimpleName());
							}
							String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType,
									trace.getLatestNode(), varID, null);
							varID = varID + ":" + definingNodeOrder;
							var.setVarID(varID);
						}
					}

					String content = (value == null) ? null : value.toString();
					varValue = new PrimitiveValue(content, false, var);
					values.add(varValue);
				}

			}
		} catch (IncompatibleThreadStateException | AbsentInformationException e) {
			e.printStackTrace();
		}

		return values;
	}

	private List<Event> sortEvents(EventSet eventSet) {
		Event[] events = eventSet.toArray(new Event[0]);
		List<Event> list = new ArrayList<>();
		for (Event e : events) {
			list.add(e);
		}

		Collections.sort(list, new Comparator<Event>() {

			@Override
			public int compare(Event o1, Event o2) {
				int score1 = getScore(o1);
				int score2 = getScore(o2);
				return score2 - score1;
			}

			private int getScore(Event o) {
				if (o instanceof StepEvent) {
					return -1;
				} else if ((o instanceof MethodEntryEvent) || (o instanceof MethodExitEvent)) {
					return 1;
				}

				return 0;
			}
		});

		return list;
	}

	private void popUpMethodCausedByException(Stack<TraceNode> methodNodeStack, 
			Stack<String> methodSignatureStack,
			Location caughtLocationForJustException) {
		if(isInIncludedLibrary(caughtLocationForJustException)) {
			return;
		}
		
		if(!methodNodeStack.isEmpty()) {
			String name = caughtLocationForJustException.method().name();
			String sig = caughtLocationForJustException.method().signature();
			String m = name + sig;
			
			/**
			 * if the exception is caught by current method, we do not need to pop
			 */
			String currentMethod = methodSignatureStack.peek();
			if(currentMethod.contains(m)){
				return;
			}
			
			/**
			 * the last popped method should share the context with m
			 */
			String peek = methodNodeStack.peek().getMethodSign();
			while(!peek.contains(m)) {
				if (!methodNodeStack.isEmpty()) {
					TraceNode node = methodNodeStack.pop();
					peek = node.getMethodSign();
					methodSignatureStack.pop();
				}
				else {
					break;
				}
			}
		}
		
		System.currentTimeMillis();
	}

	private BreakPoint getNextPoint(List<BreakPoint> executionOrderList) {
		int index = trace.getExecutionList().size();
		if (index >= executionOrderList.size()) {
			return null;
		}

		return executionOrderList.get(index);
	}

	private void printProgress(int size, int stepNum) {
		double progress = ((double) size) / stepNum;

		double preProgr = 0;
		if (size == 1) {
			System.out.print("progress: ");
		} else {
			preProgr = ((double) (size - 1)) / stepNum;
		}

		int prog = (int) (progress * 100);
		int preP = (int) (preProgr * 100);

		int diff = prog - preP;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < diff; i++) {
			buffer.append("=");
		}
		System.out.print(buffer.toString());

		int[] percentiles = { 10, 20, 30, 40, 50, 60, 70, 80, 90 };
		for (int i = 0; i < percentiles.length; i++) {
			int percentile = percentiles[i];
			if (preP < percentile && percentile <= prog) {
				System.out.print(prog + "%");
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
	
	class ReturnStatementFinder extends ASTVisitor{
		CompilationUnit cu;
		int lineNumber;
		
		public ReturnStatementFinder(CompilationUnit cu, int lineNumber){
			this.cu = cu;
			this.lineNumber = lineNumber;
		}
		
		ReturnStatement returnStatement;
		
		@Override
		public boolean visit(MethodDeclaration md){
			int start = cu.getLineNumber(md.getStartPosition());
			int end = cu.getLineNumber(md.getStartPosition()+md.getLength());
			if(lineNumber<start || end<lineNumber){
				return false;
			}
			return true;
		}
		
		@Override
		public boolean visit(ReturnStatement rStat){
			int start = cu.getLineNumber(rStat.getStartPosition());
			int end = cu.getLineNumber(rStat.getStartPosition()+rStat.getLength());
			if(start<=lineNumber && lineNumber<=end){
				this.returnStatement = rStat;
				return true;
			}
			
			return false;
		}
	}

	class MethodFinder extends ASTVisitor {
		CompilationUnit cu;
		int lineNumber;

		MethodDeclaration candidate;

		public MethodFinder(CompilationUnit cu, int lineNumber) {
			this.cu = cu;
			this.lineNumber = lineNumber;
		}

		@Override
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
			}

			return true;
		}
	}


	class MethodNameRetriever extends ASTVisitor {
		MethodDeclaration innerMostMethod = null;

		CompilationUnit cu;
		int lineNumber;

		public MethodNameRetriever(CompilationUnit cu, int lineNumber) {
			this.cu = cu;
			this.lineNumber = lineNumber;
		}

		@Override
		public boolean visit(MethodDeclaration md) {
			int start = cu.getLineNumber(md.getStartPosition());
			int end = cu.getLineNumber(md.getStartPosition() + md.getLength());

			if (start <= lineNumber && lineNumber <= end) {
				if (innerMostMethod == null) {
					innerMostMethod = md;
				} else {
					if (isMoreInner(md, innerMostMethod)) {
						innerMostMethod = md;
					}
				}
				return true;
			} else {
				return false;
			}
		}

		private boolean isMoreInner(MethodDeclaration oldMD, MethodDeclaration newMD) {
			int oldStart = cu.getLineNumber(oldMD.getStartPosition());
			int oldEnd = cu.getLineNumber(oldMD.getStartPosition() + oldMD.getLength());

			int newStart = cu.getLineNumber(newMD.getStartPosition());
			int newEnd = cu.getLineNumber(newMD.getStartPosition() + newMD.getLength());

			if (oldStart <= newStart && newEnd <= oldEnd) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * build the written relations between method invocation
	 */
	private void parseWrittenParameterVariableForMethodInvocation(ThreadReference thread,
			TraceNode invokerNode, TraceNode invokeeNode) {

		StackFrame frame = null;
		try {
			frame = thread.frame(0);
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		}
		
		if (frame == null) {
			return;
		}
		
		String compilationUnit = invokeeNode.getDeclaringCompilationUnitName();
		int lineNumber = invokeeNode.getLineNumber();

		SingleLineByteCodeVisitor visitor = findMethodByteCode(invokeeNode.getBreakPoint());
		List<Param> paramList = parseParameterList(visitor.getMethod());
		for (Param param : paramList) {
			Value value = JavaUtil.retriveExpression(frame, param.getName());
			
			LocalVar localVar = new LocalVar(param.getName(), param.getType(),
					compilationUnit, lineNumber);
			localVar.setParameter(true);
			VarValue varValue = null;

			if (!(value instanceof ObjectReference) || value == null) {
				VariableScopeParser parser = new VariableScopeParser();
				LocalVariableScope scope = parser.parseMethodScope(compilationUnit, lineNumber,
						localVar.getName(), appPath);
				String varID;
				if (scope != null) {
					varID = Variable.concanateLocalVarID(compilationUnit, localVar.getName(),
							scope.getStartLine(), scope.getEndLine());
					String definingNodeOrder = this.trace.findDefiningNodeOrder(Variable.WRITTEN, invokerNode, 
							varID, localVar.getAliasVarID());
					varID = varID + ":" + definingNodeOrder;
					localVar.setVarID(varID);
				} else {
					System.currentTimeMillis();
				}
				
				if (value != null) {
					varValue = new PrimitiveValue(value.toString(), false, localVar);
				}
			} 
			else {
				ObjectReference objRef = (ObjectReference) value;
				String varID = String.valueOf(objRef.uniqueID());
//				String definingNodeOrder = this.trace.findDefiningNodeOrder(Variable.WRITTEN, invokerNode, 
//						varID, null);
//				varID = varID + ":" + definingNodeOrder;
				localVar.setVarID(varID);
				
				varValue = new ReferenceValue(true, false, localVar);
				
				if (objRef instanceof ArrayReference) {
					ArrayReference arrayValue = (ArrayReference) objRef;
					varValue = constructArrayVarValue(arrayValue, localVar, frame.thread(), 
							invokerNode.getBreakPoint(), Variable.WRITTEN, Settings.getVariableLayer());
				} else {
					varValue = constructReferenceVarValue(objRef, localVar, frame.thread(), 
							invokerNode.getBreakPoint(), Variable.WRITTEN, Settings.getVariableLayer());
				}

				StringBuffer buffer = new StringBuffer();
				buffer.append("[");
				for (VarValue child : varValue.getChildren()) {
					buffer.append(child.getVarName() + "=" + child.getStringValue());
					buffer.append(",");
				}
				buffer.append("]");
				varValue.setStringValue(buffer.toString());
			}

			StepVariableRelationEntry entry = this.trace.getStepVariableTable().get(localVar.getVarID());
			if (entry == null && localVar.getVarID() != null) {
				entry = new StepVariableRelationEntry(localVar.getVarID());
				this.trace.getStepVariableTable().put(localVar.getVarID(), entry);
			}

			if (entry != null) {
				entry.addAliasVariable(localVar);
				entry.addProducer(invokerNode);

				if (varValue != null && varValue.getVarID() != null) {
					invokerNode.addWrittenVariable(varValue);
				}
			}
		}
	}

	private List<Param> parseParameterList(org.apache.bcel.classfile.Method method) {
		List<Param> paramList = new ArrayList<>();
		LocalVariableTable table = method.getCode().getLocalVariableTable();
		if(table==null || method.getArgumentTypes().length==0) {
			return paramList;
		}
		
		String methodString = method.toString();
		String parameterString = methodString.substring(methodString.indexOf("(")+1, methodString.indexOf(")"));
		List<String> paraList = new ArrayList<>();
		if(parameterString.contains(",")) {
			String[] params = parameterString.split(",");	
			for(String p: params) {
				paraList.add(p);
			}
		}
		else {
			paraList.add(parameterString);
		}
		
		
		for(String pStr: paraList) {
			pStr = pStr.trim();
			if(pStr.contains(" ")) {
				String paramType = pStr.substring(0, pStr.indexOf(" "));
				if(PrimitiveUtils.isPrimitiveType(paramType)){
					String paramName = pStr.substring(pStr.indexOf(" ")+1, pStr.length());
					
					Param param = new Param(paramType, paramName);
					paramList.add(param);				
					
				}
				
			}
		}
		
		return paramList;
	}

	private void parseBreakpoints(VirtualMachine vm, ClassPrepareEvent classPrepEvent,
			Map<String, BreakPoint> locBrpMap) {
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

	private VarValue constructReferenceVarValue(ObjectReference objRef, Variable var0, ThreadReference thread,
			BreakPoint point, String accessType, int retrieveLayer) {
		Variable var = var0.clone();
		VarValue varValue = new ReferenceValue(false, objRef.uniqueID(), true, var);
		String order = this.trace.findDefiningNodeOrder(accessType, trace.getLatestNode(), 
				var.getVarID(), var.getAliasVarID());
		String varID = var.getVarID() + ":" + order;
		varValue.setVarID(varID);
		
		if(retrieveLayer==0){
			return varValue;
		}

		ClassType type = (ClassType) objRef.type();
		boolean needParseFields = HeuristicIgnoringFieldRule.isNeedParsingFields(type);
		if (needParseFields) {
			Map<Field, Value> map = objRef.getValues(type.allFields());
			List<Field> fieldList = new ArrayList<>(map.keySet());
			Collections.sort(fieldList, new Comparator<Field>() {
				@Override
				public int compare(Field o1, Field o2) {
					return o1.name().compareTo(o2.name());
				}
			});

			VariableValueExtractor extractor = new VariableValueExtractor(point, thread, null, this);
			for (Field field : fieldList) {
				if (type.isEnum()) {
					String childTypeName = field.typeName();
					if (childTypeName.equals(type.name())) {
						continue;
					}
				}

				boolean isIgnore = HeuristicIgnoringFieldRule.isForIgnore(type, field);
				if (!isIgnore) {
					FieldVar variable = new FieldVar(false, field.name(), field.typeName(), field.declaringType().signature());
					extractor.appendVarVal(varValue, variable, map.get(field), retrieveLayer, thread,
							false);
				}
			}
		}

		return varValue;
	}

	private VarValue constructArrayVarValue(ArrayReference arrayValue, Variable var0, ThreadReference thread,
			BreakPoint point, String accessType, int retrieveLayer) {
		Variable var = var0.clone();
		ArrayValue arrayVal = new ArrayValue(false, true, var);
		String componentType = ((ArrayType) arrayValue.type()).componentTypeName();
		arrayVal.setComponentType(componentType);
		arrayVal.setReferenceID(arrayValue.uniqueID());
		String order = this.trace.findDefiningNodeOrder(accessType, trace.getLatestNode(), 
				var.getVarID(), var.getAliasVarID());
		String varID = var.getVarID() + ":" + order;
		arrayVal.setVarID(varID);
		
		if(retrieveLayer==0){
			return arrayVal;
		}

		VariableValueExtractor extractor = new VariableValueExtractor(point, thread, null, this);
		// add value of elements
		List<Value> list = new ArrayList<>();
		if (arrayValue.length() > 0) {
			list = arrayValue.getValues(0, arrayValue.length());
		}
		for (int i = 0; i < arrayValue.length(); i++) {
			String parentSimpleID = Variable.truncateSimpleID(arrayVal.getVarID());
			String aliasVarID = Variable.concanateArrayElementVarID(parentSimpleID, String.valueOf(i));

			String varName = String.valueOf(i);
			Value elementValue = list.get(i);

			ArrayElementVar varElement = new ArrayElementVar(varName, componentType, aliasVarID);
			extractor.appendVarVal(arrayVal, varElement, elementValue, retrieveLayer, thread, false);
		}

		return arrayVal;
	}

	private VarValue generateVarValue(StackFrame frame, Variable var0, TraceNode node, String accessType,
			BreakPoint point) {
		VarValue varValue = null;
		/**
		 * Note that the read/written variables in breakpoint should be
		 * different when set them into different trace node.
		 */
		Variable var = var0.clone();
		String varName = var.getName();
		try {
			System.currentTimeMillis();
			ExpressionValue expValue = retriveExpression(frame, varName, node.getBreakPoint());
			if (expValue == null) {
				return null;
			}

			Value value = expValue.value;
			if (value instanceof ObjectReference) {
				ObjectReference objRef = (ObjectReference) value;
				String varID = String.valueOf(objRef.uniqueID());

//				String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType, node, 
//						varID, var.getAliasVarID());
//				varID = varID + ":" + definingNodeOrder;
				var.setVarID(varID);

				if (value.type().toString().equals("java.lang.String")) {
					String strValue = value.toString();
					strValue = strValue.substring(1, strValue.length() - 1);
					varValue = new StringValue(strValue, false, var);
				} else {
					if (objRef instanceof ArrayReference) {
						ArrayReference arrayValue = (ArrayReference) objRef;
						varValue = constructArrayVarValue(arrayValue, var, frame.thread(), point, accessType, Settings.getVariableLayer());
					} else {
						varValue = constructReferenceVarValue(objRef, var, frame.thread(), point, accessType, Settings.getVariableLayer());
					}

					StringBuffer buffer = new StringBuffer();
					buffer.append("[");
					for (VarValue child : varValue.getChildren()) {
						buffer.append(child.getVarName() + "=" + child.getStringValue());
						buffer.append(",");
					}
					buffer.append("]");
					varValue.setStringValue(buffer.toString());
				}
			}
			/**
			 * its a primitive type
			 */
			else {
				/**
				 * see whether its a local variable
				 */
				if (var instanceof LocalVar) {
					LocalVariableScope scope = this.trace.getLocalVariableScopes().findScope(var.getName(),
							node.getBreakPoint().getLineNumber(),
							node.getBreakPoint().getDeclaringCompilationUnitName());

					String varID;
					if (scope != null) {
						varID = Variable.concanateLocalVarID(node.getBreakPoint().getDeclaringCompilationUnitName(),
								var.getName(), scope.getStartLine(), scope.getEndLine());
						String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType, node, 
								varID, var.getAliasVarID());
						varID = varID + ":" + definingNodeOrder;
					}
					/**
					 * it means that an implicit "this" variable is visited.
					 */
					else if (var.getName().equals("this")) {
						varID = String.valueOf(frame.thisObject().uniqueID());
					} else {
						return null;
					}
					var.setVarID(varID);
				}
				/**
				 * It's a field or array element.
				 */
				else {
					Value parentValue = expValue.parentValue;
					ObjectReference objRef = (ObjectReference) parentValue;

					if (objRef == null) {
						objRef = null;
						try {
							objRef = frame.thisObject();
						} catch (Exception e) {
						}
						if (objRef == null) {
							return null;
						}
					}

					if (var instanceof FieldVar) {
						String varID = null;
						if (((FieldVar) var).isStatic()) {
							varID = var.getName();
						} else {
							varID = Variable.concanateFieldVarID(String.valueOf(objRef.uniqueID()),
									var.getSimpleName());
						}
						String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType, node, 
								varID, var.getAliasVarID());
						varID = varID + ":" + definingNodeOrder;
						var.setVarID(varID);
					} else if (var instanceof ArrayElementVar) {
						String index = var.getSimpleName();
						ExpressionValue indexValue = retriveExpression(frame, index, node.getBreakPoint());
						String indexValueString = null;
						if(indexValue.value!=null){
							indexValueString = indexValue.value.toString();
						}
						else{
							indexValueString = "unknown";
						}
						String varID = Variable.concanateArrayElementVarID(String.valueOf(objRef.uniqueID()),
								indexValueString);
						String definingNodeOrder = this.trace.findDefiningNodeOrder(accessType, node, 
								varID, var.getAliasVarID());
						varID = varID + ":" + definingNodeOrder;
						var.setVarID(varID);
					}
				}

				String content = (value == null) ? null : value.toString();
				varValue = new PrimitiveValue(content, false, var);
			}
			return varValue;
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
		} catch (Exception e) {
			System.out.print(location);
			e.printStackTrace();
		}

		return frame;
	}

	private void processReadVariable(TraceNode node, ThreadReference thread, Location location) {
		Map<String, StepVariableRelationEntry> stepVariableTable = trace.getStepVariableTable();
		BreakPoint point = node.getBreakPoint();

		StackFrame frame = findFrame(thread, location);
		if (frame == null) {
			return;
		}

		synchronized (frame) {
			List<Variable> readVariables = node.getBreakPoint().getReadVariables();
			for (Variable readVar : readVariables) {
				VarValue varValue = generateVarValue(frame, readVar, node, Variable.READ, point);
				if (varValue != null) {
					node.addReadVariable(varValue);

					List<StepVariableRelationEntry> entries = constructStepVariableEntry(stepVariableTable, varValue);
					for(StepVariableRelationEntry entry: entries){
						entry.addConsumer(node);
					}
				}
			}
		}
	}
	
	private void processReturnVariable(TraceNode node, ThreadReference thread, Location location) {
		if(!node.isReturnNode()){
			return;
		}
		
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(node.getDeclaringCompilationUnitName(), appPath);
		ReturnStatementFinder finder = new ReturnStatementFinder(cu, node.getLineNumber());
		cu.accept(finder);
		ReturnStatement rStat = finder.returnStatement;
		if(rStat!=null){
			Expression expr = rStat.getExpression();
			String vID = VirtualVar.VIRTUAL_PREFIX + node.getOrder();
			if(expr != null){
				String exprString = expr.toString();
				String varName = node.getMethodName();
				if(exprString.contains("(") || exprString.contains("[")){
					int count = 0;
					for(VarValue readVar: node.getReadVariables()){
						VirtualVar virVar = new VirtualVar(varName+(count++), "return type");
						
						Long uniqueID = -1l;
						if(readVar instanceof ReferenceValue) {
							uniqueID = ((ReferenceValue)readVar).getUniqueID();
						}
						
						VirtualValue virValue = new VirtualValue(false, virVar, uniqueID);
						virValue.setVarID(vID);
						virValue.setStringValue(readVar.getStringValue());
						node.addReturnVariable(virValue);
					}
				}
				else{
					if(expr instanceof NullLiteral || expr instanceof NullLiteral 
							|| expr instanceof StringLiteral || expr instanceof TypeLiteral 
							|| expr instanceof BooleanLiteral || expr instanceof CharacterLiteral) {
						Variable vVar = new VirtualVar(varName, "return type");
						VirtualValue virValue = new VirtualValue(false, vVar, -1l);
						virValue.setVarID(vID);
						virValue.setStringValue(exprString);
						node.addReturnVariable(virValue);
					}
					else {
						StackFrame frame = findFrame(thread, location);
						if (frame == null) {
							return;
						}
						
						synchronized (frame) {
							Variable vVar = new VirtualVar(exprString, "return type");
							VarValue valueV = generateVarValue(frame, vVar, node, Variable.WRITTEN, node.getBreakPoint());
							vVar.setName(varName);
							if(valueV != null){
								valueV.setVarID(vID);
								node.addReturnVariable(valueV);
							}
						}
					}
				}
			}
			
		}
		
		
	}

	private List<StepVariableRelationEntry> constructStepVariableEntry(Map<String, StepVariableRelationEntry> stepVariableTable,
			VarValue var) {
		List<StepVariableRelationEntry> list = new ArrayList<>();
		StepVariableRelationEntry entry = constructStepVariableEntry(stepVariableTable, var.getVarID(), var.getVariable());
		list.add(entry);
		
		if(var.getAliasVarID()!=null){
			StepVariableRelationEntry entry0 = constructStepVariableEntry(stepVariableTable, var.getAliasVarID(), var.getVariable());
			list.add(entry0);
		}
		
		return list;
	}
	
	private StepVariableRelationEntry constructStepVariableEntry(Map<String, StepVariableRelationEntry> stepVariableTable,
			String varID, Variable var){
		StepVariableRelationEntry entry = stepVariableTable.get(varID);
		if (entry == null) {
			entry = new StepVariableRelationEntry(varID);
			stepVariableTable.put(varID, entry);
		}
		entry.addAliasVariable(var);
		return entry;
	}

	private void processWrittenVariable(TraceNode node, Map<String, StepVariableRelationEntry> stepVariableTable,
			ThreadReference thread, Location location) {
		BreakPoint point = node.getBreakPoint();

		StackFrame frame = findFrame(thread, location);
		if (frame == null) {
			System.err.println("get a null frame from thread!");
			return;
		}
		synchronized (frame) {
			List<Variable> writtenVariables = node.getBreakPoint().getWrittenVariables();
			for (Variable writtenVar : writtenVariables) {
				VarValue varValue = generateVarValue(frame, writtenVar, node, Variable.WRITTEN, point);

				if (varValue != null) {
					
					List<VarValue> list = new ArrayList<>();
					list.add(varValue);
					List<VarValue> children = varValue.getAllDescedentChildren();
					list.addAll(children);
					
					for(VarValue v: list){
						node.addWrittenVariable(v);
						List<StepVariableRelationEntry> entries = constructStepVariableEntry(stepVariableTable, v);
						for(StepVariableRelationEntry entry: entries){
							entry.addProducer(node);
						}
					}
				}
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

		try {
			ExpressionParser.clear();

			CompilationUnit cu;
			if (point == null) {
				cu = null;
			} else {
				cu = JavaUtil.findCompilationUnitInProject(point.getDeclaringCompilationUnitName(), appPath);
			}

			int lineNumber = -1;
			if (point != null) {
				lineNumber = point.getLineNumber();
			}

			ExpressionParser.setParameters(cu, lineNumber);

			Value val = null;
			if (expression.contains("(")) {
				if (expression.contains("[")) {
					val = null;
				} else {
					val = retrieveComplicatedExpressionValue(expression, frame.virtualMachine(), frameGetter);
				}
			} else {
				val = ExpressionParser.evaluate(expression, frame.virtualMachine(), frameGetter);
			}

			eValue = new ExpressionValue(val, ExpressionParser.parentValue, null);

		} catch (ParseException e) {
//			 e.printStackTrace();
		} catch (InvocationException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		} catch (ClassNotLoadedException e) {
			e.printStackTrace();
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
//			e.printStackTrace();
		} finally {
		}
		System.currentTimeMillis();
		
		return eValue;
	}

	private void collectValueOfPreviousStep(BreakPoint lastSteppingInPoint, ThreadReference thread, Location loc)
			throws SavException {

		BreakPoint current = new BreakPoint(lastSteppingInPoint.getClassCanonicalName(),
				lastSteppingInPoint.getDeclaringCompilationUnitName(), lastSteppingInPoint.getLineNumber());
		current.setReadVariables(lastSteppingInPoint.getReadVariables());
		current.setWrittenVariables(lastSteppingInPoint.getWrittenVariables());

		BreakPointValue bkpVal = extractValuesAtLocation(current, thread, loc);

		int len = trace.getExecutionList().size();
		TraceNode node = trace.getExecutionList().get(len - 1);
		node.setAfterStepInState(bkpVal);

	}

	private TraceNode recordTrace(BreakPoint bkp, BreakPointValue bkpVal) {
		int order = trace.size() + 1;
		TraceNode node = new TraceNode(bkp, bkpVal, order, trace);

		TraceNode stepInPrevious = null;
		if (order >= 2) {
			stepInPrevious = trace.getExecutionList().get(order - 2);
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
				VariableValueExtractor extractor = new VariableValueExtractor(bkp, thread, loc, this);
				BreakPointValue bpValue = extractor.extractValue(Variable.READ);
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
		return trace;
	}

	public AppJavaClassPath getConfig() {
		return appPath;
	}

	public void setConfig(AppJavaClassPath config) {
		this.appPath = config;
	}

	public StepRequest getStepRequest(ThreadReference thread) {
		for (StepRequest stepRequest : this.stepRequestList) {
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
