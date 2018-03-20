package microbat.instrumentation;

import java.lang.instrument.ClassFileTransformer;

public interface IAgent {
	
	public void startup();
	
	public void shutdown() throws Exception;
	
	public void startTest(String junitClass, String junitMethod);
	
	public void finishTest(String junitClass, String junitMethod);
	
	public ClassFileTransformer getTransformer();

	public void setTransformableClasses(Class<?>[] retransformableClasses);
	
}
