package microbat.filedb.store.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microbat.filedb.RecordsFileException;
import microbat.filedb.annotation.RecordType;
import microbat.filedb.utils.ReflectionUtils;
import microbat.instrumentation.utils.CollectionUtils;
import microbat.util.PrimitiveUtils;

/**
 * @author LLT
 * 
 * limited support for types;
 */
public class RTypeFactory {
	private Map<String, RType<?>> map = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public <T>RType<T> create(Class<T> clazz) throws RecordsFileException {
		RType<?> rClass = map.get(clazz.getName());
		if (rClass == null) {
			rClass = new RType<T>(clazz, this);
			map.put(clazz.getName(), rClass);
		}
		return (RType<T>) rClass;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RAttribute<?> createAttribute(Class<?> clazz, Field field) throws RecordsFileException {
		RAttribute attribute = new RAttribute<>(field.getType().getName());
		if (PrimitiveUtils.isPrimitiveTypeOrString(field.getType().getName())) {
			attribute = new RAttribute<Object>(field.getType().getName());
		} else {
			if (CollectionUtils.existIn(field.getType(), List.class, Set.class, Map.class)) {
				attribute = createColAttribute(field);
			} else {
				attribute = new RAttribute(create(field.getType()));
			}
		}
		attribute.setGetter(ReflectionUtils.getGetterMethod(clazz, field));
		attribute.setName(field.getName());
		return attribute;
	}
	
	public RAttribute<?> createKeyAttr(Method method) {
		RAttribute<?> attribute = new RAttribute<>(method.getReturnType().getName());
		attribute.setGetter(method);
		return attribute;
	}

	private RAttribute<?> createColAttribute(Field field) throws RecordsFileException {
		if (!(field.getGenericType() instanceof ParameterizedType)) {
			throw new RecordsFileException("Unsupported collection generic type: " + field.getGenericType());
		}
		ParameterizedType paramType = (ParameterizedType) field.getGenericType();
		List<Object> contentTypes = new ArrayList<>(paramType.getActualTypeArguments().length);
		for (Type actType : paramType.getActualTypeArguments()) {
			if (!(actType instanceof Class<?>)) {
				throw new RecordsFileException("Unsupported collection actualType: " + actType);
			}
			Class<?> actClazz = (Class<?>) actType;
			if (PrimitiveUtils.isPrimitiveTypeOrString(actClazz.getName())) {
				contentTypes.add(actClazz);
			} else if (actClazz.isAnnotationPresent(RecordType.class)) {
				contentTypes.add(create(actClazz));
			} else {
				throw new RecordsFileException("Unsupported collection actualType: " + actType);
			}
		}
		RColAttribute<?> colAttr = new RColAttribute<>(paramType.getRawType().getTypeName());
		colAttr.setContentTypes(contentTypes);
		return colAttr;
	}

	
}
