/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.codeanalysis.runtime.variable;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * @author LLT
 * 
 * A wrapper class containing JDI information.
 *
 */
@SuppressWarnings("restriction")
public class JDIParam {
	private JDIParamType type;
	/* local variable */
	private LocalVariable variable;
	/* field */
	private Field field;
	/* non-static */
	private ObjectReference obj;
	/* static */
	private ReferenceType objType;
	/* for arr */
	private int idx;
	/* value */
	private Value value;
	
	private JDIParam() {}
	
	public LocalVariable getLocalVariable() {
		return variable;
	}

	public Field getField() {
		return field;
	}

	public ObjectReference getObj() {
		return obj;
	}

	public ReferenceType getObjType() {
		return objType;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}
	
	public JDIParamType getType() {
		return type;
	}
	
	public int getIdx() {
		return idx;
	}
	
	public ArrayReference getArrayRef() {
//		Assert.assertTrue(type == JDIParamType.ARRAY_ELEMENT,
//				"Expected arrayType, but get ", type.name());
		
		if(type != JDIParamType.ARRAY_ELEMENT){
			System.err.println("Expected arrayType, but get " + type.name());
		}
		
		return (ArrayReference) obj;
	}

	@Override
	public String toString() {
		return "JdiParam [type=" + type + ", variable=" + variable + ", field="
				+ field + ", obj=" + obj + ", objType=" + objType + ", idx="
				+ idx + ", value=" + value + "]";
	}

	public static JDIParam localVariable(LocalVariable variable, Value value) {
		JDIParam param = new JDIParam();
		param.type = JDIParamType.LOCAL_VAR;
		param.variable = variable;
		param.value = value;
		return param;
	}
	
	public static JDIParam staticField(Field field, ReferenceType objType, Value value) {
		JDIParam param = new JDIParam();
		param.type = JDIParamType.STATIC_FIELD;
		param.field = field;
		param.objType = objType;
		param.value = value;
		return param;
	}

	public static JDIParam nonStaticField(Field field, ObjectReference objRef, Value value) {
		JDIParam param = new JDIParam();
		param.type = JDIParamType.NON_STATIC_FIELD;
		param.field = field;
		param.obj = objRef;
		param.value = value;
		return param;
	}
	
	public static JDIParam arrayElement(ArrayReference array, int idx, Value value) {
		JDIParam param = new JDIParam();
		param.type = JDIParamType.ARRAY_ELEMENT;
		param.obj = array;
		param.value = value;
		param.idx = idx;
		return param;
	}

	public enum JDIParamType {
		NON_STATIC_FIELD,
		ARRAY_ELEMENT,
		STATIC_FIELD,
		LOCAL_VAR
	}
}