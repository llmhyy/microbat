package microbat.codeanalysis.runtime.variable;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

import microbat.codeanalysis.ast.LocalVariableScope;
import microbat.codeanalysis.runtime.ProgramExecutor;
import microbat.codeanalysis.runtime.herustic.HeuristicIgnoringFieldRule;
import microbat.model.BreakPoint;
import microbat.model.BreakPointValue;
import microbat.model.trace.Trace;
import microbat.model.value.ArrayValue;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.StringValue;
import microbat.model.value.VarValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.util.JavaUtil;
import microbat.util.PrimitiveUtils;
import microbat.util.Settings;
@SuppressWarnings("restriction")
public class VariableValueExtractor {
//	protected static Logger log = LoggerFactory.getLogger(DebugValueExtractor.class);
	private static final Pattern OBJECT_ACCESS_PATTERN = Pattern.compile("^\\.([^.\\[]+)(\\..+)*(\\[.+)*$");
	private static final Pattern ARRAY_ACCESS_PATTERN = Pattern.compile("^\\[(\\d+)\\](.*)$");

	/**
	 * In order to handle the graph structure of objects, this map is used to remember which object has been analyzed
	 * to construct a graph of objects.
	 */
	private Map<Long, ReferenceValue> objectPool = new HashMap<>();
	
	
	private BreakPoint bkp;
	private ThreadReference thread;
	private Location loc;
	private ProgramExecutor executor;
	
	public VariableValueExtractor(BreakPoint bkp, ThreadReference thread,
			Location loc, ProgramExecutor executor) {
		this.bkp = bkp;
		this.thread = thread;
		this.loc = loc;
		this.setExecutor(executor);
	}

	public final BreakPointValue extractValue(String accessType)
			throws IncompatibleThreadStateException, AbsentInformationException {
		if (bkp == null) {
			return null;
		}
		
		BreakPointValue bkVal = new BreakPointValue(bkp.getId());
		synchronized (thread) {
			List<StackFrame> frames = this.thread.frames();
			if (!frames.isEmpty()) {
				final StackFrame frame = findFrameByLocation(frames, loc);
				Method method = frame.location().method();
				ReferenceType refType;
				ObjectReference objRef = null;
				if (method.isStatic()) {
					refType = method.declaringType();
				} else {
					objRef = frame.thisObject();
					refType = objRef.referenceType();
				}
				
				/**
				 * Local variables MUST BE navigated before fields, because: in
				 * case a class field and a local variable in method have the
				 * same name, and the breakpoint variable with that name has the
				 * scope UNDEFINED, it must be the variable in the method.
				 */
				final Map<Variable, JDIParam> allVariables = new HashMap<Variable, JDIParam>();
				final List<LocalVariable> visibleVars = frame.visibleVariables();
				final List<Field> allFields = refType.allFields();
				
				List<Variable> allVisibleVariables = collectAllVariable(visibleVars, allFields);
				bkp.setAllVisibleVariables(allVisibleVariables);
				
				for (Variable bpVar : bkp.getReadVariables()) {
				//for (Variable bpVar : bkp.getAllVisibleVariables()) {
					// First check local variable
					LocalVariable matchedLocalVariable = findMatchedLocalVariable(bpVar, visibleVars);
					
					JDIParam param = null;
					if (matchedLocalVariable != null) {
						try {
							param = recursiveMatch(frame, matchedLocalVariable, bpVar.getName());							
						} catch (Exception e) {
							e.printStackTrace();
						}
					} 
					else {
						// Then check class fields (static & non static)
						Field matchedField = findMatchedField(bpVar, allFields);

						if (matchedField != null) {
							if (matchedField.isStatic()) {
								param = JDIParam.staticField(matchedField, refType, refType.getValue(matchedField));
							} else {
								Value value = objRef == null ? null : objRef.getValue(matchedField);
								param = JDIParam.nonStaticField(matchedField, objRef, value);
							}
							
							if (param.getValue() != null && !matchedField.name().equals(bpVar.getName())) {
								param = recursiveMatch(param, extractSubProperty(bpVar.getName()));
							}
						}
					}
					if (param != null) {
						allVariables.put(bpVar, param);
					}
				}

				if (!allVariables.isEmpty()) {
					collectValue(bkVal, objRef, thread, allVariables, accessType);
				}
			}
		}
		return bkVal;
	}
	
	private List<Variable> collectAllVariable(List<LocalVariable> visibleVars, List<Field> allFields) {
		List<Variable> varList = new ArrayList<>();
		for(LocalVariable lv: visibleVars){
//			Var var = new Var(lv.name(), lv.name(), VarScope.UNDEFINED);
			LocalVar var = new LocalVar(lv.name(), lv.typeName(), 
					bkp.getDeclaringCompilationUnitName(), bkp.getLineNumber());
			varList.add(var);
		}
		for(Field field: allFields){
			FieldVar var = new FieldVar(field.isStatic(), field.name(), field.typeName(), field.declaringType().signature());
			var.setDeclaringType(field.declaringType().name());
			varList.add(var);
		}
		return varList;
	}

	private LocalVariable findMatchedLocalVariable(Variable bpVar, List<LocalVariable> visibleVars){
		LocalVariable match = null;
		if (bpVar instanceof LocalVar) {
			for (LocalVariable localVar : visibleVars) {
				if (localVar.name().equals(bpVar.getName())) {
					match = localVar;
					break;
				}
			}
		}
		
		return match;
	}
	
	private Field findMatchedField(Variable bpVar, List<Field> allFields){
		Field matchedField = null;
		for (Field field : allFields) {
			if (field.name().equals(bpVar.getName())) {
				matchedField = field;
				break;
			}
		}
		
		return matchedField;
	}

	protected void collectValue(BreakPointValue bkVal, ObjectReference objRef, ThreadReference thread,
			Map<Variable, JDIParam> allVariables, String accessType){
		
		int level = Settings.getVariableLayer();
//		if(objRef != null){
//			LocalVar variable = new LocalVar("this", objRef.type().toString(), 
//					bkp.getDeclaringCompilationUnitName(), bkp.getLineNumber());
//			appendClassVarVal(bkVal, variable, objRef, level, thread, true);			
//		}
		
		List<Variable> vars = new ArrayList<>(allVariables.keySet());
		Collections.sort(vars, new Comparator<Variable>() {
			@Override
			public int compare(Variable o1, Variable o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		for (Variable var : vars) {
			JDIParam param = allVariables.get(var);
			Value value = param.getValue();
			//boolean isField = (param.getField() != null);
			
			//if(!isField){
				LocalVar variable = new LocalVar(var.getName(), var.getType(), 
						bkp.getDeclaringCompilationUnitName(), bkp.getLineNumber());
				System.currentTimeMillis();
				
				appendVarVal(bkVal, variable, value, level, thread, true);				
			//}
			
		}
		
//		System.currentTimeMillis();
	}
	
	protected String extractSubProperty(final String fullName) {
		// obj idx
		int idx = fullName.indexOf(".");
		int arrIndex = fullName.indexOf("[");
		if ((idx < 0) || (arrIndex >= 0 && arrIndex < idx)) {
			idx = arrIndex;
		}  
		if (idx >= 0) {
			return fullName.substring(idx);
		}
		return fullName;
	}
	
	protected JDIParam recursiveMatch(final StackFrame frame, final LocalVariable match, final String fullName) {
		Value value = frame.getValue(match);
		JDIParam param = JDIParam.localVariable(match, value);
		if (!match.name().equals(fullName)) {
			return recursiveMatch(param , extractSubProperty(fullName));
		}
		return param;
	}
	
	protected JDIParam recursiveMatch(JDIParam param, final String property) {
		if (property==null || property.isEmpty()) {
			return param;
		}
		Value value = param.getValue();
		if (value == null) {
			// cannot get property for a null object
			return null;
		}
		JDIParam subParam = null;
		String subProperty = null;
		/** 
		 * 	NOTE: must check Array before Object because ArrayReferenceImpl
		 *	implements both ArrayReference and ObjectReference (by extending
		 *	ObjectReferenceImpl)
		 * 
		 */
		if (ArrayReference.class.isAssignableFrom(value.getClass())) {
			ArrayReference array = (ArrayReference) value;
			// Can access to the array's length or values
			if (".length".equals(property)) {
				subParam = JDIParam.nonStaticField(null, array, array.virtualMachine().mirrorOf(array.length()));
				// No sub property is available after this
			} else {
				final Matcher matcher = ARRAY_ACCESS_PATTERN.matcher(property);
				if (matcher.matches()) {
					int index = Integer.valueOf(matcher.group(1));
					subParam = JDIParam.arrayElement(array, index, getArrayEleValue(array, index)); 
					// After this we can have access to another dimension of the
					// array or access to the retrieved object's property
					subProperty = matcher.group(2);
				}
			}
		} else if (ObjectReference.class.isAssignableFrom(value.getClass())) {
			ObjectReference object = (ObjectReference) value;
			final Matcher matcher = OBJECT_ACCESS_PATTERN.matcher(property);
			if (matcher.matches()) {
				final String propertyName = matcher.group(1);
				Field propertyField = null;
				for (Field field : object.referenceType().allFields()) {
					if (field.name().equals(propertyName)) {
						propertyField = field;
						break;
					}
				}
				if (propertyField != null) {
					subParam = JDIParam.nonStaticField(propertyField, object, object.getValue(propertyField));
					subProperty = matcher.group(2);
					if (sav.common.core.utils.StringUtils.isEmpty(subProperty)) {
						subProperty = matcher.group(3);
					}
				}
			}
		}
		return recursiveMatch(subParam, subProperty);
	}

	private Value getArrayEleValue(ArrayReference array, int index) {
		if (array == null) {
			return null;
		}
		if (index >= array.length()) {
			return null;
		}
		return array.getValue(index);
	}

	/** 
	 * 
	 * append execution value
	 * 
	 */
	public void appendVarVal(VarValue parent, Variable childVar, Value childVarValue, int level, 
			ThreadReference thread, boolean isRoot) {
		if(level<=0) {
			return;
		}
		level--;
		
		if (childVarValue == null) {
			ReferenceValue val = new ReferenceValue(true, false, childVar);
			setPrimitiveID(parent, val, executor.getTrace());
			
			if(val.getVarID()!=null){
				parent.addChild(val);
				val.addParent(parent);				
			}
			return;
		}
//		System.out.println(level);
		
		Type type = childVarValue.type();
		
		if (type instanceof PrimitiveType) {
			PrimitiveValue ele = new PrimitiveValue(childVarValue.toString(), isRoot, childVar);
			setPrimitiveID(parent, ele, executor.getTrace());
			if(ele.getVarID()!=null){
				parent.addChild(ele);
				ele.addParent(parent);				
			}
		} else if (type instanceof ArrayType) { 
			appendArrVarVal(parent, childVar, (ArrayReference)childVarValue, level, thread, isRoot);
		} else if (type instanceof ClassType) {
			/**
			 * if the class name is "String"
			 */
			if (PrimitiveUtils.isString(type.name())) {
				String pValue = JavaUtil.toPrimitiveValue((ClassType) type, (ObjectReference)childVarValue, thread);
				StringValue ele = new StringValue(pValue, isRoot, childVar);
				ele.setVarID(String.valueOf(((ObjectReference)childVarValue).uniqueID()));
				appendVarID(ele);
				parent.addChild(ele);
				ele.addParent(parent);
			} 
			/**
			 * if the class name is "Integer", "Float", ...
			 */
			else if (PrimitiveUtils.isPrimitiveType(type.name())) {
				String pValue = JavaUtil.toPrimitiveValue((ClassType) type, (ObjectReference)childVarValue, thread);
				PrimitiveValue ele = new PrimitiveValue(pValue, isRoot, childVar);
				ele.setVarID(String.valueOf(((ObjectReference)childVarValue).uniqueID()));
				appendVarID(ele);
				parent.addChild(ele);
				ele.addParent(parent);
			} 
			/**
			 * if the class is an arbitrary complicated class
			 */
			else {
				appendClassVarVal(parent, childVar, (ObjectReference) childVarValue, level, thread, isRoot);
			}
		}
	}
	
	private void setPrimitiveID(VarValue parent, VarValue child, Trace trace){
		ReferenceValue refValue = (ReferenceValue)parent;
		String uniqueID = String.valueOf(refValue.getUniqueID());
		String rawChildVarID = null;
		if(child.isField()){
			rawChildVarID = Variable.concanateFieldVarID(uniqueID, child.getVarName());
		}
		else if(child.isElementOfArray()){
			rawChildVarID = Variable.concanateArrayElementVarID(uniqueID, child.getVarName());
		}
		else if(child.isLocalVariable()){
			LocalVar localVar = (LocalVar)child.getVariable();
			LocalVariableScope scope = trace.getLocalVariableScopes().findScope(child.getVarName(), 
					localVar.getLineNumber(), localVar.getLocationClass());
			
			if(scope != null){
				rawChildVarID = Variable.concanateLocalVarID(localVar.getLocationClass(), localVar.getName(), 
						scope.getStartLine(), scope.getEndLine());
			}
		}
		
		if(rawChildVarID != null){
			child.setVarID(rawChildVarID);
			appendVarID(child);
		}
	}
	

	/**
	 * add a given variable to its parent
	 * 
	 * @param parent
	 * @param varName
	 * @param objRef
	 * @param level
	 * @param thread
	 */
	private void appendClassVarVal(VarValue parent, Variable childVar0, ObjectReference objRef, 
			int level, ThreadReference thread, boolean isRoot) {
		if(level==0) {
			return;
		}
//		level--;
		ClassType type = (ClassType) objRef.type();
		
		long refID = objRef.uniqueID();
		Variable childVar = childVar0.clone();
		/**
		 * Here, check whether this object has been parsed.
		 */
		ReferenceValue val = this.objectPool.get(refID);
		if(val == null){
			val = new ReferenceValue(false, refID, isRoot, childVar);	
			val.setVarID(String.valueOf(refID));
			this.objectPool.put(refID, val);
			
			boolean needParseFields = HeuristicIgnoringFieldRule.isNeedParsingFields(type);
			if(needParseFields){
				Map<Field, Value> fieldValueMap = objRef.getValues(type.allFields());
				List<Field> fieldList = new ArrayList<>(fieldValueMap.keySet());
				Collections.sort(fieldList, new Comparator<Field>() {
					@Override
					public int compare(Field o1, Field o2) {
						return o1.name().compareTo(o2.name());
					}
				});
				for (Field field : fieldList) {
					Value childVarValue = fieldValueMap.get(field);
					if(type.isEnum()){
						String childTypeName = field.typeName();
						if(childTypeName.equals(type.name())){
							continue;
						}
					}
					
					boolean isIgnore = HeuristicIgnoringFieldRule.isForIgnore(type, field);
					if(!isIgnore){
//						String childVarID = val.getChildId(field.name());
						if(childVarValue != null){
							FieldVar var = new FieldVar(field.isStatic(), field.name(), 
									childVarValue.type().toString(), field.declaringType().signature());
							appendVarVal(val, var, childVarValue, level, thread, false);											
						}
					}
				}
			}
			
	        
			StringBuffer buffer = new StringBuffer();
			buffer.append("[");
			for(VarValue child: val.getChildren()){
				String childStringValue = child.getStringValue(); 
				if(childStringValue!=null && childStringValue.length()>300){
					childStringValue = childStringValue.substring(0, 300);
				}
				buffer.append(child.getVarName() + "=" + childStringValue + "; ");
			}
			buffer.append("]");
			val.setStringValue(buffer.toString());
		}
		/**
		 * handle the case of alias variable
		 */
		else if(!val.getVarName().equals(childVar.getName())){
			ReferenceValue cachedValue = val/*.clone()*/;
			val = new ReferenceValue(false, refID, isRoot, childVar);	
			val.setStringValue(cachedValue.getStringValue());
			val.setVarID(String.valueOf(refID));
			val.setChildren(cachedValue.getChildren());
			for(VarValue child: cachedValue.getChildren()){
				child.addParent(val);
			}
		}
		
		appendVarID(val);
		
		parent.addChild(val);
		val.addParent(parent);
	}

	/**
	 * given a variable with a raw variable id (i.e., a variable id without order), this 
	 * method generates an order for it.
	 * @param val
	 * @param accessType
	 */
	private void appendVarID(VarValue val) {
		Variable var = val.getVariable();
		Trace trace = this.executor.getTrace();
		
		/**
		 * if the variable is an array element, we favor the alias ID.
		 */
		String varID0 = var.getVarID();
		if(val.getVariable() instanceof ArrayElementVar){
			varID0 = null;
		}
		
		String order = trace.findDefiningNodeOrder(Variable.READ, trace.getLatestNode(), varID0, var.getAliasVarID());
		if(order.equals("0")){
			order = trace.findDefiningNodeOrder(Variable.WRITTEN, trace.getLatestNode(), var.getVarID(), var.getAliasVarID());
		}
		String varID = var.getVarID() + ":" + order;
		val.setVarID(varID);
		
		if(var.getAliasVarID()!=null){
			String aliasID = var.getAliasVarID() + ":" + order;
			val.setAliasVarID(aliasID);			
		}
	}

	private synchronized void setMessageValue(ThreadReference thread, ReferenceValue val) {
		StackFrame frame;
		try {
			frame = findFrameByLocation(thread.frames(), loc);
			Value value = JavaUtil.retriveExpression(frame, val.getVarName());
			
			if(value instanceof ObjectReference){
				ObjectReference objValue = (ObjectReference)value;
				String stringValue;
				if(value instanceof ArrayReference){
					stringValue = JavaUtil.retrieveStringValueOfArray((ArrayReference)value);
				}
				else{
					stringValue = JavaUtil.retrieveToStringValue(objValue, Settings.getVariableLayer(), thread);					
				}
				
				val.setStringValue(stringValue);
			}
			else{
				System.currentTimeMillis();
			}
		} catch (AbsentInformationException e) {
			e.printStackTrace();
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * append a array variable (namely, varaible0) to its parent.
	 * @param parent
	 * @param variable0
	 * @param value
	 * @param level
	 * @param thread
	 * @param isRoot
	 */
	private void appendArrVarVal(VarValue parent, Variable variable0,
			ArrayReference value, int level, ThreadReference thread, boolean isRoot) {
		if(level==0) {
			return;
		}
//		level--;
		
		Variable variable = variable0.clone();
		ArrayValue arrayVal = new ArrayValue(false, isRoot, variable);
		String componentType = ((ArrayType)value.type()).componentTypeName();
		arrayVal.setComponentType(componentType);
		arrayVal.setReferenceID(value.uniqueID());
		appendVarID(arrayVal);
		
		//add value of elements
		List<Value> list = new ArrayList<>();
		if(value.length() > 0){
			list = value.getValues(0, value.length()); 
		}
		for(int i = 0; i < value.length(); i++){
			String parentSimpleID = Variable.truncateSimpleID(arrayVal.getVarID());
			String aliasVarID = Variable.concanateArrayElementVarID(parentSimpleID, String.valueOf(i));
//			variable0.setAliasVarID(aliasVarID);
//			Trace trace = this.executor.getTrace();
//			String order = trace.findDefiningNodeOrder(accessType, trace.getLatestNode(), true, aliasVarID, aliasVarID);
//			aliasVarID = aliasVarID + ":" + order;
			
			String varName = String.valueOf(i);
			Value elementValue = list.get(i);
			
			ArrayElementVar var = new ArrayElementVar(varName, componentType, aliasVarID);
			appendVarVal(arrayVal, var, elementValue, level, thread, false);
		}
		
		StringBuffer buffer = new StringBuffer();
		for(VarValue childValue: arrayVal.getChildren()){
			buffer.append(childValue.getStringValue()+", ");
		}
		arrayVal.setStringValue(buffer.toString());
		
		parent.addChild(arrayVal);
		arrayVal.addParent(parent);
	}
	/***/
	protected StackFrame findFrameByLocation(List<StackFrame> frames,
			Location location) throws AbsentInformationException {
		for (StackFrame frame : frames) {
			if (areLocationsEqual(frame.location(), location)) {
				return frame;
			}
		}
		
		throw new AbsentInformationException("Can not find frame");
	}
	
	private boolean areLocationsEqual(Location location1, Location location2) throws AbsentInformationException {
		//return location1.compareTo(location2) == 0;
		return location1.equals(location2);
	}

	public ProgramExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(ProgramExecutor executor) {
		this.executor = executor;
	}
	
}
