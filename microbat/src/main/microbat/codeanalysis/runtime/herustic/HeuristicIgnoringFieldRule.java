package microbat.codeanalysis.runtime.herustic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Type;

@SuppressWarnings("restriction")
public class HeuristicIgnoringFieldRule {
	
	public static final String ENUM = "enum";
	
	/**
	 * this map store <className, list<fieldName>>, specifying which fields will be
	 * ignored in which class.
	 */
	private static Map<String, ArrayList<String>> ignoringMap = new HashMap<>();
	
	private static List<String> prefixExcludes = new ArrayList<>();
	
	static{
		String c1 = "java.util.ArrayList";
		
		ArrayList<String> fieldList1 = new ArrayList<>();
		fieldList1.add("serialVersionUID");
		fieldList1.add("DEFAULT_CAPACITY");
		fieldList1.add("EMPTY_ELEMENTDATA");
		fieldList1.add("DEFAULTCAPACITY_EMPTY_ELEMENTDATA");
		fieldList1.add("MAX_ARRAY_SIZE");
		fieldList1.add("modCount");
		fieldList1.add("ENUM$VALUES");
		ignoringMap.put(c1, fieldList1);
		
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
	
	public static boolean isForIgnore(ClassType type, String fieldName){
		String className;
		if(type.isEnum()){
			className = ENUM;
		}
		else{
			className = type.name();
		}
		
		ArrayList<String> fields = ignoringMap.get(className);
		if(fields != null){
			return fields.contains(fieldName);			
		}
		
		System.currentTimeMillis();
		return false;
	}

	/**
	 * For some JDK class, we do not need its detailed fields. However, we may still be
	 * interested in the elements in Collection class.
	 * @param type
	 * @return
	 */
	public static boolean isNeedParsingFields(ClassType type) {
		String typeName = type.name();
		
		if(containPrefix(typeName, prefixExcludes)){
			for(InterfaceType interf: type.interfaces()){
				if(interf.name().contains("java.util.Collection")){
					return true;
				}
				else{
					List<Type> allInterfaces = new ArrayList<>();
					findAllSuperTypes(type, allInterfaces);
					if(allInterfaces.toString().contains("java.util.Collection")){
						return true;
					}
				}
			}
			
			return false;
		}
		
		return true;
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
