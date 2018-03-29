package microbat.instrumentation.filter;

import java.util.HashSet;
import java.util.Set;

public class JdkFilter {
	private static final Set<String> jdkExclusives;
	
	static {
		jdkExclusives = new HashSet<>();
		jdkExclusives.add(Integer.class.getName());
		jdkExclusives.add(Boolean.class.getName());
		jdkExclusives.add(Float.class.getName());
		jdkExclusives.add(Character.class.getName());
		jdkExclusives.add(Double.class.getName());
		jdkExclusives.add(Long.class.getName());
		jdkExclusives.add(Short.class.getName());
		jdkExclusives.add(Byte.class.getName());
		jdkExclusives.add(Object.class.getName());
		jdkExclusives.add(String.class.getName());
		jdkExclusives.add("java.lang.Thread");
		jdkExclusives.add("java.lang.ThreadLocal");
		jdkExclusives.add("java.lang.Error");
//		jdkExclusives.add("java.lang.Throwable");
		jdkExclusives.add("java.lang.AssertionError");
		jdkExclusives.add("java.lang.Class");
	}

	public static boolean filter(String className) {
		return !jdkExclusives.contains(className);
	}
	
}
