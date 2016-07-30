package microbat.codeanalysis.runtime.herustic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

@SuppressWarnings("restriction")
public class HeuristicIgnoringFieldRule {
	
	public static final String ENUM = "enum";
	public static final String SERIALIZABLE = "java.io.Serializable";
	public static final String COLLECTION = "java.util.Collection";
	
	/**
	 * for example, I record map(java.util.Stack)=java.io.Collection
	 */
	private static Map<String, Boolean> isCollectionMap = new HashMap<>();
	private static Map<String, Boolean> isSerializableMap = new HashMap<>();
	
	/**
	 * this map store <className, list<fieldName>>, specifying which fields will be
	 * ignored in which class.
	 */
	private static Map<String, ArrayList<String>> ignoringMap = new HashMap<>();
	
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
		
		String c2 = ENUM;
		ArrayList<String> fieldList2 = new ArrayList<>();
		fieldList2.add("ordinal");
		fieldList2.add("ENUM$VALUES");
		ignoringMap.put(c2, fieldList2);
		
		String[] excArray = new String[]{"java.", "javax.", "sun.", "com.sun.", "org.junit."};
		for(String exc: excArray){
			prefixExcludes.add(exc);
		}
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
		
		Boolean isNeed = parsingTypeMap.get(typeName);
		if(isNeed == null){
			if(containPrefix(typeName, prefixExcludes)){
				isNeed = isCollectionClass(type);
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
