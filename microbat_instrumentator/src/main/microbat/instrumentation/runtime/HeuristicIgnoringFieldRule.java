package microbat.instrumentation.runtime;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import microbat.instrumentation.AgentLogger;
import sav.common.core.utils.CollectionUtils;

public class HeuristicIgnoringFieldRule {
	
	public static final String ENUM = "enum";
	public static final String SERIALIZABLE = "java.io.Serializable";
	public static final String COLLECTION = "java.util.Collection";
	public static final String HASHMAP = "java.util.HashMap";
	
	/**
	 * for example, I record map(java.util.Stack)=java.io.Collection
	 */
	private static Map<String, Boolean> isCollectionMap = new HashMap<>();
	private static Map<String, Boolean> isHashMapMap = new HashMap<>();
	private static Map<String, Boolean> isSerializableMap = new HashMap<>();
	
	/**
	 * this map store <className, list<fieldName>>, specifying which fields will be
	 * ignored in which class.
	 */
	private static Map<String, List<String>> ignoringMap = new HashMap<>();
	private static Set<String> toCheckIgnoreFieldClasses;
	
	/**
	 * specify special JDK class to parse its fields
	 */
	private static Set<String> isSpecialToRecordFieldSet = new HashSet<>();
	
	private static List<String> prefixExcludes = new ArrayList<>();
	
	static{
		ArrayList<String> fieldList0 = new ArrayList<>();
		fieldList0.add("serialVersionUID");
		ignoringMap.put(SERIALIZABLE, fieldList0);
		
		ArrayList<String> fieldList1 = new ArrayList<>();
		fieldList1.add("DEFAULT_CAPACITY");
		fieldList1.add("EMPTY_ELEMENTDATA");
		fieldList1.add("DEFAULTCAPACITY_EMPTY_ELEMENTDATA");
		fieldList1.add("MAX_ARRAY_SIZE");
		fieldList1.add("modCount");
		fieldList1.add("ENUM$VALUES");
		ignoringMap.put(COLLECTION, fieldList1);
		
		ArrayList<String> fieldList2 = new ArrayList<>();
		fieldList2.add("ALTERNATIVE_HASHING_THRESHOLD_DEFAULT");
		fieldList2.add("DEFAULT_INITIAL_CAPACITY");
		fieldList2.add("MAXIMUM_CAPACITY");
		fieldList2.add("DEFAULT_LOAD_FACTOR");
		fieldList2.add("EMPTY_TABLE");
		fieldList2.add("modCount");
		fieldList2.add("threshold");
		fieldList2.add("hashSeed");
		fieldList2.add("loadFactor");
		ignoringMap.put(HASHMAP, fieldList2);
		
		String c2 = ENUM;
		ArrayList<String> fieldList3 = new ArrayList<>();
		fieldList3.add("ordinal");
		fieldList3.add("ENUM$VALUES");
		ignoringMap.put(c2, fieldList3);
		
		ignoringMap.put("java.util.HashMap$KeySet", Arrays.asList("this$0"));
		ignoringMap.put("java.util.HashMap$Values", Arrays.asList("this$0"));
		
		String[] excArray = new String[]{"java.", "javax.", "sun.", "com.sun.", "org.junit."};
		for(String exc: excArray){
			prefixExcludes.add(exc);
		}
		
		isSpecialToRecordFieldSet.add("java.lang.StringBuilder");
		
		toCheckIgnoreFieldClasses = new HashSet<>();
		toCheckIgnoreFieldClasses.add("java.util.HashMap$KeySet");
		toCheckIgnoreFieldClasses.add("java.util.HashMap$Values");
	}
	
	public static boolean isForIgnore(Class<?> type, Field field){
		String fieldName = field.getName();
		String className;
		List<String> fields;
		
		if(type.isEnum()){
			Class<?> fieldType;
			fieldType = field.getType();
			if (fieldType.isEnum()) {
				return true;
			}
			
			className = ENUM;
			fields = ignoringMap.get(className);
			if(fields != null){
				return fields.contains(fieldName);			
			}
		}
		else{
			className = type.getName();
			
			if (toCheckIgnoreFieldClasses.contains(className)) {
				return CollectionUtils.nullToEmpty(ignoringMap.get(className)).contains(fieldName);
			}
			
			if(isSerializableClass(type)){
				if(isValidField(fieldName, HeuristicIgnoringFieldRule.SERIALIZABLE, ignoringMap)){
					return true;
				}
			}
			
			if(isCollectionClass(type)){
				if(isValidField(fieldName, HeuristicIgnoringFieldRule.COLLECTION, ignoringMap)){
					return true;
				}
			}
			
			if(isHashMapClass(type)){
				if(isValidField(fieldName, HeuristicIgnoringFieldRule.HASHMAP, ignoringMap)){
					return true;
				}
			}
			return false;
		}
		
		return false;
	}
	
	
	private static boolean isSerializableClass(Class<?> type) {
		String typeName = type.getName();
		Boolean isSerializable = isSerializableMap.get(typeName);
		
		if(isSerializable == null){
			isSerializable = Serializable.class.isAssignableFrom(type);
			isSerializableMap.put(typeName, isSerializable);
		}
		
		return isSerializable;
	}


	public static boolean isCollectionClass(Class<?> type){
		String typeName = type.getName();
		Boolean isCollection = isCollectionMap.get(typeName);
		
		if(isCollection == null){
			isCollection = Collection.class.isAssignableFrom(type);
			isCollectionMap.put(typeName, isCollection);
		}
		
		return isCollection;
	}
	
	public static boolean isHashMapClass(Class<?> type){
		String typeName = type.getName();
		Boolean isHashMap = isHashMapMap.get(typeName);
		
		if(isHashMap == null){
			isHashMap = HashMap.class.isAssignableFrom(type);
			isHashMapMap.put(typeName, isHashMap);
		}
		
		return isHashMap;
	}

	private static boolean isValidField(String fieldName, String className,
			Map<String, List<String>> ignoringMap) {
		List<String> fields = ignoringMap.get(className);
		if(fields != null){
			return fields.contains(fieldName);
		}
		
		return false;
	}

	
	
	
	private static Map<String, Boolean> parsingTypeMap = new HashMap<>();
	/**
	 * For some JDK class, we do not need its detailed fields. However, we may still be
	 * interested in the elements in Collection class.
	 * @param type
	 * @return
	 */
	public static boolean isNeedParsingFields(Class<?> type) {
		String typeName = type.getName();
		
		if(isSpecialToRecordFieldSet.contains(typeName)) {
			return true;
		}
		
		Boolean isNeed = parsingTypeMap.get(typeName);
		if(isNeed == null){
			if(containPrefix(typeName, prefixExcludes)){
				isNeed = isCollectionClass(type) || isHashMapClass(type);
			}
			else{
				isNeed = true;				
			}
			
			parsingTypeMap.put(typeName, isNeed);
		}
		
		return isNeed;
	}
	
	private static boolean containPrefix(String name, List<String> prefixList){
		for(String prefix: prefixList){
			if(name.startsWith(prefix)){
				return true;
			}
		}
		return false;
	}

	private static final Map<String, List<String>> collectionMapElements = new HashMap<>();
	static {
		
		collectionMapElements.put("java.util.ArrayList", Arrays.asList("elementData"));
		collectionMapElements.put("java.util.HashMap", Arrays.asList("table"));
	}
	public static boolean isCollectionOrMapElement(String className, String fieldName) {
		return CollectionUtils.nullToEmpty(collectionMapElements.get(className)).contains(fieldName);
	}

	public static List<Field> getValidFields(Class<?> objClass, Object value) {
		List<Field> validFields = new ArrayList<>();
		for (Field field : objClass.getDeclaredFields()) {
			if (!isForIgnore(objClass, field)) {
				validFields.add(field);
			}
		}
		if (isHashMapClass(objClass)) {
			Field field;
			try {
				field = HashMap.class.getDeclaredField("table");
				validFields.add(field);
			} catch (Exception e) {
				AgentLogger.error(e);
			}
		} 
		
		if(Vector.class.isAssignableFrom(objClass)) {
			Field field;
			try {
				field = Vector.class.getDeclaredField("elementData");
				validFields.add(field);
			} catch (Exception e) {
				AgentLogger.error(e);
			}
		}
		
		return validFields;
	}

	public static boolean isHashMapTableType(String type) {
		return "java.util.HashMap$Node".equals(type) || 
				"java.util.HashMap$Entry".equals(type);
	}
	
	public static String getInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("isCollectionMap: ").append(isCollectionMap.size())
			.append("isHashMapMap: ").append(isHashMapMap.size())
			.append("isSerializableMap: ").append(isSerializableMap.size())
			.append("parsingTypeMap: ").append(parsingTypeMap.size());
		return sb.toString();
	}

	public static void clearCache() {
		isCollectionMap = new HashMap<>();
		isHashMapMap = new HashMap<>();
		parsingTypeMap = new HashMap<>();
	}
}
