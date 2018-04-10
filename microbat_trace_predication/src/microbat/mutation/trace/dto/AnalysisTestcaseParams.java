package microbat.mutation.trace.dto;

import microbat.mutation.trace.MuRegressionUtils;

public class AnalysisTestcaseParams {
	private String junitClassName;
	private String testMethod;
	private String projectName;
	private String analysisOutputFolder;
	
	/* internal use fields */
	private String testcaseName;
	private AnalysisParams analysisParams;
	private String testSourceFolder;
	private BackupClassFiles bkClassFiles;
	
	public AnalysisTestcaseParams(String projectName, String junitClassName, String testMethod,
			AnalysisParams analysisParams) {
		this.projectName = projectName;
		setJunitTest(junitClassName, testMethod);
		this.analysisParams = analysisParams;
		analysisOutputFolder = MuRegressionUtils.getAnalysisOutputFolder(projectName, junitClassName, testMethod);
	}

	public String getAnalysisOutputFolder() {
		return analysisOutputFolder;
	}
	
	public String getJunitClassName() {
		return junitClassName;
	}

	public void setJunitTest(String junitClassName, String testMethod) {
		this.junitClassName = junitClassName;
		this.testMethod = testMethod;
		this.testcaseName = junitClassName + "#" + testMethod;
	}

	public String getTestMethod() {
		return testMethod;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getTestcaseName() {
		return testcaseName;
	}

	public AnalysisParams getAnalysisParams() {
		return analysisParams;
	}

	public void setAnalysisParams(AnalysisParams analysisParams) {
		this.analysisParams = analysisParams;
	}

	public String getTestSourceFolder() {
		return testSourceFolder;
	}

	public void setTestSourceFolder(String testSourceFolder) {
		this.testSourceFolder = testSourceFolder;
	}

	public BackupClassFiles getBkClassFiles() {
		return bkClassFiles;
	}

	public void setBkClassFiles(BackupClassFiles bkClassFiles) {
		this.bkClassFiles = bkClassFiles;
	}
	
}
