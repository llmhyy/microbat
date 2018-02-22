package microbat.agent;

import org.junit.Test;

import microbat.Activator;
import microbat.instrumentation.AgentParams;
import microbat.model.trace.Trace;
import microbat.util.IResourceUtils;
import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.common.core.utils.ClassUtils;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.vm.VMConfiguration;

public class TraceConstructor {
	
	/**
	 * test
	 */
	@Test
	public void test() throws SavException {
		String jarPath = "E:/lyly/Projects/microbat/master/microbat_instrumentator/src/resources/instrumentator.jar";
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath);
		VMConfiguration config = new VMConfiguration();
		config.setJavaHome("E:/lyly/Tools/jdk/jdk1.7.0_80");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/org.hamcrest.core.jar");
		config.addClasspath("E:/linyun/bug_repo/Chart/1/bug/build-tests");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/linyun/bug_repo/Chart/1/bug/lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		config.addClasspath("E:/linyun/bug_repo/Chart/1/bug/lib/servlet.jar");
		config.addClasspath("E:/linyun/bug_repo/Chart/1/bug/lib/iText-2.1.4.jar");
		config.addClasspath("E:/linyun/bug_repo/Chart/1/bug/build");
		
		String junitClass = "org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests";
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs("test2947660");
		config.setNoVerify(true);
		agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		agentRunner.addAgentParam("java_home", config.getJavaHome());
		agentRunner.addAgentParam("class_path", config.getClasspathStr());
		agentRunner.addAgentParam("working_dir", "E:/linyun/bug_repo/Chart/1/bug");
		long start = System.currentTimeMillis();
		agentRunner.runWithDumpFileOption(config, null);
//		agentRunner.runWithSocket(config);
		Trace trace = agentRunner.getTrace();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}
	
	
	public Trace getTrace(AppJavaClassPath appClassPath) throws Exception {
		String instrumentationJarPath = IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "lib")
				+ "/instrumentation.jar";

		TraceAgentRunner agentRunner = new TraceAgentRunner(instrumentationJarPath);
		VMConfiguration config = new VMConfiguration(appClassPath);
		config.setLaunchClass(appClassPath.getLaunchClass());
		config.setNoVerify(true);
		agentRunner.addAgentParam("entry_point",
				ClassUtils.toClassMethodStr(appClassPath.getOptionalTestClass(), appClassPath.getOptionalTestMethod()));
		agentRunner.startVm(config);
		Trace trace = agentRunner.getTrace();
		if (trace == null) {
			throw new SavRtException("Cannot build trace");
		}
		return trace;
	}
}
