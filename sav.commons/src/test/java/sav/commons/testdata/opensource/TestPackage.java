/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.opensource;

import static sav.common.core.utils.ResourceUtils.appendPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import sav.common.core.utils.Assert;
import sav.common.core.utils.StringUtils;
import sav.commons.TestConfiguration;

/**
 * @author LLT
 *
 */
public class TestPackage {
	private static final String ITEM_SEPARATOR = ";";
	private static final String IGNORE_CELL_STR = "#";
	private static final int TESTDATA_START_RECORD = 4;
	private static Properties testDataConfig;
	private static String workspace;
	
	private static Map<String, TestPackage> allTestData;
	private Map<TestDataColumn, Object> packageData;
	private String projectFolder;
	
	static {
		try {
			testDataConfig = new Properties();
			testDataConfig.load(new FileInputStream(
					TestConfiguration.TESTDATA_PROPERTIES));
			workspace = testDataConfig.getProperty("testdata.workspace");
			allTestData = new LinkedHashMap<String, TestPackage>();
			loadTestData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TestPackage() {
		packageData = new HashMap<TestPackage.TestDataColumn, Object>();
	}
	
	private static Map<String, TestPackage> loadTestData() throws IOException {
		return loadTestData(TestConfiguration.TESTDATA_CSV);
	}
	
	public static Map<String, TestPackage> loadTestData(String testDataFile) throws IOException {
		CSVFormat format = CSVFormat.EXCEL.withHeader(TestDataColumn.allColumns());
		CSVParser parser = CSVParser.parse(new File(
				testDataFile), Charset.forName("UTF-8"), format);
		List<CSVRecord> records = parser.getRecords();
		Map<String, TestPackage> allTests = new LinkedHashMap<String, TestPackage>();
		for (int i = TESTDATA_START_RECORD; i < records.size(); i++) {
			TestPackage pkg = new TestPackage();
			CSVRecord record = records.get(i);
			for (TestDataColumn col : TestDataColumn.values()) {
				String cellStr = record.get(col);
				if (cellStr.startsWith(IGNORE_CELL_STR)) {
					cellStr = StringUtils.EMPTY;
				}
				if (col.multi) {
					pkg.packageData.put(col, Arrays
							.asList(org.apache.commons.lang.StringUtils.split(
									cellStr, ITEM_SEPARATOR)));
				} else {
					pkg.packageData.put(col, cellStr);
				}
			}
			pkg.projectFolder = appendPath(workspace,
					pkg.getValue(TestDataColumn.PROJECT_NAME));
			allTests.put(getPkgId(record), pkg);
		}
		allTestData.putAll(allTests);
		return allTests;
	}
	
	public String getValue(TestDataColumn col) {
		Assert.assertTrue(!col.multi);
		String value = (String) packageData.get(col);
		if (col.relativePath) {
			return toAbsolutePath(value);
		}
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getValues(TestDataColumn col) {
		Assert.assertTrue(col.multi);
		List<String> value = (List<String>) packageData.get(col);
		if (col.relativePath) {
			return toAbsolutePaths(value);
		}
		return value;
	}
	
	private static String getPkgId(CSVRecord record) {
		return getPkgId(record.get(TestDataColumn.PROJECT_NAME),
				record.get(TestDataColumn.BUG_NUMBER));
	}
	
	private static String getPkgId(String prjName, String bugNo) {
		return StringUtils.join(":", prjName, bugNo);
	}
	
	public static TestPackage getPackage(String projectName, String bugNo) {
		TestPackage pkg = allTestData.get(getPkgId(projectName, bugNo));
		if (pkg == null) {
			throw new IllegalArgumentException(
					String.format(
							"cannot find the description for %s of project %s in testdata file",
							bugNo, projectName));
		}
		return pkg;
	}

	public static Map<String, TestPackage> getAllTestData() {
		return allTestData;
	}

	public List<String> getClassPaths() {
		return toAbsolutePaths(getValues(TestDataColumn.CLASS_PATH));
	}
	
	
	private List<String> toAbsolutePaths(List<String> paths) {
		List<String> abPaths = new ArrayList<String>();
		for (String path : paths) {
			abPaths.add(toAbsolutePath(path));
		}
		return abPaths;
	}

	private String toAbsolutePath(String path) {
		return appendPath(projectFolder, path);
	}
	
	public List<String> getLibFolders() {
		return toAbsolutePaths(getValues(TestDataColumn.LIB_FOLDERS));
	}
	
	public static enum TestDataColumn {
		PROJECT_NAME (false), // String
		BUG_NUMBER (false), // String
		SOURCE_FOLDER(false, true),
		TARGET_FOLDER(false, true),
		TEST_TARGET_FOLDER(false, true),
		CLASS_PATH (true), // List<String>
		LIB_FOLDERS (true), 
		TEST_CLASSES (true), // List<String>
		ANALYZING_CLASSES (true),
		ANALYZING_PACKAGES (true), // List<String>
		EXPECTED_BUG_LOCATION (true); // List<String>
		
		private boolean multi;
		private boolean relativePath;
		
		private TestDataColumn(boolean multi) {
			this(multi, false);
		}
		
		private TestDataColumn(boolean multi, boolean relativePath) {
			this.multi = multi;
			this.relativePath = relativePath;
		}
		
		public static String[] allColumns() {
			TestDataColumn[] values = values();
			String[] cols = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				cols[i] = values[i].name();
			}
			return cols;
		}
	}

}
