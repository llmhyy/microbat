package microbat.instrumentation.trace.data;

public interface IFilterChecker {

	void startup();

	boolean checkTransformable(String className);

	boolean checkExclusive(String className, String methodName);

}
