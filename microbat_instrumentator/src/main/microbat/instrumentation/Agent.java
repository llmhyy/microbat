package microbat.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.AgentParams.LogType;
import microbat.instrumentation.cfgcoverage.CoverageAgent;
import microbat.instrumentation.cfgcoverage.CoverageAgentParams;
import microbat.instrumentation.filter.FilterChecker;

/**
 * @author LLT
 */
public class Agent {
	private static IAgent agent;
	private static String programMsg = "";
	private volatile static Boolean shutdowned = false;
	private static int numberOfThread = 1;
	private static Instrumentation instrumentation;
	
	public Agent(CommandLine cmd, Instrumentation inst) {
		if (cmd.getBoolean(CoverageAgentParams.OPT_IS_COUNT_COVERAGE, false)) {
			agent = new CoverageAgent(cmd);
		} else if (cmd.getBoolean(AgentParams.OPT_PRECHECK, false)) {
			agent = new PrecheckAgent(cmd);
		} else {
			agent = new TraceAgent(cmd);
		}
		instrumentation = inst;
		AgentLogger.setup(LogType.valuesOf(cmd.getStringList(AgentParams.OPT_LOG)));
	}

	public void startup() {
		agent.startup();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Agent.stop();
			}
		});
	}

	public static void _exitProgram(String programMsg) {
		Agent.programMsg = programMsg;
		stop();
		Runtime.getRuntime().exit(1); // force program to exit to avoid getting stuck by background running threads.
	}
	
	public static void _exitTest(String testResultMsg, String junitClass, String junitMethod) {
		agent.exitTest(testResultMsg, junitClass, junitMethod);
	}
	
	public static String getProgramMsg() {
		return programMsg;
	}
	
	public static synchronized void stop() {
		synchronized (shutdowned) {
			try {
				if (!shutdowned) {
					instrumentation.removeTransformer(agent.getTransformer());
					Class<?>[] retransformableClasses = getRetransformableClasses(instrumentation);
					if (retransformableClasses != null) {
						instrumentation.retransformClasses(retransformableClasses);
					}
					agent.shutdown();
				}
				shutdowned = true;
			} catch (Throwable e) {
				AgentLogger.error(e);
				shutdowned = true;
			}
		}
	}
	
	private static Class<?>[] getRetransformableClasses(Instrumentation inst) {
		AgentLogger.debug("Collect classes to reset instrumentation....");
		List<Class<?>> candidates = new ArrayList<Class<?>>();
		List<String> bootstrapIncludes = FilterChecker.getInstance().getBootstrapIncludes();
		List<String> includedLibraryClasses = FilterChecker.getInstance().getIncludedLibraryClasses();
		if (bootstrapIncludes.isEmpty() && includedLibraryClasses.isEmpty()) {
			return null;
		}
		Class<?>[] classes = inst.getAllLoadedClasses();
		for (Class<?> c : classes) {
			if (bootstrapIncludes.contains(c.getName().replace(".", "/"))
					|| includedLibraryClasses.contains(c.getName())) {
				if (inst.isModifiableClass(c) && inst.isRetransformClassesSupported() && !ClassLoader.class.equals(c)) {
					candidates.add(c);
				}
			}
		}
		AgentLogger.debug(candidates.size() + " retransformable candidates");
		if (candidates.isEmpty()) {
			return null;
		}
		return candidates.toArray(new Class<?>[candidates.size()]);
	}

	public static void _startTest(String junitClass, String junitMethod) {
		try {
			agent.startTest(junitClass, junitMethod);
		} catch (Throwable e) {
			AgentLogger.error(e);
		}
	}
	
	public static void _finishTest(String junitClass, String junitMethod) {
		try {
			agent.finishTest(junitClass, junitMethod);
		} catch (Throwable e) {
			AgentLogger.error(e);
		}
	}

	public static String extractJarPath() {
		return null;
	}

	public static void _onStartThread() {
		if (!shutdowned) {
//			boolean isCalledFromApp = false;
//			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//			boolean threadStartReached = false;
//			for (StackTraceElement ste : stackTrace) {
//				if ("java.lang.Thread".equals(ste.getClassName()) 
//						&& "start".equals(ste.getMethodName())) {
//					threadStartReached = true;
//				} else if (threadStartReached) {
//					isCalledFromApp = FilterChecker.isAppClazz(ste.getClassName());
//					break;
//				}
//			}
//			if (isCalledFromApp) {
//				numberOfThread++;
//			}
			numberOfThread++;
		}
	}
	
	public static int getNumberOfThread() {
		return numberOfThread;
	}

	public ClassFileTransformer getTransformer() {
		return agent.getTransformer();
	}

	public void retransformClasses(Class<?>[] retransformableClasses) throws Exception {
		agent.retransformBootstrapClasses(instrumentation, retransformableClasses);
	}

	public static boolean isInstrumentationActive() {
		return agent.isInstrumentationActive();
	}
}
