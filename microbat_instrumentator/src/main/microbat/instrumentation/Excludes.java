package microbat.instrumentation;

public class Excludes {
	public static String[] defaultLibExcludes = { 
			"microbat.*",
			"java.awt.*",
			"java.applet.*", 
			"java.lang.*",
			"java.beans.*", 
			"java.io.*", 
			"java.math.*", 
			"java.net.*", 
			"java.nio.*", 
			"java.rmi.*",
			"java.security.*", 
			"java.sql.*", 
			"java.text.*", 
			"java.util.*",
			"javax.*", 
			"sun.*", 
			"com.sun.*", 
			"com.oracle.*",
			"org.ietf.*",
			"org.omg.*",
			"org.jcp.*",
			"org.w3c.*",
			"org.xml.*",
			"sunw.*",
			"org.junit.*", "junit.*", "junit.framework.*", "org.hamcrest.*", "org.hamcrest.core.*", "org.hamcrest.internal.*",
			"jdk.*", "jdk.internal.*", "org.GNOME.Accessibility.*"
			};
	
	public static String[] defaultLibExcludePrefixes;
	static {
		defaultLibExcludePrefixes = new String[defaultLibExcludes.length];
		for (int i = 0; i < defaultLibExcludes.length; i++) {
			String exclude = defaultLibExcludes[i];
			defaultLibExcludePrefixes[i] = exclude.substring(0, exclude.length() - 1); // 1 = "*".length
		}
	}
	public static boolean isExcluded(String className) {
		for (String excludePrefix : defaultLibExcludePrefixes) {
			if (className.startsWith(excludePrefix)) {
				return true;
			}
		}
		return false;
	}
}
