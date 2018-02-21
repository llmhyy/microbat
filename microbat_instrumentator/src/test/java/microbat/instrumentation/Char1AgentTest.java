package microbat.instrumentation;

import java.util.ArrayList;

import org.junit.Test;

import sav.common.core.SavException;
import sav.commons.TestConfiguration;
import sav.strategies.vm.AgentVmRunner;
import sav.strategies.vm.VMConfiguration;

public class Char1AgentTest extends AgentTest {

	@Test
	public void runChar1() throws SavException {
		AgentVmRunner vmRunner = new AgentVmRunner(JAR_PATH, AgentConstants.AGENT_OPTION_SEPARATOR,
				AgentConstants.AGENT_PARAMS_SEPARATOR);
		VMConfiguration config = new VMConfiguration();
		config.setJavaHome(TestConfiguration.getJavaHome());
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
		config.setDebug(true);
		config.setPort(9595);
		config.setNoVerify(true);
		vmRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		vmRunner.addAgentParam("java_home", config.getJavaHome());
		vmRunner.addAgentParam("class_path", config.getClasspathStr());
		vmRunner.addAgentParam("working_dir", "E:/linyun/bug_repo/Chart/1/bug");
		
//		vmRunner.startVm(config);
		System.out.println(vmRunner.getCommandLinesString(config));
	}
	
	@Test
	public void runCom0Test() throws SavException {
		AgentVmRunner vmRunner = new AgentVmRunner(JAR_PATH, AgentConstants.AGENT_OPTION_SEPARATOR,
				AgentConstants.AGENT_PARAMS_SEPARATOR);
		VMConfiguration config = new VMConfiguration();
		config.setJavaHome(TestConfiguration.getJavaHome());
		config.addClasspath("E:/lyly/workspace/microbat_instrumentation/instrument.test/bin");
		config.addClasspath("E:/linyun/bug_repo/Chart/1/bug/build-tests");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/linyun/bug_repo/Chart/1/bug/lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		
		String launchClass = "com0.Test";
		config.setLaunchClass(launchClass);
		config.setDebug(true);
		config.setPort(9595);
		config.setNoVerify(true);
		vmRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, launchClass);
		vmRunner.addAgentParam(AgentParams.OPT_ENTRY_POINT, launchClass + ".main");
		vmRunner.addAgentParam(AgentParams.OPT_INCLUDES, ArrayList.class.getName());
		vmRunner.addAgentParam("java_home", config.getJavaHome());
		vmRunner.addAgentParam("class_path", config.getClasspathStr());
		vmRunner.addAgentParam("working_dir", "E:/lyly/workspace/microbat_instrumentation/instrument.test");
		
//		vmRunner.startVm(config);
		System.out.println(vmRunner.getCommandLinesString(config));
	}
}
