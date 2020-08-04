package microbat.dbtest;


import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;



public class TestReflect{
	
	@Test
	public <T> void testFiledIsPrimitive() {
		Field[] fields=SampleClass.class.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (field.getName().contains("String")) {
				assertTrue(String.class.isAssignableFrom(field.getType()));
			}	
			else if (field.getName().contains("Int")) {
				assertTrue(field.getType().isPrimitive());
			}
			
			else {
				SampleClass sampleClass =new SampleClass();
				T g=(T) getter(sampleClass, field.getName());
				if (g!=null) {
					System.out.println(getter(g, "name"));
				}			
			}
		}
	}
	
	private <T> Object getter(T row, String name) {
		Object result = null;
		Method method = null;
		try {
			method = row.getClass().getMethod("get" + StringUtils.capitalize(name));
			// (throw exception if the corresponding getter method not exist)
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		try {
			result = method.invoke(row);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

}
