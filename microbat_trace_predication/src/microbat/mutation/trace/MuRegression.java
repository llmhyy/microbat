package microbat.mutation.trace;

import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.StringUtils;
import tregression.empiricalstudy.Regression;

public class MuRegression {
	private Regression regression;
	private String mutationCode;
	private String orginalCode;
	private String mutationClassName;

	public Regression getRegression() {
		return regression;
	}

	public void setRegression(Regression regression) {
		this.regression = regression;
	}

	public void setMutationFiles(String correctCode, String buggyCode) {
		// TODO: we should have another column for className in table mutationFile
		mutationClassName = ClassUtils.getCanonicalName(StringUtils.subString(correctCode, "package ", ";"),
				StringUtils.subString(correctCode, "public class ", " "));
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
	
}
