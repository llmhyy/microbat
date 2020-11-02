package microbat.handler.xml;

import static microbat.handler.xml.VarValueXmlConstants.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
import microbat.model.variable.Variable;
import microbat.model.variable.VirtualVar;
import microbat.util.PrimitiveUtils;
import sav.common.core.SavRtException;
import sav.common.core.utils.StringUtils;

public class VarValueXmlReader {
	
	public static List<VarValue> read(String str) {
		if (StringUtils.isEmpty(str)) {
			return new ArrayList<>();
		}
		VarValueXmlReader reader = new VarValueXmlReader();
		ByteArrayInputStream in;
		try {
			in = new ByteArrayInputStream(str.getBytes("UTF-8"));
			return reader.read(in);
		} catch (UnsupportedEncodingException e) {
			throw new SavRtException(e);
		}
	}
	
	public List<VarValue> read(InputStream in) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in);
			doc.getDocumentElement().normalize();
			return parse(doc);
		} catch (Exception e) {
			throw new SavRtException(e);
		}
	}

	private List<VarValue> parse(Document doc) {
		NodeList nList = doc.getElementsByTagName(VALUE_TAG);
		List<VarValue> allVarValues = new ArrayList<VarValue>(nList.getLength());
		Map<VarValue, String[]> childrenMap = new HashMap<VarValue, String[]>();
		Map<String, VarValue> varValueIdMap = new HashMap<String, VarValue>();
		for (int i = 0; i < nList.getLength(); i++) {
			Element valueEle = (Element) nList.item(i);
			/* variable */
			Variable variable = parseVariable((Element)valueEle.getElementsByTagName(VARIABLE_TAG).item(0));
			/* value */
			long unqiueID = getLongProperty(valueEle, VALUE_REF_UNIQUE_ID_PROP);
			String valueEleId = getAttribute(valueEle, VALUE_ID_ATT);
			boolean isRoot = getBooleanAttribute(valueEle, VALUE_IS_ROOT_ATT);
			String varType = getAttribute(valueEle, VALUE_VAR_TYPE_ATT);
			String stringVal = getStringValueProperty(valueEle);
			boolean isArray = getBooleanProperty(valueEle, VALUE_IS_ARRAY_PROP);
			
			VarValue value = null;
			if (varType == null) {
				value = new VirtualValue(isRoot, variable, unqiueID);
				value.setStringValue(stringVal);
			} else if (StringValue.TYPE.equals(varType)) {
				value = new StringValue(stringVal, isRoot, variable);
			} 
//			else if (BooleanValue.TYPE.endsWith(varType)) {
//				value = new BooleanValue(Boolean.valueOf(stringVal), isRoot, variable);
//			} 
			else if (PrimitiveUtils.isPrimitiveType(varType)) {
				value = new PrimitiveValue(stringVal, isRoot, variable);
			} else if (isArray) {
				value = new ArrayValue(false, isRoot, variable);
				ArrayValue arrayVal = (ArrayValue) value;
				arrayVal.setComponentType(getProperty(valueEle, VALUE_ARR_COMPONENT_TYPE_PROP));
				arrayVal.setNull(getBooleanProperty(valueEle, VALUE_REF_IS_NULL_PROP));
			} else {
				value = new ReferenceValue(false, isRoot, variable);
				ReferenceValue refVal = (ReferenceValue) value;
				refVal.setUniqueID(getLongProperty(valueEle, VALUE_REF_UNIQUE_ID_PROP));
				refVal.setNull(getBooleanProperty(valueEle, VALUE_REF_IS_NULL_PROP));
			}
			varValueIdMap.put(valueEleId, value);
			String childIds = getProperty(valueEle, VALUE_CHILDREN_PROP);
			if (!StringUtils.isEmpty(childIds)) {
				childrenMap.put(value, childIds.split(VALUE_CHILDREN_SEPARATOR));
			}
			allVarValues.add(value);
		}
		List<VarValue> result = updateVarValueChildren(allVarValues , childrenMap, varValueIdMap);
		for (int i = allVarValues.size() - 1; i >= 0; i--) {
			VarValue value = allVarValues.get(i);
			if (value instanceof ReferenceValue) {
				((ReferenceValue) value).buildStringValue();
			}
		}
		return result;
	}

	private String getStringValueProperty(Element valueEle) {
		String str = getProperty(valueEle, VALUE_STRING_VALUE_PROP);
		return XmlFilter.getValue(str);
	}
	
	/**
	 * return only root values.
	 */
	private List<VarValue> updateVarValueChildren(List<VarValue> allVarValues, Map<VarValue, String[]> childrenMap,
			Map<String, VarValue> varValueIdMap) {
		List<VarValue> result = new ArrayList<>(allVarValues);
		for (VarValue varValue : childrenMap.keySet()) {
			String[] childIds = childrenMap.get(varValue);
			if (childIds == null) {
				continue;
			}
			for (String childId : childIds) {
				VarValue child = varValueIdMap.get(childId);
				varValue.addChild(child);
				child.addParent(varValue);
				result.remove(child);
			}
		}
		return result;
	}

	private Variable parseVariable(Element ele) {
		String cat = ele.getAttribute(VAR_CAT_ATT);
		String name = ele.getAttribute(VAR_NAME_ATT);
		String type = ele.getAttribute(VAR_TYPE_ATT);
		String aliasId = StringUtils.emptyToNull(ele.getAttribute(VAR_ALIAS_ID_ATT));
		String varId = ele.getAttribute(VAR_ID_ATT);
		Variable variable = null;
		if (isOfType(cat, ArrayElementVar.class)) {
			variable = new ArrayElementVar(name, type, aliasId);
		} else if(isOfType(cat, ConstantVar.class)) {
			variable = new ConstantVar(name, type);
		} else if (isOfType(cat, FieldVar.class)) {
			boolean isStatic = getBooleanAttribute(ele, FIELD_VAR_IS_STATIC);
			//TODO, we need the declaring type of the field
			variable = new FieldVar(isStatic , name, type, null);
			((FieldVar) variable).setDeclaringType(getAttribute(ele, FIELD_VAR_DECLARING_TYPE));
		} else if (isOfType(cat, LocalVar.class)) {
			String locationClass = getAttribute(ele, LOCAL_VAR_LOCATION_CLASS);
			int lineNumber = getIntAttribute(ele, LOCAL_VAR_LINE_NUMBER);
			variable = new LocalVar(name, type, locationClass, lineNumber);
		} else if (isOfType(cat, VirtualVar.class)) {
			variable = new VirtualVar(name, type);
		}
		variable.setVarID(varId);
		variable.setAliasVarID(aliasId);
		return variable;
	}
	
	private boolean isOfType(String simpleName, Class<?> type) {
		return type.getSimpleName().equals(simpleName);
	}
	
	private boolean getBooleanProperty(Element element, String propTagName) {
		String str = getProperty(element, propTagName);
		if (str == null) {
			return false;
		}
		return Boolean.valueOf(str);
	}
	
	private String getProperty(Element element, String propTagName) {
		NodeList nList = element.getElementsByTagName(propTagName);
		if (nList == null || nList.getLength() == 0) {
			return null;
		}
		Node item = nList.item(0);
		if (item instanceof CharacterData) {
			CharacterData child = (CharacterData) item;
			return child.getData();
		}
		return item.getTextContent();
	}
	
	private long getLongProperty(Element element, String propTagName) {
		String str = getProperty(element, propTagName);
		if (str == null) {
			return 0;
		}
		return Long.valueOf(str);
	}
	
	private boolean getBooleanAttribute(Element element, String attName) {
		if (!element.hasAttribute(attName)) {
			return false;
		}
		return Boolean.valueOf(element.getAttribute(attName));
	}
	
	private int getIntAttribute(Element element, String attName) {
		if (!element.hasAttribute(attName)) {
			return 0;
		}
		return Integer.valueOf(element.getAttribute(attName));
	}
	
	private String getAttribute(Element element, String attName) {
		if (!element.hasAttribute(attName)) {
			return null;
		}
		return element.getAttribute(attName);
	}
}
