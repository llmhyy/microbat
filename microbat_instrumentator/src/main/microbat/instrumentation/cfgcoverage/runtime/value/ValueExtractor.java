package microbat.instrumentation.cfgcoverage.runtime.value;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.runtime.HeuristicIgnoringFieldRule;
import microbat.instrumentation.runtime.TraceUtils;
import microbat.model.BreakPointValue;
import microbat.model.value.ArrayValue;
import microbat.model.value.PrimitiveValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.StringValue;
import microbat.model.value.VarValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.util.PrimitiveUtils;
import sav.common.core.utils.SignatureUtils;

/**
 * 
 * @author lyly
 * 
 */
public class ValueExtractor {
	public static int variableLayer = 1;

	public BreakPointValue extractInputValue(String valueId, String className,
			Object methodSignature, String paramTypeSignsCode, String paramNamesCode, Object[] params) {
		String[] parameterTypes = TraceUtils.parseArgTypesOrNames(paramTypeSignsCode);
		String[] parameterNames = TraceUtils.parseArgTypesOrNames(paramNamesCode);
		BreakPointValue bkpValue = new BreakPointValue(valueId);
		for(int i=0; i<parameterTypes.length; i++){
			String pType = parameterTypes[i];
			String parameterType = SignatureUtils.signatureToName(pType);
			String varName = parameterNames[i];
			
			Variable var = new LocalVar(varName, parameterType, className, -1);
			var.setVarID(varName);
			if(!PrimitiveUtils.isPrimitive(pType)){
				String aliasID = TraceUtils.getObjectVarId(params[i], pType);
				var.setAliasVarID(aliasID);				
			}
			VarValue value = appendVarValue(params[i], var, null);
			bkpValue.addChild(value);
		}
		return bkpValue;
	}
	
	private VarValue appendVarValue(Object value, Variable var, VarValue parent) {
		return appendVarValue(value, var, parent, variableLayer); 
	}

	public VarValue appendVarValue(Object value, Variable var, VarValue parent, int retrieveLayer) {
		if (retrieveLayer <= 0) {
			return null;
		}
		retrieveLayer--;
		
		boolean isRoot = (parent == null);
		VarValue varValue = null;
		if (PrimitiveUtils.isString(var.getType())) {
			varValue = new StringValue(String.valueOf(value), isRoot, var);
		} else if (PrimitiveUtils.isPrimitive(var.getType())) {
			varValue = new PrimitiveValue(String.valueOf(value), isRoot, var);
		} else if(var.getType().endsWith("[]")) {
			/* array */
			ArrayValue arrVal = new ArrayValue(value == null, isRoot, var);
			arrVal.setComponentType(var.getType().substring(0, var.getType().length() - 2)); // 2 = "[]".length
			varValue = arrVal;
			if (value == null) {
				arrVal.setNull(true);
			} else {
				int length = Array.getLength(value);
				arrVal.ensureChildrenSize(length);
				for (int i = 0; i < length; i++) {
					String arrayElementID = ExecutionValueHelper.getArrayElementID(var.getVarID(), i);
					ArrayElementVar varElement = new ArrayElementVar(arrayElementID, arrVal.getComponentType(), arrayElementID);
					Object elementValue = Array.get(value, i);
					appendVarValue(elementValue, varElement, arrVal, retrieveLayer);
//					if (HeuristicIgnoringFieldRule.isHashMapTableType(arrVal.getComponentType())) {
//						appendVarValue(elementValue, varElement, arrVal, retrieveLayer + 1);
//					} else {
//						appendVarValue(elementValue, varElement, arrVal, retrieveLayer);
//					}
				}
			}
		} else {
			ReferenceValue refVal = new ReferenceValue(value == null, -1, isRoot, var);
			varValue = refVal;
			if (value != null) {
				Class<?> objClass = value.getClass();
				var.setRtType(objClass.getName());
				boolean needParseFields = HeuristicIgnoringFieldRule.isNeedParsingFields(objClass);
				boolean isCollectionOrHashMap = HeuristicIgnoringFieldRule.isCollectionClass(objClass)
						|| HeuristicIgnoringFieldRule.isHashMapClass(objClass);
				if (needParseFields) {
					List<Field> validFields = HeuristicIgnoringFieldRule.getValidFields(objClass, value);
					for (Field field : validFields) {
						field.setAccessible(true);
						try {
							Object fieldValue = field.get(value);
							Class<?> fieldType = field.getType();
							String fieldTypeStr = fieldType.getName();
							if (fieldType.isArray()) {
								fieldTypeStr = SignatureUtils.signatureToName(fieldTypeStr);
							}
							if(fieldType.isEnum()){
								if(fieldTypeStr.equals(var.getType())){
									continue;
								}
							}
							if(fieldValue != null){
								FieldVar fieldVar = new FieldVar(Modifier.isStatic(field.getModifiers()),
										field.getName(), fieldTypeStr, field.getDeclaringClass().getName());
								fieldVar.setVarID(ExecutionValueHelper.getFieldId(var.getVarID(), field.getName()));
								if (isCollectionOrHashMap && HeuristicIgnoringFieldRule
										.isCollectionOrMapElement(var.getRuntimeType(), field.getName())) {
									appendVarValue(fieldValue, fieldVar, refVal, retrieveLayer + 1);
								} else {
									appendVarValue(fieldValue, fieldVar, refVal, retrieveLayer);
								}
							}
						} catch (Exception e) {
							handleException(e);
						}
					}
				}
			}
		}
		if (parent != null) {
			parent.linkAchild(varValue);
		}
		return varValue;
	}

	private void handleException(Throwable t) {
		if (t.getMessage() != null) {
			AgentLogger.info("ExecutionTracer error: " + t.getMessage());
		}
		AgentLogger.error(t);
	}

}
