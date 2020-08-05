package microbat.filedb.store.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import microbat.filedb.RecordsFileException;
import microbat.filedb.annotation.Attribute;
import microbat.filedb.annotation.Key;
import microbat.filedb.annotation.RecordType;
import microbat.filedb.utils.ReflectionUtils;

/**
 * @author LLT
 *
 */
public class RType<T> {
	private String recordName;
	private Class<T> clazz;
	private RAttribute<?> key;
	private List<RAttribute<?>> attributes = new ArrayList<>();

	public RType(Class<T> clazz, RTypeFactory rFactory) throws RecordsFileException {
		if (!clazz.isAnnotationPresent(RecordType.class)) {
			throw new RecordsFileException(
					String.format("Object class is not annotated with @RecordType: %s", clazz.getName()));
		}
		this.clazz = clazz;
		RecordType recordType = clazz.getAnnotation(RecordType.class);
		recordName = recordType.name();
		List<Field> fields = ReflectionUtils.getAllRecordFields(clazz);
		for (Field field : fields) {
			boolean isKey = field.isAnnotationPresent(Key.class);
			boolean isAttribute = field.isAnnotationPresent(Attribute.class);
			if (!isKey && !isAttribute) {
				continue;
			}
			RAttribute<?> attribute = rFactory.createAttribute(clazz, field);
			
			if (isKey) {
				key = attribute;
			}
			if (isAttribute) {
				attributes.add(attribute);
			}
		}
		
		if (key == null) {
			lookupKeyFromMethods(rFactory);
		}
		// still cannot find key
		if (key == null) {
			throw new RecordsFileException(String.format("Key is not defined for Recordtype: %s", clazz.getName()));
		}
	}

	private void lookupKeyFromMethods(RTypeFactory rFactory) throws RecordsFileException {
		for (Method method : ReflectionUtils.getAllRecordMethods(clazz)) {
			if (method.isAnnotationPresent(Key.class)) {
				if (!method.getName().startsWith("get")) {
					throw new RecordsFileException(String.format("@Key method must start with 'get': %s.%s",
							clazz.getName(), method.getName()));
				}
				key = rFactory.createKeyAttr(method);
				break;
			}
		}
	}

	public String getRecordName() {
		return recordName;
	}

	public RAttribute<?> getKey() {
		return key;
	}

	public List<RAttribute<?>> getAttributes() {
		return attributes;
	}

	public Class<?> getOwner() {
		return clazz;
	}
}
