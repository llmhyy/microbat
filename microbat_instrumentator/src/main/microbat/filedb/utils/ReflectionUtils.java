package microbat.filedb.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import microbat.filedb.RecordsFileException;
import microbat.filedb.annotation.RecordType;

/**
 * @author LLT
 *
 */
public class ReflectionUtils {

	public static Method getGetterMethod(Class<?> clazz, Field field) throws RecordsFileException {
		String methodName = null;
		if (boolean.class.isAssignableFrom(field.getType())) {
			if (field.getName().startsWith("is")) {
				methodName = field.getName();
			} else {
				methodName = "is" + StringUtils.capitalize(field.getName());
			}
		} else {
			methodName = "get" + StringUtils.capitalize(field.getName());
		}
		try {
			return clazz.getMethod(methodName);
		} catch (Exception e) {
			throw new RecordsFileException("Fail to get getter method", e);
		}
	}
	
	public static List<Field> getAllRecordFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		collectRecordFields(clazz, fields);
		return fields;
	}

	private static void collectRecordFields(Class<?> clazz, List<Field> fields) {
		if (!clazz.isAnnotationPresent(RecordType.class)) {
			return;
		}
		for (Field field : clazz.getDeclaredFields()) {
			fields.add(field);
		}
		collectRecordFields(clazz.getSuperclass(), fields);
	}

	public static List<Method> getAllRecordMethods(Class<?> clazz) {
		List<Method> methods = new ArrayList<>();
		collectRecordMethods(clazz, methods );
		return methods;
	}
	
	private static void collectRecordMethods(Class<?> clazz, List<Method> methods) {
		if (!clazz.isAnnotationPresent(RecordType.class)) {
			return;
		}
		for (Method method : clazz.getDeclaredMethods()) {
			methods.add(method);
		}
		collectRecordMethods(clazz.getSuperclass(), methods);
	}
}
