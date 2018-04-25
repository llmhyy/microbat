package microbat.mutation.trace.dto;

import java.io.File;

import microbat.mutation.trace.MuRegressionUtils;
import sav.common.core.utils.FileUtils;

public class AnalysisTestcaseParams {
	private String junitClassName;
	private String testMethod;
	private String projectName;
	private String analysisOutputFolder;
	private String projectOutputFolder;
	private String projectFolder;
	
	/* internal use fields */
	private String testcaseName;
	private AnalysisParams analysisParams;
	private String testSourceFolder;
	private BackupClassFiles bkClassFiles;
	
	public AnalysisTestcaseParams(String projectName, String junitClassName, String testMethod,
			AnalysisParams analysisParams, String projectFolder) {
		this.projectName = projectName;
		setJunitTest(junitClassName, testMethod);
		this.analysisParams = analysisParams;
		analysisOutputFolder = MuRegressionUtils.getAnalysisOutputFolder(analysisParams.getMutationOutputSpace(),
				projectName, junitClassName, testMethod);
		projectOutputFolder = FileUtils.getFilePath(analysisParams.getMutationOutputSpace(), "mutation", projectName) + File.separator;
		this.projectFolder = projectFolder;
	}

	public String getAnalysisOutputFolder() {
		return analysisOutputFolder;
	}
	
	public String getProjectOutputFolder() {
		return projectOutputFolder;
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

	public String getProjectFolder() {
		return projectFolder;
	}
	
	public void setProjectFolder(String projectFolder) {
		this.projectFolder = projectFolder;
	}
	
	public void recoverOrgMutatedClassFile() {
		if (bkClassFiles != null) {
			bkClassFiles.restoreOrgClassFile();
		}
	}
}
