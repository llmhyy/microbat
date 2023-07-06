package microbat.log;

public class Log {
	
	public static String genMsg(final String className, final String message) {
		return "[" + className + "]" + message;
 	}
	
	public static void pringMsg(final String className, final String message) {
		System.out.println(Log.genMsg(className, message));
	}
	
	public static String genMsg(final Class<?> clazz,  final String message) {
		final String className = clazz.getSimpleName();
		return "[" + className + "] " + message; 
	}
	
	public static void printMsg(final Class<?> clazz, final String message) {
		System.out.println(Log.genMsg(clazz, message));
	}
	
}
