package microbat.codeanalysis.runtime.herustic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Type;

@SuppressWarnings("restriction")
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
	private static Map<String, ArrayList<String>> ignoringMap = new HashMap<>();
	
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
		
		String[] excArray = new String[]{"java.", "javax.", "sun.", "com.sun.", "org.junit."};
		for(String exc: excArray){
			prefixExcludes.add(exc);
		}
		
		isSpecialToRecordFieldSet.add("java.lang.StringBuilder");
	}
	
	public static boolean isForIgnore(ClassType type, Field field){
		String fieldName = field.name();
		String className;
		ArrayList<String> fields;
		
		if(type.isEnum()){
			Type fieldType;
			try {
				fieldType = field.type();
				if(fieldType instanceof ClassType){
					ClassType rType = (ClassType)fieldType;
					if(rType.isEnum()){
						return true;
					}
				}
			} catch (ClassNotLoadedException e) {
				//e.printStackTrace();
			}
			
			className = ENUM;
			fields = ignoringMap.get(className);
			if(fields != null){
				return fields.contains(fieldName);			
			}
		}
		else{
			className = type.name();
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
	
	
	private static boolean isSerializableClass(ClassType type) {
		String typeName = type.name();
		Boolean isSerializable = isSerializableMap.get(typeName);
		
		if(isSerializable == null){
			List<Type> allSuperTypes = new ArrayList<>();
			findAllSuperTypes(type, allSuperTypes);
			isSerializable = allSuperTypes.toString().contains(HeuristicIgnoringFieldRule.SERIALIZABLE);
			if(isSerializable){
				isSerializableMap.put(typeName, true);
			}
			else{
				isSerializableMap.put(typeName, false);
			}
		}
		
		return isSerializable;
	}


	public static boolean isCollectionClass(ClassType type){
		String typeName = type.name();
		Boolean isCollection = isCollectionMap.get(typeName);
		
		if(isCollection == null){
			List<Type> allSuperTypes = new ArrayList<>();
			findAllSuperTypes(type, allSuperTypes);
			isCollection = allSuperTypes.toString().contains(HeuristicIgnoringFieldRule.COLLECTION);
			if(isCollection){
				isCollectionMap.put(typeName, true);
			}
			else{
				isCollectionMap.put(typeName, false);
			}
		}
		
		return isCollection;
	}
	
	public static boolean isHashMapClass(ClassType type){
		String typeName = type.name();
		Boolean isHashMap = isHashMapMap.get(typeName);
		
		if(isHashMap == null){
			List<Type> allSuperTypes = new ArrayList<>();
			findAllSuperTypes(type, allSuperTypes);
			isHashMap = allSuperTypes.toString().contains(HeuristicIgnoringFieldRule.HASHMAP);
			if(isHashMap){
				isHashMapMap.put(typeName, true);
			}
			else{
				isHashMapMap.put(typeName, false);
			}
		}
		
		return isHashMap;
	}

	private static boolean isValidField(String fieldName, String className,
			Map<String, ArrayList<String>> ignoringMap) {
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
	public static boolean isNeedParsingFields(ClassType type) {
		String typeName = type.name();
		
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
	
	private static void findAllSuperTypes(Type type, List<Type> allSuperType) {
		if(type instanceof ClassType){
			ClassType classType = (ClassType)type;
			if(!allSuperType.contains(classType)){
				allSuperType.add(classType);				
			}
			ClassType superClass = classType.superclass();
			findAllSuperTypes(superClass, allSuperType);
			
			for(InterfaceType interfaceType: classType.interfaces()){
				findAllSuperTypes(interfaceType, allSuperType);
			}
		}
		else if(type instanceof InterfaceType){
			InterfaceType interfaceType = (InterfaceType)type;
			if(!allSuperType.contains(interfaceType)){
				allSuperType.add(interfaceType);
			}
			
			for(InterfaceType superInterface: interfaceType.superinterfaces()){
				findAllSuperTypes(superInterface, allSuperType);
			}
		}
		
	}

	private static boolean containPrefix(String name, List<String> prefixList){
		for(String prefix: prefixList){
			if(name.startsWith(prefix)){
				return true;
			}
		}
		return false;
	}
}
