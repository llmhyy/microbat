package microbat.mutation.trace;

import microbat.mutation.trace.dto.MutationCase;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.StringUtils;
import tregression.empiricalstudy.Regression;

public class MuRegression {
	private Regression regression;
	private String mutationClassName;
	private String mutationFile;
	private String orgFile;
	
	/* to remove */
	private String mutationCode;
	private String orginalCode;
	private MutationCase mutationCase;

	public Regression getRegression() {
		return regression;
	}

	public void setRegression(Regression regression) {
		this.regression = regression;
	}

	public void setMutationFiles(String correctCode, String buggyCode, String className) {
		mutationClassName = className;
		if (mutationClassName == null) {
			mutationClassName = ClassUtils.getCanonicalName(StringUtils.subString(correctCode, "package ", ";"),
					StringUtils.subString(correctCode, "public class ", " "));
		}
		orginalCode = correctCode;
		mutationCode = buggyCode;
	}

	public String getMutationCode() {
		return mutationCode;
	}

	public String getOrginalCode() {
		return orginalCode;
	}

	public String getMutationClassName() {
		return mutationClassName;
	}

	public String getMutationFile() {
		return mutationFile;
	}

	public void setMutationFile(String mutationFile) {
		this.mutationFile = mutationFile;
	}

	@Deprecated
	public String getOrgFile() {
		return orgFile;
	}

	public void setOrgFile(String orgFile) {
		this.orgFile = orgFile;
	}
	
	public void setMutationClassName(String mutationClassName) {
		this.mutationClassName = mutationClassName;
	}

	public MutationCase getMutationCase() {
		return mutationCase;
	}

	public void setMutationCase(MutationCase mutationCase) {
		this.mutationCase = mutationCase;
	}
	
}
