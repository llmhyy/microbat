package microbat.instrumentation;

import java.lang.instrument.ClassFileTransformer;

/**
 * 
 * @author LLT
 *  the Agent proxy
 * (Real agent would be TraceAgent & PrecheckAgent)
 */
public class Agent implements IAgent {
	private static IAgent agent;
	private static String programMsg = "";
	private volatile static boolean shutdowned = false;
	
	public Agent(AgentParams agentParams) {
		if (agentParams.isPrecheck()) {
			agent = new PrecheckAgent(agentParams);
		} else {
			agent = new TraceAgent(agentParams);
		}
	}

	public void startup() {
		agent.startup();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void _exitProgram(String programMsg) {
		Agent.programMsg = programMsg;
		try {
			if (!shutdowned) {
				agent.shutdown();
			}
			shutdowned = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Runtime.getRuntime().exit(1); // force program to exit to avoid getting stuck by background running threads.
	}
	
	public static String getProgramMsg() {
		return programMsg;
	}
	
	public void shutdown() throws Exception {
		if (!shutdowned) {
			agent.shutdown();
		}
		shutdowned = true;
	}
	
	public static void _startTest(String junitClass, String junitMethod) {
		agent.startTest(junitClass, junitMethod);
	}
	
	public static void _finishTest(String junitClass, String junitMethod) {
		agent.finishTest(junitClass, junitMethod);
	}

	public static String extractJarPath() {
		return null;
	}
	
	@Override
	public void startTest(String junitClass, String junitMethod) {
		agent.startTest(junitClass, junitMethod);
	}

	@Override
	public void finishTest(String junitClass, String junitMethod) {
		agent.finishTest(junitClass, junitMethod);
	}

	@Override
	public ClassFileTransformer getTransformer() {
		return agent.getTransformer();
	}
}
