/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.junit;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sav.common.core.SavRtException;
import sav.common.core.utils.JunitUtils;

/**
 * @author LLT
 * 
 */
@SuppressWarnings("static-access")
public class JunitRunnerParameters {
	private static final Options opts;
	static final String CLASS_METHODS = "methods";
	static final String TESTING_CLASS_NAMES = "testingclass";
	static final String TESTING_PACKAGE_NAMES = "testingpkgs";
	static final String DEST_FILE = "destfile";
	static final String TESTCASE_TIME_OUT = "tc_timeout";
	static final String SINGLE_TEST_RESULT_DETAIL = "tc_result_detail";
	
	private List<String> classMethods;
	private List<String> testingClassNames;
	private List<String> testingPkgs;
	private String destfile;
	/* for each testcase, to make sure the testcase does not run forever */
	private long timeout; 
	private boolean storeTestResultDetail = false;
	
	static {
		opts = new Options();
		opts.addOption(classMethods());
		opts.addOption(testingClassNames());
		opts.addOption(testingPkgs());
		opts.addOption(destfile());
		opts.addOption(timeout());
		opts.addOption(singleTestResultDetail());
	}

	private static Option classMethods() {
		return OptionBuilder
				.withArgName(CLASS_METHODS)
				.withDescription(
						"methods with full name includes className and method name")
				.hasArgs()
				.isRequired()
				.create(CLASS_METHODS);
	}

	private static Option timeout() {
		return OptionBuilder
				.withArgName(TESTCASE_TIME_OUT)
				.withDescription("Timeout for each testcase to make sure it does not run forever")
				.hasArg()
				.isRequired(false)
				.create(TESTCASE_TIME_OUT);
	}

	private static Option testingPkgs() {
		return OptionBuilder
				.withArgName(TESTING_PACKAGE_NAMES)
				.withDescription(
						"Testing packages contain classes for extracting failure traces")
				.hasArgs()
				.isRequired(false)
				.create(TESTING_PACKAGE_NAMES);
	}

	private static Option destfile() {
		return OptionBuilder
				.withArgName(DEST_FILE)
				.withDescription("Name of file to save testing result")
				.hasArg()
				.isRequired(false)
				.create(DEST_FILE);
	}

	private static Option testingClassNames() {
		return OptionBuilder
				.withArgName(TESTING_CLASS_NAMES)
				.withDescription(
						"Testing class names for extracting failure traces")
				.hasArgs()
				.isRequired(false)
				.create(TESTING_CLASS_NAMES);
	}
	
	private static Option singleTestResultDetail() {
		return OptionBuilder
				.withArgName(SINGLE_TEST_RESULT_DETAIL)
				.withDescription("Store detail of test result (stacktrace)")
				.isRequired(false)
				.create(SINGLE_TEST_RESULT_DETAIL);
	}

	public static JunitRunnerParameters parse(String[] args) throws ParseException {
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(opts, args);
		if (cmd.getOptions().length == 0) {
			throw new ParseException("No specified option");
		}
		JunitRunnerParameters params = new JunitRunnerParameters();
		if (cmd.hasOption(CLASS_METHODS)) {
			params.classMethods = Arrays.asList(cmd.getOptionValues(CLASS_METHODS));
		}
		if (cmd.hasOption(TESTING_CLASS_NAMES)) {
			params.testingClassNames = Arrays.asList(cmd.getOptionValues(TESTING_CLASS_NAMES));
		}
		if (cmd.hasOption(TESTING_PACKAGE_NAMES)) {
			params.testingPkgs = Arrays.asList(cmd.getOptionValues(TESTING_PACKAGE_NAMES));
		}
		if (cmd.hasOption(DEST_FILE)) {
			params.destfile = cmd.getOptionValue(DEST_FILE);
		}
		if (cmd.hasOption(TESTCASE_TIME_OUT)) {
			params.timeout = Long.valueOf(cmd.getOptionValue(TESTCASE_TIME_OUT));
		}
		if (cmd.hasOption(SINGLE_TEST_RESULT_DETAIL)) {
			params.storeTestResultDetail = true;
		}
		return params;
	}

	public List<String> getClassMethods() {
		return classMethods;
	}

	public List<String> getTestingClassNames() {
		return testingClassNames;
	}
	
	public List<String> getTestingPkgs() {
		return testingPkgs;
	}
	
	public String getDestfile() {
		return destfile;
	}

	public void setClassMethods(List<String> classMethods) {
		this.classMethods = classMethods;
	}
	
	/**
	 * all test methods in the given junit classes will be detected automatically
	 * and add to the classMethods for the test.
	 */
	public void setJunitClasses(List<String> junitClassNames) {
		try {
			this.classMethods = JunitUtils.extractTestMethods(junitClassNames, null);
		} catch (ClassNotFoundException e) {
			throw new SavRtException(
					"cannot extract test methods from junit classes: ",
					e.getMessage());
		} 
	}

	public void setTestingClassNames(List<String> testingClassNames) {
		this.testingClassNames = testingClassNames;
	}
	
	public void setTestingPkgs(List<String> testingPkgs) {
		this.testingPkgs = testingPkgs;
	}

	public void setDestfile(String destfile) {
		this.destfile = destfile;
	}
	
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout, TimeUnit unit) {
		this.timeout = unit.toMillis(timeout);
	}
	
	public boolean isStoreTestResultDetail() {
		return storeTestResultDetail;
	}

	public void setStoreTestResultDetail(boolean storeTestResultDetail) {
		this.storeTestResultDetail = storeTestResultDetail;
	}

	@Override
	public String toString() {
		return "JunitRunnerParameters [classMethods=" + classMethods
				+ ", testingClassNames=" + testingClassNames + ", testingPkgs="
				+ testingPkgs + ", destfile=" + destfile + ", timeout="
				+ timeout + ", storeTestResultDetail=" + storeTestResultDetail
				+ "]";
	}
	
}
