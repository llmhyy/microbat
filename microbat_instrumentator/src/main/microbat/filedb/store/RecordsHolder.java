package microbat.filedb.store;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import microbat.filedb.annotation.Attribute;
import microbat.filedb.annotation.RecordType;

/**
 * @author LLT Table which holds only data part, not header
 */
public class RecordsHolder<T> {
	private RecordsHeader header;
	private Class<T> clazz;
	private String recordName;
	private List<String> recordAttributes = new ArrayList<String>();

	public RecordsHolder(Class<T> clazz) {
		this.clazz = clazz;
		RecordType recordType = clazz.getAnnotation(RecordType.class);
		recordName = recordType.name();
	}

	public void insert(T row) throws Exception {
		// Get row's class to know whether row is annotated with @RecordType,
		// if not throw exception,
		// while get all fields in row.
		Class<? extends Object> rowClazz = row.getClass();
		if (rowClazz.isAnnotationPresent(RecordType.class)) {
			// For a @RecordType annotated object, look up all fields annotated with
			// @Attribute,
			Field[] fields = rowClazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.isAnnotationPresent(Attribute.class)) {
					// if field type is primitive/String, printout is value,
					// format:field_name=field_value.
					if (checkIsPrimitiveOrString(field)) {
						// Field value can be get using getter method.
						System.out.println(field.getName() + " = " + getter(row, field).toString());
					} else {
						// if field type is a @RecordType annotated class,
						// look up all fields annotated with @Attribute and do the same.
						if (field.getType().isAnnotationPresent(RecordType.class)) {
							T getter = (T) getter(row, field);
							// recursively
							if (getter != null) {
								insert(getter);
							}	
						}
					}
				}
			}
		} else {
			throw new Exception("Object class is not annotated with @RecordType");
		}
	}

	public void insertBatch(List<T> rows) {
		// FIXME XUEZHI [2]: implement as in task description.
		// For each all row in rows,
		for (T row : rows) {
			try {
				insert(row);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean checkIsPrimitiveOrString(Field field) {
		boolean flag = false;
		Class<?> fieldClazz = field.getType();
		if (fieldClazz.isPrimitive() || String.class.isAssignableFrom(fieldClazz)) {
			flag = true;
		}
		return flag;
	}

	private Object getter(T row, Field field) {
		Object result = null;
		Method method = null;
		try {
			String methodName="";
			if (boolean.class.isAssignableFrom(field.getType())) {
				methodName=field.getName();
			}else {
				methodName="get" + StringUtils.capitalize(field.getName());
			}
			method = row.getClass().getMethod(methodName);
			try {
				result = method.invoke(row);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			// (throw exception if the corresponding getter method not exist)
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}		
		return result;

	}
}
