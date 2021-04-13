package microbat.examples.instrumentator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import microbat.agent.TraceAgentRunner;
import microbat.examples.benchmark.Benchmark;
import microbat.examples.benchmark.test.BenchmarkTest;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.AgentParams.LogType;
import microbat.instrumentation.filter.CodeRangeEntry;
import microbat.model.trace.Trace;
import sav.common.core.SavException;
import sav.commons.TestConfiguration;
import sav.strategies.vm.VMConfiguration;

public class ExampleTraceConstructor {
	private static final String EXAMPLE_PRJ_ROOT = System.getProperty("user.dir");
	private int debugPort = 9595;
	
	@Test
	public void runExampleCodeRange() throws Exception {
		String junitClass = BenchmarkTest.class.getName();
		String testMethod = "test1";
		TraceAgentRunner agentRunner = setupAgentRunner(junitClass, testMethod);
		List<CodeRangeEntry> entries = new ArrayList<>();
		entries.add(new CodeRangeEntry(Benchmark.class.getName(), 5, 7));
		entries.add(new CodeRangeEntry(Benchmark.class.getName(), 15, 15));
		agentRunner.addAgentParams(AgentParams.OPT_CODE_RANGE, entries);
		execute(agentRunner);
	}
	
	public void runBenchmark() throws Exception {
		String junitClass = BenchmarkTest.class.getName();
		String testMethod = "test1";
		TraceAgentRunner agentRunner = setupAgentRunner(junitClass, testMethod);
		execute(agentRunner);
	}

	private void execute(TraceAgentRunner agentRunner) throws SavException {
		long start = System.currentTimeMillis();
		agentRunner.runWithDumpFileOption(null);
		Trace trace = agentRunner.getTraces().get(0);
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("trace length: " + trace.size());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}

	private TraceAgentRunner setupAgentRunner(String junitClass, String testMethod) {
		String jarPath = EXAMPLE_PRJ_ROOT + "/resources/instrumentator.jar";
		String libsFolder = EXAMPLE_PRJ_ROOT.replace("microbat_examples", "microbat_instrumentator/lib");
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		config.setJavaHome(TestConfiguration.getJavaHome());
		
		/* debug */
//		agentRunner.setVmDebugPort(debugPort);
		
		config.addClasspath(EXAMPLE_PRJ_ROOT + "/bin");
		config.addClasspath( EXAMPLE_PRJ_ROOT + "/resources/testrunner.jar");
		config.addClasspath(libsFolder + "/hamcrest-core-1.3.jar");
		config.addClasspath(libsFolder + "/junit-4.11.jar");
		config.setWorkingDirectory(EXAMPLE_PRJ_ROOT);
		
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs(testMethod);
		config.setNoVerify(true);
		agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		agentRunner.addAgentParam(AgentParams.OPT_JAVA_HOME, config.getJavaHome());
		agentRunner.addAgentParams(AgentParams.OPT_CLASS_PATH, config.getClasspaths());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		List<String> includes = new ArrayList<>();
		agentRunner.addIncludesParam(includes);
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, 100000);
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(LogType.printProgress, LogType.info,
				LogType.debug, LogType.error));
		return agentRunner;
	}
	
}
