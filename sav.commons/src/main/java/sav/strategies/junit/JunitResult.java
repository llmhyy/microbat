/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.junit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import sav.common.core.Pair;
import sav.common.core.utils.JunitUtils;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.BreakPoint;
import sav.strategies.dto.ClassLocation;

/**
 * @author LLT
 * 
 */
public class JunitResult {
	private static final String JUNIT_RUNNER_BLOCK = "JunitRunner Result";
	private static final String JUNIT_RUNNER_FAILURE_TRACE = "FailureTraces";
	private static final String STORE_SINGLE_FAIL_TRACE_FLAG = "//store failure trace for each fail test";
	private Set<BreakPoint> failureTraces = new HashSet<BreakPoint>();
	private Map<Pair<String, String>, TestResult> testResult = new LinkedHashMap<Pair<String, String>, TestResult>();
	private List<Boolean> result = new ArrayList<Boolean>();

	public Set<BreakPoint> getFailureTraces() {
		return failureTraces;
	}
	
	public void addResult(Pair<String, String> classMethod, boolean pass, String firstStackTraceEle) {
		testResult.put(classMethod, TestResult.of(pass, firstStackTraceEle));
		result.add(pass);
	}
	
	public List<Pair<String, String>> getFailTests() {
		List<Pair<String, String>> tests = new ArrayList<Pair<String,String>>();
		for (Entry<Pair<String, String>, TestResult> entry : testResult.entrySet()) {
			if (entry.getValue() != TestResult.PASS) {
				tests.add(entry.getKey());
			}
		}
		return tests;
	}
	
	public List<Pair<String, String>> getPassTests() {
		List<Pair<String, String>> tests = new ArrayList<Pair<String,String>>();
		for (Entry<Pair<String, String>, TestResult> entry : testResult.entrySet()) {
			if (entry.getValue() == TestResult.PASS) {
				tests.add(entry.getKey());
			}
		}
		return tests;
	}
	
	public List<Pair<String, String>> getTests() {
		List<Pair<String, String>> tests = new ArrayList<Pair<String,String>>();
		for (Entry<Pair<String, String>, TestResult> entry : testResult.entrySet()) {
			tests.add(entry.getKey());
		}
		return tests;
	}
	
	public boolean getResult(int idx) {
		if (idx < 0 || idx >= result.size()) {
			return false;
		}
		return result.get(idx);
	}
	
	public Map<String, Boolean> getResult(List<String> tcs) {
		List<Pair<String, String>> keys = JunitUtils.toPair(tcs);
		Map<String, Boolean> tcResults = new HashMap<String, Boolean>();
		for (int i = 0; i < keys.size(); i++) {
			tcResults.put(tcs.get(i), testResult.get(keys.get(i)).isPass());
		}
		return tcResults;
	}
	
	public boolean getResult(String tc) {
		return testResult.get(JunitUtils.toPair(tc)).isPass();
	}
	
	public TestResult getTestResult(String tc) {
		return testResult.get(JunitUtils.toPair(tc));
	}
	
	public Map<Pair<String, String>, TestResult> getTestResults() {
		return testResult;
	}
	
	public List<Boolean> getTestResult() {
		return result;
	}

	public void save(File file, boolean storeSingleFailTrace) throws IOException {
		FileOutputStream output = null;
		try {
			/*
			 * NOTE!: this allows to append file if it already existed.
			 * FileUtils.writeStringToFile of apache only allows to write content to file
			 * without appending.
			 */
			output = new FileOutputStream(file, true);
			if (storeSingleFailTrace) {
				IOUtils.write(STORE_SINGLE_FAIL_TRACE_FLAG, output);
				IOUtils.write("\n", output);
			}
			IOUtils.write(JUNIT_RUNNER_BLOCK, output);
			IOUtils.write("\n", output);
			for (Entry<Pair<String, String>, TestResult> entry : testResult.entrySet()) {
				boolean pass = entry.getValue().isPass();
				IOUtils.write(StringUtils.spaceJoin(entry.getKey().first(),
						entry.getKey().second(), pass), output);
				IOUtils.write("\n", output);
				if (!pass && storeSingleFailTrace) {
					IOUtils.write(entry.getValue().getFailureTrace(), output);
					IOUtils.write("\n", output);
				}
			}
			if (!failureTraces.isEmpty()) {
				IOUtils.write(JUNIT_RUNNER_FAILURE_TRACE, output);
				IOUtils.write("\n", output);
				for (ClassLocation loc : failureTraces) {
					IOUtils.write(StringUtils.spaceJoin(
							loc.getClassCanonicalName(), loc.getMethodName(),
							loc.getLineNo()), output);
					IOUtils.write("\n", output);
				}
			}
		} finally {
			IOUtils.closeQuietly(output);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static JunitResult readFrom(String junitResultFile)
			throws IOException {
		List<String> lines = (List<String>) FileUtils.readLines(new File(
				junitResultFile));
		JunitResult result = new JunitResult();
		String block = null;
		Iterator<String> it = lines.iterator();
		boolean storeSingleFailTrace = false;
		while(it.hasNext()) {
			String line = it.next();
			if (STORE_SINGLE_FAIL_TRACE_FLAG.equals(line)) {
				storeSingleFailTrace = true;
				continue;
			}
			if (JUNIT_RUNNER_BLOCK.equals(line)) {
				block = JUNIT_RUNNER_BLOCK;
				continue;
			}
			if (JUNIT_RUNNER_FAILURE_TRACE.equals(line)) {
				block = JUNIT_RUNNER_FAILURE_TRACE;
				continue;
			}
			if (block == JUNIT_RUNNER_BLOCK) {
				String[] strs = line.split(" ");
				if (strs.length != 3) {
					throw new IllegalArgumentException(
							"junit runner block was written incorrectly, expect \"{class} {method} {line}, get \""
									+ line);
				}
				boolean isPass = Boolean.valueOf(strs[strs.length - 1]);
				String failTrace = StringUtils.EMPTY;
				if (!isPass && storeSingleFailTrace) {
					failTrace = it.next(); 
				}
				result.testResult.put(Pair.of(strs[0], strs[1]), TestResult.of(isPass, failTrace));
				result.result.add(isPass);
			} else if (block == JUNIT_RUNNER_FAILURE_TRACE) {
				String[] strs = line.split(" ");
				result.failureTraces.add(new BreakPoint(strs[0], 
						strs[1], Integer.valueOf(strs[2])));
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "JunitResult [testResult=" + testResult + "]";
	}

}
