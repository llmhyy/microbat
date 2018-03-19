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
	}

	public static boolean filter(String className) {
		return !jdkExclusives.contains(className);
	}
	
}
