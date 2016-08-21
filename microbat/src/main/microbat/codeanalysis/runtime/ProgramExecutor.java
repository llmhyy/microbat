package microbat.codeanalysis.runtime;

import static sav.strategies.junit.SavJunitRunner.ENTER_TC_BKP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.internal.VoidValueImpl;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
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
public class ProgramExecutor extends Executor {
	public static final long DEFAULT_TIMEOUT = -1;

	/**
	 * fundamental fields for debugging
	 */
	/**
	 * the class patterns indicating the classes into which I will not step to
	 * get the runtime values
	 */
	private AppJavaClassPath config;
	
	/** maps from a given class name to its contained breakpoints */
	private Map<String, List<BreakPoint>> brkpsMap;

	/**
	 * for recording execution trace
	 */
	private Trace trace = new Trace();

	public ProgramExecutor() {}

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
	 * @param runningStatements
	 * @throws SavException
	 */
	public void run(List<BreakPoint> runningStatements, IProgressMonitor monitor) throws SavException, TimeoutException {

		this.brkpsMap = BreakpointUtils.initBrkpsMap(runningStatements);

		/** start debugger */
		VirtualMachine vm = new VMStarter(this.config).start();

		try{
			constructTrace(monitor, vm);			
		}
		finally{
			if(vm != null){
				vm.exit(0);
			}
			System.out.println("JVM is ended.");
		}

	}

	private void constructTrace(IProgressMonitor monitor, VirtualMachine vm) throws SavException, TimeoutException {
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
		 * Yun Lin: <br>
		 * This variable <code>isLastStepEventRecordNode</code> is used to check
		 * whether a step performs a method invocation. Based on the
		 * *observation*, a method entry event happens directly after a step
		 * event if this step invokes a method. Therefore, if a step event
		 * contains the statements we need, meanwhile, the next received event
		 * is a method entry event, then, I will consider the corresponding step
		 * invokes a method.
		 * 
		 * In the implementation, the variable
		 * <code>isLastStepEventRecordNode</code> is to indicate a method entry
		 * that an interesting step event just happened right before. Thus, the
		 * last recorded trace node should be method invocation.
		 */
		boolean isLastStepEventRecordNode = false;

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
		Value lastestReturnedValue = null;

		/** this variable is used to handle exception case. */
		Location caughtLocationForJustException = null;

		cancel: 
		while (!stop && !eventTimeout) {
			EventSet eventSet;
			try {
				eventSet = eventQueue.remove(10000);
			} catch (InterruptedException e) {
				break;
			}
			if (eventSet == null) {
				System.out.println("Time out! Cannot get event set!");
				eventTimeout = true;
				break;
			}

			if (trace.getLastestNode() != null) {
				if (trace.getLastestNode().getOrder() == 96) {
					System.currentTimeMillis();
				}
			}

			for (Event event : eventSet) {
				
				if (event instanceof VMStartEvent) {
					System.out.println("JVM is started...");

					addStepWatch(erm, event);
					addMethodWatch(erm);
					addExceptionWatch(erm);
				}
				if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
					stop = true;
					break;
				} 
				else if (event instanceof ClassPrepareEvent) {
					parseBreakpoints(vm, (ClassPrepareEvent) event, locBrpMap);
				} else if (event instanceof StepEvent) {
					ThreadReference thread = ((StepEvent) event).thread();
					Location currentLocation = ((StepEvent) event).location();

					/**
					 * collect the variable values after executing previous step
					 */
					boolean isContextChange = false;
					if (lastSteppingInPoint != null) {
						collectValueOfPreviousStep(lastSteppingInPoint, thread, currentLocation);

						/**
						 * Parsing the written variables of last step.
						 * 
						 * If the context changes, the value of some variables
						 * may not be retrieved. Thus, the variable ID cannot be
						 * generated. Note that the ID of a variable need
						 * parsing its heap ID which can only be accessed by
						 * runtime.
						 */
						isContextChange = checkContext(lastSteppingInPoint, currentLocation);
						if (!isContextChange) {
							parseReadWrittenVariableInThisStep(thread, currentLocation,
									this.trace.getLastestNode(), this.trace.getStepVariableTable(), Variable.WRITTEN);
						}

						lastSteppingInPoint = null;
					}

					BreakPoint bkp = locBrpMap.get(currentLocation.toString());
					/**
					 * This step is an interesting step (sliced statement) in
					 * our debugging process
					 */
					if (bkp != null) {
						BreakPointValue bkpVal;
						if(this.trace.getLastestNode() != null && !isContextChange){
							bkpVal = this.trace.getLastestNode().getAfterStepInState();
						}
						else{
							bkpVal = extractValuesAtLocation(bkp, thread, currentLocation);
						}
						
						TraceNode node = recordTrace(bkp, bkpVal);
						
						/**
						 * pop up method after an exception is caught.
						 */
						if (caughtLocationForJustException != null) {
							if (!methodNodeStack.isEmpty()) {
								TraceNode invocationNode = this.trace.findLastestExceptionNode();
								boolean isInvocationEnvironmentContainingLocation = isInvocationEnvironmentContainingLocation(
										invocationNode, caughtLocationForJustException);
								while (!isInvocationEnvironmentContainingLocation) {
									if (!methodNodeStack.isEmpty()) {
										invocationNode = methodNodeStack.pop();
										methodNodeJustPopedOut = invocationNode;
										// methodStack.pop();
										methodSignatureStack.pop();

										isInvocationEnvironmentContainingLocation = isInvocationEnvironmentContainingLocation(
												invocationNode, caughtLocationForJustException);
									} else {
										break;
									}
								}
							}
							caughtLocationForJustException = null;
						}

						handleCompensationForMethodEntryExistOptimization(methodNodeStack, methodSignatureStack, event,
								isContextChange, node);

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
						Value returnedValue = null;
						if (node != null && methodNodeJustPopedOut != null) {
							methodNodeJustPopedOut.setStepOverNext(node);
							methodNodeJustPopedOut.setAfterStepOverState(node.getProgramState());

							node.setStepOverPrevious(methodNodeJustPopedOut);

							methodNodeJustPopedOut = null;
							returnedValue = lastestReturnedValue;
						}

						if (currentLocation.lineNumber()==85) {
							System.currentTimeMillis();
						}
						parseReadWrittenVariableInThisStep(thread, currentLocation, node,
								this.trace.getStepVariableTable(), Variable.READ);

						/**
						 * create virtual variable for return statement
						 */
						if (this.trace.size() > 1) {
							TraceNode lastestNode = this.trace.getExectionList().get(this.trace.size() - 2);
							if (lastestNode.getBreakPoint().isReturnStatement()) {
								createVirutalVariableForReturnStatement(thread, node,
										lastestNode, returnedValue);
							}
						}

						lastSteppingInPoint = bkp;
						isLastStepEventRecordNode = true;
					} else {
						isLastStepEventRecordNode = false;
					}

					getMethodEntryRequest().setEnabled(true);
					getMethodExitRequset().setEnabled(true);

					monitor.worked(1);
					if (monitor.isCanceled() || this.trace.getExectionList().size() >= Settings.stepLimit) {
						stop = true;
						break cancel;
					}
				} else if (event instanceof MethodEntryEvent) {
					MethodEntryEvent mee = (MethodEntryEvent) event;
					Method method = mee.method();
					// System.out.println(method + ":" + ((MethodEntryEvent)
					// event).location());
					
					Location location = ((MethodEntryEvent) event).location();
					boolean isLocationInRunningStatement = isLocationInRunningStatement(location, locBrpMap);
					if (isLocationInRunningStatement) {

						if (isLastStepEventRecordNode) {
							TraceNode lastestNode = this.trace.getLastestNode();

							try {
								if (!method.arguments().isEmpty()) {
									StackFrame frame = findFrame(((MethodEntryEvent) event).thread(), mee.location());
									String typeSig = method.declaringType().signature();
									String declaringType = SignatureUtils.signatureToName(typeSig);

									int methodLocationLine = method.location().lineNumber();
									List<Param> paramList = parseParamList(method);

									parseWrittenParameterVariableForMethodInvocation(frame, declaringType,
											methodLocationLine, paramList, lastestNode);
								}
							} catch (AbsentInformationException e) {
								e.printStackTrace();
							}

							methodNodeStack.push(lastestNode);
							String methodSignature = createSignature(method);
							methodSignatureStack.push(methodSignature);
						}
					}
					/**
					 * if not, just shut down the method event listening for
					 * saving time.
					 */
					else {
						getMethodEntryRequest().setEnabled(false);
						getMethodExitRequset().setEnabled(false);
					}

				} else if (event instanceof MethodExitEvent) {
					MethodExitEvent mee = (MethodExitEvent) event;
					Method method = mee.method();
					// System.out.println(method + ":" + ((MethodExitEvent)
					// event).location());

					Location location = ((MethodExitEvent) event).location();
					boolean isLocationInRunningStatement = isLocationInRunningStatement(location, locBrpMap);

					if (isLocationInRunningStatement) {

						if (!methodSignatureStack.isEmpty()) {
							String peekSig = methodSignatureStack.peek();
							String thisSig = createSignature(method);
							if (JavaUtil.isCompatibleMethodSignature(peekSig, thisSig)) {
								TraceNode node = methodNodeStack.pop();
								methodNodeJustPopedOut = node;
								methodSignatureStack.pop();
								lastestReturnedValue = mee.returnValue();
							}
						}
					} else {
						getMethodEntryRequest().setEnabled(false);
						getMethodExitRequset().setEnabled(false);
					}

				} else if (event instanceof ExceptionEvent) {
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
	}

	/**
	 * The following code is used for side-effect of optimization, compensating
	 * the missing method invocation because of disabling some method entry/exit
	 * event in some cases.
	 */
	private void handleCompensationForMethodEntryExistOptimization(Stack<TraceNode> methodNodeStack,
			Stack<String> methodSignatureStack, Event event, boolean isContextChange, TraceNode node) {
		if (isContextChange) {
			TraceNode prevNode = node.getStepInPrevious();
			if (prevNode != null) {
				MethodDeclaration invokedMethod = JavaUtil.checkInvocationParentRelation(prevNode, node);
				if (invokedMethod != null) {
					IMethodBinding invokedMethodBinding = invokedMethod.resolveBinding();
					String complexInvokedMethodSig = JavaUtil.convertFullSignature(invokedMethodBinding);

					String invokedMethodSig = trimGenericType(complexInvokedMethodSig);

					StackFrame frame = findFrame(((StepEvent) event).thread(), ((StepEvent) event).location());
					String declaringType = invokedMethodBinding.getDeclaringClass().getBinaryName();
					int methodLine = ((StepEvent) event).location().lineNumber();
					List<Param> paramList = findParamList(invokedMethod);

					if (!methodSignatureStack.isEmpty()) {
						String peekMethodSig = methodSignatureStack.peek();
						if (!peekMethodSig.equals(invokedMethodSig)) {
							System.out.println("compensating side effect of optimization for " + prevNode);

							if (invokedMethodBinding.getParameterTypes().length != 0) {
								parseWrittenParameterVariableForMethodInvocation(frame, declaringType, methodLine,
										paramList, prevNode);
							}

							methodSignatureStack.push(invokedMethodSig);
							methodNodeStack.push(prevNode);
						}
					} else {
						System.out.println("compensating side effect of optimization for " + prevNode);
						if (invokedMethodBinding.getParameterTypes().length != 0) {
							parseWrittenParameterVariableForMethodInvocation(frame, declaringType, methodLine,
									paramList, node);
						}

						methodSignatureStack.push(invokedMethodSig);
						methodNodeStack.push(prevNode);
					}
				}
			}
		}
	}

	private String trimGenericType(String complexInvokedMethodSig) {
		String simpleSig = complexInvokedMethodSig.replaceAll("<[^<|^>]*>", "");
		return simpleSig;
	}

	private List<Param> findParamList(MethodDeclaration invokedMethod) {
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
			}

			return true;
		}
	}

	private boolean isInvocationEnvironmentContainingLocation(TraceNode methodNode,
			Location caughtLocationForJustException) {
		String qualifiedName = methodNode.getBreakPoint().getDeclaringCompilationUnitName();
		int lineNumber = methodNode.getLineNumber();

		try {
			String path = caughtLocationForJustException.sourcePath();
			path = path.substring(0, path.indexOf(".java"));
			path = path.replace("\\", ".");

			if (qualifiedName.equals(path)) {
				CompilationUnit cu = JavaUtil.findCompilationUnitInProject(qualifiedName);
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

	private ExceptionRequest exceptionRequest;
	private void addExceptionWatch(EventRequestManager erm) {

		setExceptionRequest(erm.createExceptionRequest(null, true, true));
		getExceptionRequest().setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		for (String ex : stepWatchExcludes) {
			getExceptionRequest().addClassExclusionFilter(ex);
		}
		// request.addClassFilter("java.io.FileNotFoundException");
		getExceptionRequest().enable();
	}

	private StepRequest stepRequest;
	private void addStepWatch(EventRequestManager erm, Event event) {
		stepRequest = erm.createStepRequest(((VMStartEvent) event).thread(), StepRequest.STEP_LINE,
				StepRequest.STEP_INTO);
		stepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		for (String ex : stepWatchExcludes) {
			stepRequest.addClassExclusionFilter(ex);
		}
		stepRequest.enable();
	}

	/**
	 * when the last interesting stepping statement is a return statement,
	 * create a virtual variable.
	 */
	private void createVirutalVariableForReturnStatement(ThreadReference thread, TraceNode node, TraceNode lastestNode,
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

		String name = VirtualVar.VIRTUAL_PREFIX + lastestNode.getOrder();
		VirtualVar var = new VirtualVar(name, returnedType);
		var.setVarID(name);

		Map<String, StepVariableRelationEntry> map = this.trace.getStepVariableTable();
		StepVariableRelationEntry entry = new StepVariableRelationEntry(var.getVarID());
		entry.addAliasVariable(var);
		entry.addProducer(lastestNode);
		entry.addConsumer(node);

		VarValue varValue = new VirtualValue(false, var);
		// String stringValue = "(return from " +
		// lastestNode.getBreakPoint().getMethodName() + "(...))";
		varValue.setStringValue(returnedStringValue);

		lastestNode.addWrittenVariable(varValue);
		node.addReadVariable(varValue);

		map.put(var.getVarID(), entry);
	}

	/**
	 * build the written relations between method invocation
	 */
	private void parseWrittenParameterVariableForMethodInvocation(StackFrame frame, String methodDeclaringType,
			int methodLocationLine, List<Param> paramList, TraceNode lastestNode) {

		for (Param param : paramList) {

			if (frame == null) {
				return;
			}

			Value value = JavaUtil.retriveExpression(frame, param.getName());

			if (!(value instanceof ObjectReference) || value == null) {

				LocalVar localVar = new LocalVar(param.getName(), param.getType(),
						lastestNode.getDeclaringCompilationUnitName(), lastestNode.getLineNumber());

				VariableScopeParser parser = new VariableScopeParser();
				LocalVariableScope scope = parser.parseMethodScope(methodDeclaringType, methodLocationLine,
						localVar.getName());
				String varID;
				if (scope != null) {
					varID = Variable.concanateLocalVarID(methodDeclaringType, localVar.getName(), scope.getStartLine(),
							scope.getEndLine());
					String definingNodeOrder = this.trace.findDefiningNodeOrder(Variable.WRITTEN, lastestNode, varID);
					varID = varID + ":" + definingNodeOrder;
					localVar.setVarID(varID);
				} else {
					System.err.println("cannot find the method when parsing parameter scope");
				}

				if (localVar.getVarID().contains("158")) {
					System.currentTimeMillis();
				}

				StepVariableRelationEntry entry = this.trace.getStepVariableTable().get(localVar.getVarID());
				if (entry == null) {
					entry = new StepVariableRelationEntry(localVar.getVarID());
					this.trace.getStepVariableTable().put(localVar.getVarID(), entry);
				}
				entry.addAliasVariable(localVar);
				entry.addProducer(lastestNode);

				VarValue varValue;
				if (PrimitiveUtils.isPrimitiveType(param.getType())) {
					varValue = new PrimitiveValue(value.toString(), false, localVar);
				} else {
					varValue = new ReferenceValue(true, false, localVar);
				}

				lastestNode.addWrittenVariable(varValue);
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

	private MethodEntryRequest methodEntryRequest;
	private MethodExitRequest methodExitRequset;

	/**
	 * add method enter and exit event
	 */
	private void addMethodWatch(EventRequestManager erm) {
		setMethodEntryRequest(erm.createMethodEntryRequest());
		for (String classPattern : stepWatchExcludes) {
			getMethodEntryRequest().addClassExclusionFilter(classPattern);
		}
		getMethodEntryRequest().setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		getMethodEntryRequest().enable();

		setMethodExitRequset(erm.createMethodExitRequest());
		for (String classPattern : stepWatchExcludes) {
			getMethodExitRequset().addClassExclusionFilter(classPattern);
		}
		getMethodExitRequset().setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		getMethodExitRequset().enable();
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

	private ClassPrepareRequest classPrepareRequest;
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

	/**
	 * add junit relevant classes into VM configuration, i.e., launch the
	 * program with JUnit Launcher.
	 * 
	 * @throws SavException
	 */
	// private final void setDebuggingConfiguration() throws SavException {

	// getVmConfig().setLaunchClass(JUNIT_RUNNER_CLASS_NAME);
	// JunitRunnerProgramArgBuilder builder = new
	// JunitRunnerProgramArgBuilder();
	// List<String> args =
	// builder.methods(allTests)
	// .testcaseTimeout(getTimeoutInSec(), TimeUnit.SECONDS)
	// .build();
	// getVmConfig().setProgramArgs(args);
	// getVmConfig().resetPort();
	// }

	// private long getTimeoutInSec() {
	// return timeout;
	// }

	// private TraceNode handleBreakpoint(BreakPoint bkp, ThreadReference
	// thread, Location loc) throws SavException {
	// BreakPointValue bkpVal = extractValuesAtLocation(bkp, thread, loc);
	// TraceNode node = recordTrace(bkp, bkpVal);
	//
	// if(!this.methodNodeStack.isEmpty()){
	// TraceNode parentInvocationNode = this.methodNodeStack.peek();
	// parentInvocationNode.addInvocationChild(node);
	// node.setInvocationParent(parentInvocationNode);
	// }
	//
	// return node;
	// }

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
							varID = Variable.concanateLocalVarID(node.getBreakPoint().getClassCanonicalName(),
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

		boolean classPrepare = getClassPrepareRequest().isEnabled();
		boolean step = getStepRequest().isEnabled();
		boolean methodEntry = getMethodEntryRequest().isEnabled();
		boolean methodExit = getMethodExitRequset().isEnabled();
		boolean exception = getExceptionRequest().isEnabled();
		
		getClassPrepareRequest().disable();
		getStepRequest().disable();
		getMethodEntryRequest().disable();
		getMethodExitRequset().disable();
		getExceptionRequest().disable();
		
		try {
			ExpressionParser.clear();

			CompilationUnit cu = JavaUtil.findCompilationUnitInProject(point.getDeclaringCompilationUnitName());
			ExpressionParser.setParameters(cu, point.getLineNumber());
			
			Value val = ExpressionParser.evaluate(expression, frame.virtualMachine(), frameGetter);
			
			eValue = new ExpressionValue(val, ExpressionParser.parentValue, null);

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
		} finally{
			getClassPrepareRequest().setEnabled(classPrepare);
			getStepRequest().setEnabled(step);
			getMethodEntryRequest().setEnabled(methodEntry);
			getMethodExitRequset().setEnabled(methodExit);
			getExceptionRequest().setEnabled(exception);
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
				VariableValueExtractor extractor = new VariableValueExtractor(bkp, thread, loc, this);
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
		return config;
	}

	public void setConfig(AppJavaClassPath config) {
		this.config = config;
	}

	public StepRequest getStepRequest() {
		return stepRequest;
	}

	public void setStepRequest(StepRequest stepRequest) {
		this.stepRequest = stepRequest;
	}

	public MethodEntryRequest getMethodEntryRequest() {
		return methodEntryRequest;
	}

	public void setMethodEntryRequest(MethodEntryRequest methodEntryRequest) {
		this.methodEntryRequest = methodEntryRequest;
	}

	public MethodExitRequest getMethodExitRequset() {
		return methodExitRequset;
	}

	public void setMethodExitRequset(MethodExitRequest methodExitRequset) {
		this.methodExitRequset = methodExitRequset;
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
