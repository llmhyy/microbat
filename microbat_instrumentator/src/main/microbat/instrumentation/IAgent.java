package microbat.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public interface IAgent {

	public void startup(long vmStartupTime, long agentPreStartup);

	public void shutdown() throws Exception;

	public void startTest(String junitClass, String junitMethod);

	public void finishTest(String junitClass, String junitMethod);

	public ClassFileTransformer getTransformer();

	public void retransformBootstrapClasses(Instrumentation instrumentation, Class<?>[] retransformableClasses)
			throws Exception;

	public void exitTest(String testResultMsg, String junitClass, String junitMethod, long threadId);

	public boolean isInstrumentationActive();

}
