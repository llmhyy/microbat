/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.junit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.runner.Request;
import org.junit.runner.notification.Failure;

import sav.common.core.Pair;
import sav.common.core.SavException;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.ExecutionTimer;
import sav.common.core.utils.JunitUtils;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.dto.BreakPoint;
import sav.strategies.vm.ProgramArgumentBuilder;
import sav.strategies.vm.VMConfiguration;
import sav.strategies.vm.VMRunner;

/**
 * @author LLT
 *
 */
public class JunitRunner {

	private static Request toRequest(Pair<String, String> pair)
			throws ClassNotFoundException {
		Class<?> junitClass = loadClass(pair.first());
		Method[] methods = junitClass.getMethods();
		for (Method method : methods) {
			if (method.getName().equals(pair.second())) {
				return Request.method(junitClass, method.getName());
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		if (CollectionUtils.isEmpty(args)) {
			System.exit(0);
		}
		JunitRunnerParameters params = JunitRunnerParameters.parse(args);
		JunitResult result = runTestcases(params);
		if (params.getDestfile() != null) {
			File file = new File(params.getDestfile());
			result.save(file, params.isStoreTestResultDetail());
		}
	}

	public static JunitResult runTestcases(JunitRunnerParameters params)
			throws ClassNotFoundException, IOException {
		System.out.println("RunTestcases:");
		List<Pair<String, String>> classMethods = JunitUtils.toPair(params
				.getClassMethods());
		RequestExecution requestExec = new RequestExecution();
		JunitResult result = new JunitResult();
		List<Failure> falures = new ArrayList<Failure>();
		ExecutionTimer executionTimer = getExecutionTimer(params.getTimeout());
		for (Pair<String, String> classMethod : classMethods) {
			Request request = toRequest(classMethod);
			if (request == null) {
				continue;
			}
			requestExec.setRequest(request);
			executionTimer.run(requestExec);
			
			falures.addAll(requestExec.getFailures());
			boolean isPass = requestExec.getResult();
			result.addResult(classMethod, isPass, getLastFailureTrace(requestExec.getFailures()));
		}
		extractBrkpsFromTrace(falures, params, result.getFailureTraces());
		return result;
	}
	
	private static String getLastFailureTrace(List<Failure> failures) {
		Failure lastFailures = CollectionUtils.getLast(failures);
		if (lastFailures == null) {
			return StringUtils.EMPTY;
		}
		StackTraceElement firstStacktraceEle = CollectionUtils
				.getFirstElement(lastFailures.getException().getStackTrace());
		return StringUtils.toStringNullToEmpty(firstStacktraceEle);
	}

	public static ExecutionTimer getExecutionTimer(long timeout) {
		if (timeout > 0) {
			return new ExecutionTimer(timeout);
		} else {
			return new ExecutionTimer(timeout) {
				
				@Override
				public void run(Runnable target) {
					target.run();
				}
			};
		}
	}
	
	private static void extractBrkpsFromTrace(List<Failure> falures,
			JunitRunnerParameters params, Set<BreakPoint> bkps) {
		Set<String> acceptedClasses = new HashSet<String>();
		List<String> testingClassNames = CollectionUtils.initIfEmpty(params
				.getTestingClassNames());
		List<String> testingPkgs = CollectionUtils.initIfEmpty(params
				.getTestingPkgs());
		System.out.println("testingClassNames = " + testingClassNames);
		System.out.println("testingPkgs = " + testingPkgs);
		for (Failure failure : falures) {
			for (StackTraceElement trace : failure.getException()
					.getStackTrace()) {
				String className = trace.getClassName();
				int lineNumber = trace.getLineNumber();
				if (className == null) {
					continue;
				}
				if (acceptedClasses.contains(className)
						|| testingClassNames.contains(className)) {
					bkps.add(new BreakPoint(className, trace.getMethodName(),
							lineNumber));
					continue;
				}
				for (String pkg : testingPkgs) {
					if (className.startsWith(pkg)) {
						acceptedClasses.add(className);
						bkps.add(new BreakPoint(className, trace
								.getMethodName(), lineNumber));
						break;
					}
				}
			}
		}
	}
	
	private static Class<?> loadClass(String className) throws ClassNotFoundException {
		return Class.forName(className);
	}
	
	public static JunitResult runTestcases(AppJavaClassPath appClasspath,
			JunitRunnerParameters params) throws ClassNotFoundException, IOException, SavException {
		VMConfiguration config = SavJunitRunner.createVmConfig(appClasspath);
		return runTestcases(config, params);
	}
	
	public static JunitResult runTestcases(VMConfiguration config,
			JunitRunnerParameters params) throws ClassNotFoundException,
			IOException, SavException {
		String destFile = params.getDestfile();
		if (destFile == null) {
			destFile = File.createTempFile("junitResult", ".temp").getAbsolutePath();
		}
		VMRunner runner = new VMRunner();
		config.setLaunchClass(JunitRunner.class.getName());
		config.setDebug(false);
		List<String> args = new JunitRunnerProgramArgBuilder()
				.methods(params.getClassMethods())
				.testClassNames(params.getTestingClassNames())
				.testingPackages(params.getTestingPkgs())
				.destinationFile(destFile)
				.testcaseTimeout(params.getTimeout())
				.build();
		config.setProgramArgs(args);
		runner.startAndWaitUntilStop(config);
		return JunitResult.readFrom(destFile);
	}
	
	public static class JunitRunnerProgramArgBuilder extends ProgramArgumentBuilder {
		public JunitRunnerProgramArgBuilder methods(List<String> classMethods){
			addArgument(JunitRunnerParameters.CLASS_METHODS, classMethods);
			return this;
		}

		public JunitRunnerProgramArgBuilder method(String classMethod){
			addArgument(JunitRunnerParameters.CLASS_METHODS, classMethod);
			return this;
		}
		
		public JunitRunnerProgramArgBuilder destinationFile(String destFile){
			addArgument(JunitRunnerParameters.DEST_FILE, destFile);
			return this;
		}
		
		public JunitRunnerProgramArgBuilder testClassNames(List<String> testClassNames){
			addArgument(JunitRunnerParameters.TESTING_CLASS_NAMES, testClassNames);
			return this;
		}
		
		public JunitRunnerProgramArgBuilder testingPackages(List<String> testingPkgs) {
			addArgument(JunitRunnerParameters.TESTING_PACKAGE_NAMES, testingPkgs);
			return this;
		}
		
		public ProgramArgumentBuilder testcaseTimeout(long timeout) {
			if (timeout > 0) {
				addArgument(JunitRunnerParameters.TESTCASE_TIME_OUT, String.valueOf(timeout));
			}
			return this;
		}
		
		public JunitRunnerProgramArgBuilder testcaseTimeout(long timeout,
				TimeUnit unit) {
			testcaseTimeout(unit.toMillis(timeout));
			return this;
		}
		
		public JunitRunnerProgramArgBuilder storeSingleTestResultDetail() {
			addOptionArgument(JunitRunnerParameters.SINGLE_TEST_RESULT_DETAIL);
			return this;
		}
	}
}
