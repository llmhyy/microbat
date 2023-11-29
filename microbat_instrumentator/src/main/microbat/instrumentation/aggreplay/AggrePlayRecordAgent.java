package microbat.instrumentation.aggreplay;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import microbat.instrumentation.Agent;
import microbat.instrumentation.CommandLine;

public class AggrePlayRecordAgent extends Agent {

	private CommandLine commandLine;
	
	public AggrePlayRecordAgent(CommandLine commandLine) {
		this.commandLine = commandLine;
	}
	
	
	public int getNumThreads() {
		return commandLine.getInt("numThreads", 1);
	}
	
	@Override
	public void startup0(long vmStartupTime, long agentPreStartup) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startTest(String junitClass, String junitMethod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishTest(String junitClass, String junitMethod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ClassFileTransformer getTransformer0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void retransformBootstrapClasses(Instrumentation instrumentation, Class<?>[] retransformableClasses)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTest(String testResultMsg, String junitClass, String junitMethod, long threadId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInstrumentationActive0() {
		// TODO Auto-generated method stub
		return false;
	}

}
