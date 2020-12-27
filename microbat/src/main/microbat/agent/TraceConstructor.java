package microbat.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import microbat.Activator;
import microbat.instrumentation.AgentParams;
import microbat.instrumentation.AgentParams.LogType;
import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import microbat.model.trace.Trace;
import microbat.util.IResourceUtils;
import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.SingleTimer;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.vm.VMConfiguration;

public class TraceConstructor {
	
	/**
	 * test
	 */
	@Test
	public void test() throws SavException {
		String jarPath = "E:/lyly/Projects/microbat/master/microbat_instrumentator/src/resources/instrumentator.jar";
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
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
		agentRunner.runWithDumpFileOption(null);
//		agentRunner.runWithSocket(config);
		Trace trace = agentRunner.getMainTrace();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}
	
	
	public Trace getTrace(AppJavaClassPath appClassPath) throws Exception {
		String instrumentationJarPath = IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "lib")
				+ "/instrumentation.jar";

		VMConfiguration config = new VMConfiguration(appClassPath);
		TraceAgentRunner agentRunner = new TraceAgentRunner(instrumentationJarPath, config);
		config.setLaunchClass(appClassPath.getLaunchClass());
		config.setNoVerify(true);
		agentRunner.addAgentParam("entry_point",
				ClassUtils.toClassMethodStr(appClassPath.getOptionalTestClass(), appClassPath.getOptionalTestMethod()));
		agentRunner.startVm(config);
		Trace trace = agentRunner.getMainTrace();
		if (trace == null) {
			throw new SavRtException("Cannot build trace");
		}
		return trace;
	}
	
	@Test
	public void executeMultipleTimes() {
		int total = 1;
		int fail = 0;
		List<Integer> steps = new ArrayList<>();
		for (int i = 0; i < total; i++) {
			try {
				int size = runLang13();
				if (size < 5000) {
					break;
				}
			} catch (Exception e) {
				fail++;
			}
		}
		System.out.println(String.format("Fail over total: %d/%d", fail, total));
		System.out.println("Step total: " + steps);
	}
	
	public int runClosure11() throws Exception {
		String jarPath = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/instrumentator.jar";
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		config.setJavaHome("E:/lyly/Tools/jdk/jdk1.7.0_80");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/build/test");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/build/classes");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/ant-launcher.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/ant.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/args4j.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/caja-r4314.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/guava.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/jarjar.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/json.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/jsr305.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/junit.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/protobuf-java.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/lib/rhino/testsrc/org/mozilla/javascript/tests/commonjs/module/modules.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/build/lib/rhino.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/11/fix/build/lib/rhino1_7R4pre/js.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/org.hamcrest.core.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/bcel-6.0.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/javassist.jar");
		config.setWorkingDirectory("E:/linyun/bug_repo/Closure/11/fix");
		String junitClass = "com.google.javascript.jscomp.TypeCheckTest";
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs("testGetprop4");
		config.setNoVerify(true);
		agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		agentRunner.addAgentParam(AgentParams.OPT_JAVA_HOME, config.getJavaHome());
		agentRunner.addAgentParams(AgentParams.OPT_CLASS_PATH, config.getClasspaths());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, 1000000);
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(LogType.printProgress, LogType.info,
				LogType.debug, LogType.error));
		long start = System.currentTimeMillis();
		agentRunner.runWithDumpFileOption("E:/lyly/fix.exec");
		Trace info = agentRunner.getMainTrace();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("trace length: " + info.size());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
		return info.size();
	}
	
	public int runClosure82() throws Exception {
		String jarPath = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/instrumentator.jar";
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		config.setJavaHome("E:/lyly/Tools/jdk/jdk1.7.0_80");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/build/test");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/build/classes");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/ant-launcher.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/ant.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/args4j.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/caja-r4314.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/guava.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/json.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/jsr305.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/junit.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/libtrunk_rhino_parser_jarjared.jar");
		config.addClasspath("E:/linyun/bug_repo/Closure/82/bug/lib/protobuf-java.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/org.hamcrest.core.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/bcel-6.0.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/javassist.jar");
		config.setWorkingDirectory("E:/linyun/bug_repo/Closure/82/bug");
		String junitClass = "com.google.javascript.rhino.jstype.FunctionTypeTest";
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs("testEmptyFunctionTypes");
		config.setNoVerify(true);
		agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		agentRunner.addAgentParam(AgentParams.OPT_JAVA_HOME, config.getJavaHome());
		agentRunner.addAgentParams(AgentParams.OPT_CLASS_PATH, config.getClasspaths());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, 100000);
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(LogType.printProgress, LogType.info,
				LogType.debug, LogType.error));
		long start = System.currentTimeMillis();
		agentRunner.precheck("E:/lyly/bugPrecheck.info");
		PrecheckInfo info = agentRunner.getPrecheckInfo();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("trace length: " + info.getStepTotal());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
		return info.getStepTotal();
	}
	
	public int runMath100fix() throws Exception {
		String jarPath = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/instrumentator.jar";
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		config.setJavaHome("E:/lyly/Tools/jdk/jdk1.7.0_80");
		config.addClasspath("E:/linyun/bug_repo/Math/100/fix/target/test-classes");
		config.addClasspath("E:/linyun/bug_repo/Math/100/fix/target/classes");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/org.hamcrest.core.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/bcel-6.0.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/javassist.jar");
		config.setWorkingDirectory("E:/linyun/bug_repo/Math/100/fix");
		String junitClass = "org.apache.commons.math.estimation.GaussNewtonEstimatorTest";
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs("testBoundParameters");
		config.setNoVerify(true);
		agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		agentRunner.addAgentParam(AgentParams.OPT_JAVA_HOME, config.getJavaHome());
		agentRunner.addAgentParams(AgentParams.OPT_CLASS_PATH, config.getClasspaths());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, 100000);
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(LogType.printProgress, LogType.info,
				LogType.debug, LogType.error));
		long start = System.currentTimeMillis();
		agentRunner.precheck("E:/lyly/bugPrecheck.info");
		PrecheckInfo info = agentRunner.getPrecheckInfo();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("trace length: " + info.getStepTotal());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
		return info.getStepTotal();
	}
	
	public int runMath100buggy() throws Exception {
		String jarPath = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/instrumentator.jar";
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		config.setJavaHome("E:/lyly/Tools/jdk/jdk1.7.0_80");
		config.addClasspath("E:/linyun/bug_repo/Math/100/bug/target/test-classes");
		config.addClasspath("E:/linyun/bug_repo/Math/100/bug/target/classes");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/org.hamcrest.core.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/bcel-6.0.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/javassist.jar");
		config.setWorkingDirectory("E:/linyun/bug_repo/Math/100/bug");
		String junitClass = "org.apache.commons.math.estimation.GaussNewtonEstimatorTest";
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs("testBoundParameters");
		config.setNoVerify(true);
		agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		agentRunner.addAgentParam(AgentParams.OPT_JAVA_HOME, config.getJavaHome());
		agentRunner.addAgentParams(AgentParams.OPT_CLASS_PATH, config.getClasspaths());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, 100000);
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(LogType.printProgress, LogType.info,
				LogType.debug, LogType.error));
		long start = System.currentTimeMillis();
		agentRunner.precheck("E:/lyly/bugPrecheck.info");
		PrecheckInfo info = agentRunner.getPrecheckInfo();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("trace length: " + info.getStepTotal());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
		return info.getStepTotal();
	}
	
	public void runMockito6() throws Exception {
		String jarPath = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/instrumentator.jar";
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		config.setJavaHome("E:/lyly/Tools/jdk/jdk1.7.0_80");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/build/classes/test");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/build/classes/main");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/build/ant-googlecode-0.0.3.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/build/ant4hg-V0.07.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/build/asm-3.1.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/build/bnd-0.0.313.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/build/jarjar-1.0.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/build/jaxen-1.1.1.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/build/maven-ant-tasks-2.0.9.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/build/pmd-4.1.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/build/sorcerer.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/repackaged/cglib-and-asm-1.0.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/run/com.springsource.org.hamcrest.core-1.1.0.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/run/objenesis-2.1.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/sources/cglib-and-asm-1.0-sources.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/sources/com.springsource.org.hamcrest.core-1.1.0-sources.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/sources/objenesis-2.1-sources.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/test/fest-assert-1.3.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/test/fest-util-1.1.4.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/lib/test/powermock-reflect-1.2.5.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/6/bug/build/libs/mockito-core-2.0.0-beta.jar");
		config.addClasspath("E:/linyun/bug_repo/Mockito/lib/objenesis-1.2.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/org.hamcrest.core.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/bcel-6.0.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/javassist.jar");
		config.setWorkingDirectory("E:/linyun/bug_repo/Mockito/6/bug");
		String junitClass = "org.mockitousage.matchers.AnyXMatchersAcceptNullsTest";
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs("shouldNotAcceptNullInAllAnyPrimitiveWrapperMatchers");
		config.setNoVerify(true);
		agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		agentRunner.addAgentParam(AgentParams.OPT_JAVA_HOME, config.getJavaHome());
		agentRunner.addAgentParams(AgentParams.OPT_CLASS_PATH, config.getClasspaths());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		List<String> includes = new ArrayList<>();
		includes.add("java.lang.Object[]");
		includes.add("java.lang.Boolean");
		includes.add("org.mockito.internal.creation.cglib.MethodInterceptorFilter");
		includes.add("org.mockito.internal.util.ObjectMethodsGuru");
		includes.add("java.lang.reflect.AccessibleObject");
		includes.add("java.lang.reflect.Method");
		includes.add("java.lang.Class[]");
		includes.add("java.lang.Class");
		agentRunner.addIncludesParam(includes);
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, 100000);
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(LogType.printProgress, LogType.info,
				LogType.debug, LogType.error));
		long start = System.currentTimeMillis();
		agentRunner.runWithDumpFileOption("E:/lyly/eclipse-java-mars-clean/eclipse-for-unmodified-code/trace/Mockito/6/fix.exec");
		Trace trace = agentRunner.getMainTrace();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("trace length: " + trace.size());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}
	
	public int runLang13() throws Exception {
		String jarPath = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/instrumentator.jar";
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		config.setJavaHome("E:/lyly/Tools/jdk/jdk1.7.0_80");
		config.addClasspath("E:/linyun/bug_repo/Lang/13/bug/target/tests");
		config.addClasspath("E:/linyun/bug_repo/Lang/13/bug/target/classes");
		config.addClasspath("E:/linyun/bug_repo/Lang/lib/cglib-nodep-2.2.2.jar");
		config.addClasspath("E:/linyun/bug_repo/Lang/lib/commons-io-2.4.jar");
		config.addClasspath("E:/linyun/bug_repo/Lang/lib/easymock-3.1.jar");
		config.addClasspath("E:/linyun/bug_repo/Lang/lib/objenesis-1.2.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/org.hamcrest.core.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/bcel-6.0.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/javassist.jar");
		config.setWorkingDirectory("E:/linyun/bug_repo/Lang/13/bug");
		String junitClass = "org.apache.commons.lang3.SerializationUtilsTest";
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs("testPrimitiveTypeClassSerialization");
		config.setNoVerify(true);
		agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		agentRunner.addAgentParam(AgentParams.OPT_JAVA_HOME, config.getJavaHome());
		agentRunner.addAgentParams(AgentParams.OPT_CLASS_PATH, config.getClasspaths());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		List<String> includes = new ArrayList<>();
		includes.add("java.io.ObjectStreamClass"); 
		includes.add("java.io.ObjectInputStream"); 
		includes.add("java.io.InputStream"); 
		includes.add("org.apache.commons.lang3.SerializationUtils$ClassLoaderAwareObjectInputStream"); 
		includes.add("java.io.Serializable"); 
		includes.add("java.lang.RuntimeException"); 
		includes.add("java.lang.Exception"); 
		includes.add("java.lang.Throwable"); 
		includes.add("org.apache.commons.lang3.SerializationException"); 
		includes.add("java.lang.Thread"); 
		includes.add("java.lang.ClassLoader"); 
		includes.add("java.lang.System"); 
		includes.add("sun.reflect.Reflection"); 
		includes.add("java.lang.Class"); 
		includes.add("java.security.BasicPermission"); 
		includes.add("java.security.Permission"); 
		includes.add("java.lang.RuntimePermission"); 
		includes.add("java.lang.SecurityManager"); 
		includes.add("java.lang.ReflectiveOperationException"); 
		includes.add("java.lang.ClassNotFoundException"); 
		includes.add("java.io.ByteArrayInputStream"); 
		includes.add("java.util.Map"); 
		includes.add("java.util.Properties"); 
		includes.add("java.util.WeakHashMap"); 
		includes.add("java.util.HashMap"); 
		includes.add("java.util.jar.Attributes"); 
		includes.add("java.util.Collections$EmptyMap"); 
		includes.add("java.util.Hashtable"); 
		includes.add("java.util.AbstractMap"); 
		includes.add("java.lang.ClassValue$ClassValueMap"); 
		includes.add("java.util.LinkedHashMap"); 
		includes.add("java.util.Collections$SingletonMap"); 
		includes.add("java.io.ObjectInputStream$BlockDataInputStream"); 
		includes.add("java.io.ObjectInputStream$HandleTable"); 
		includes.add("java.io.ObjectInputStream$ValidationList");
		agentRunner.addIncludesParam(includes);
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, 100000);
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(LogType.printProgress, LogType.info,
				LogType.debug, LogType.error));
		long start = System.currentTimeMillis();
		agentRunner.runWithDumpFileOption(null);
		Trace trace = agentRunner.getMainTrace();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("trace length: " + trace.size());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
		return trace.size();
	}
	
	public void runLang56() throws Exception {
		String jarPath = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/instrumentator.jar";
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		config.setJavaHome("E:/lyly/Tools/jdk/jdk1.7.0_80");
		config.addClasspath("E:/linyun/bug_repo/Lang/56/fix/target/tests");
		config.addClasspath("E:/linyun/bug_repo/Lang/56/fix/target/classes");
		config.addClasspath("E:/linyun/bug_repo/Lang/lib/cglib-nodep-2.2.2.jar");
		config.addClasspath("E:/linyun/bug_repo/Lang/lib/commons-io-2.4.jar");
		config.addClasspath("E:/linyun/bug_repo/Lang/lib/easymock-3.1.jar");
		config.addClasspath("E:/linyun/bug_repo/Lang/lib/objenesis-1.2.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/org.hamcrest.core.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/bcel-6.0.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/javassist.jar");
		config.setWorkingDirectory("E:/linyun/bug_repo/Lang/56/fix");
		String junitClass = "org.apache.commons.lang.time.FastDateFormatTest";
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs("testLang303");
		config.setNoVerify(true);
		agentRunner.addAgentParam(AgentParams.OPT_LAUNCH_CLASS, junitClass);
		agentRunner.addAgentParam(AgentParams.OPT_JAVA_HOME, config.getJavaHome());
		agentRunner.addAgentParams(AgentParams.OPT_CLASS_PATH, config.getClasspaths());
		agentRunner.addAgentParam(AgentParams.OPT_WORKING_DIR, config.getWorkingDirectory());
		/* build includes & excludes params */
		List<String> includes = new ArrayList<>();
		includes.add("java.lang.Exception");
		includes.add("java.lang.Throwable");
		includes.add("java.io.IOException");
		includes.add("java.io.OutputStream");
		includes.add("java.io.ObjectOutputStream");
		includes.add("java.io.Serializable");
		includes.add("java.io.ObjectOutputStream$BlockDataOutputStream");
		includes.add("java.io.ObjectOutputStream$HandleTable");
		includes.add("java.io.ObjectOutputStream$ReplaceTable");
		includes.add("java.io.ObjectOutputStream$DebugTraceInfoStack");
		agentRunner.addIncludesParam(includes);
		agentRunner.addAgentParam(AgentParams.OPT_STEP_LIMIT, 100000);
		agentRunner.addAgentParams(AgentParams.OPT_LOG, Arrays.asList(LogType.printProgress, LogType.info,
				LogType.debug, LogType.error));
		long start = System.currentTimeMillis();
		agentRunner.runWithDumpFileOption(null);
		Trace trace = agentRunner.getMainTrace();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("trace length: " + trace.size());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}
	
	@Test
	public void runBenchmark() throws Exception {
		String jarPath = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/instrumentator.jar";
		VMConfiguration config = new VMConfiguration();
		TraceAgentRunner agentRunner = new TraceAgentRunner(jarPath, config);
		config.setJavaHome("E:/lyly/Tools/jdk/jdk1.7.0_80");
		config.addClasspath("E:/lyly/Projects/TestData/learntest-benchmark/master/benchmark/bin");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/junit.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/org.hamcrest.core.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/testrunner.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/bcel-6.0.jar");
		config.addClasspath("E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/javassist.jar");
		config.setWorkingDirectory("E:/lyly/Projects/TestData/learntest-benchmark/master/benchmark");
		String junitClass = "testdata.learntest.benchmark.test1.Benchmark2";
		config.setLaunchClass("microbat.evaluation.junit.MicroBatTestRunner");
		config.addProgramArgs(junitClass);
		config.addProgramArgs("test1");
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
		long start = System.currentTimeMillis();
		agentRunner.runWithDumpFileOption(null);
		Trace trace = agentRunner.getMainTrace();
		System.out.println("isTestSuccessful? " + agentRunner.isTestSuccessful());
		System.out.println("testFailureMessage: " + agentRunner.getTestFailureMessage());
		System.out.println("trace length: " + trace.size());
		System.out.println("finish!");
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}
	
	@Test
	public void loadTrace() {
		SingleTimer timer = SingleTimer.start("loadTrace");
		RunningInfo runningInfo = RunningInfo
				.readFromFile("E:/lyly/bug.exec");
		System.out.println(timer.getResult());
//		int maxR = -1;
//		int maxW = -1;
//		for (TraceNode node : runningInfo.getTrace().getExecutionList()) {
//			System.out.print(node.getOrder() + ": ");
//			System.out.println("R= " + node.getReadVariables().size() + ", W= " + node.getWrittenVariables().size());
//			if (maxR < node.getReadVariables().size()) {
//				maxR = node.getReadVariables().size();
//			}
//			if (maxW < node.getWrittenVariables().size()) {
//				maxW = node.getWrittenVariables().size();
//			}
//		}
//		System.out.println("maxR=" + maxR + ", maxW=" + maxW);
		System.out.println(runningInfo);
		System.out.println(timer.getResult());
	}
}
