package microbat.mutation.trace;

import java.io.File;

import sav.common.core.utils.FileUtils;
import tregression.empiricalstudy.Regression;

public class MuRegression {
	private Regression regression;
	private File mutationFile;
	private File orginalFile;

	public Regression getRegression() {
		return regression;
	}

	public void setRegression(Regression regression) {
		this.regression = regression;
	}

	public File getMutationFile() {
		return mutationFile;
	}

	public void setMutationFile(File mutationFile) {
		this.mutationFile = mutationFile;
	}

	public File getOrginalFile() {
		return orginalFile;
	}

	public void setOrginalFile(File orginalFile) {
		this.orginalFile = orginalFile;
	}

	public void setMutationFiles(String correctCode, String buggyCode) {
		orginalFile = FileUtils.getFileInTempFolder("CorrectCode.java");
		FileUtils.appendFile(orginalFile.getAbsolutePath(), correctCode);
		
		mutationFile = FileUtils.getFileInTempFolder("MutationCode.java");
		FileUtils.appendFile(mutationFile.getAbsolutePath(), buggyCode);
	}
	
}
