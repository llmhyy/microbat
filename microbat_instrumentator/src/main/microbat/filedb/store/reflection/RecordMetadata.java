package microbat.filedb.store.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import microbat.filedb.RecordsFileException;
import microbat.filedb.annotation.Attribute;
import microbat.filedb.annotation.RecordType;
import microbat.filedb.utils.ReflectionUtils;

/**
 * @author LLT
 *
 */
public class RecordMetadata<T> {
	private String recordName;
	private List<Method> getterMethods = new ArrayList<>();

	public RecordMetadata(Class<T> clazz) throws RecordsFileException {
		RecordType recordType = clazz.getAnnotation(RecordType.class);
		recordName = recordType.name();
		try {
			extractMetatdata(clazz);
		} catch (Exception e) {
			throw new RecordsFileException("Fail to extract metadata from Record Type", e);
		}
	}

	private void extractMetatdata(Class<T> clazz) throws Exception {
		if (!clazz.isAnnotationPresent(RecordType.class)) {
			throw new RecordsFileException("Object class is not annotated with @RecordType");
		}
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (!field.isAnnotationPresent(Attribute.class)) {
				continue;
			}
			getterMethods.add(ReflectionUtils.getGetterMethod(clazz, field));
		}
	}

	public String getRecordName() {
		return recordName;
	}

	public List<Method> getGetterMethods() {
		return getterMethods;
	}

}
