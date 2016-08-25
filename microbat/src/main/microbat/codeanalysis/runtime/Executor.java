package microbat.codeanalysis.runtime;

/**
 * This class is used to locate all the executor classes in this project
 * @author "linyun"
 *
 */
public abstract class Executor {
	
	protected int steps = 0;
	
	public static final int TIME_OUT = 10000;
	
	protected String[] stepWatchExcludes = { "java.*", "java.lang.*", "javax.*", "sun.*", "com.sun.*", 
			"org.junit.*", "junit.*", "junit.framework.*", "org.hamcrest.*", "org.hamcrest.core.*", "org.hamcrest.internal.*"};
}
