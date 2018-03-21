package microbat.mutation.trace.dto;

import microbat.evaluation.io.IgnoredTestCaseFiles;
import tregression.junit.ParsedTrials;

public class AnalysisParams {
	private IgnoredTestCaseFiles ignoredTestCaseFiles = new IgnoredTestCaseFiles();
	private ParsedTrials parsedTrials;
	private int trialNumPerTestCase = 3;
	private double[] unclearRates = { 0 };
	private boolean isLimitTrialNum = false;
	private int optionSearchLimit = 100;
	private int muTotal = 10;

	public IgnoredTestCaseFiles getIgnoredTestCaseFiles() {
		return ignoredTestCaseFiles;
	}

	public void setIgnoredTestCaseFiles(IgnoredTestCaseFiles ignoredTestCaseFiles) {
		this.ignoredTestCaseFiles = ignoredTestCaseFiles;
	}

	public ParsedTrials getParsedTrials() {
		return parsedTrials;
	}

	public void setParsedTrials(ParsedTrials parsedTrials) {
		this.parsedTrials = parsedTrials;
	}

	public int getTrialNumPerTestCase() {
		return trialNumPerTestCase;
	}

	public void setTrialNumPerTestCase(int trialNumPerTestCase) {
		this.trialNumPerTestCase = trialNumPerTestCase;
	}

	public double[] getUnclearRates() {
		return unclearRates;
	}

	public void setUnclearRates(double[] unclearRates) {
		this.unclearRates = unclearRates;
	}

	public boolean isLimitTrialNum() {
		return isLimitTrialNum;
	}

	public void setLimitTrialNum(boolean isLimitTrialNum) {
		this.isLimitTrialNum = isLimitTrialNum;
	}

	public int getOptionSearchLimit() {
		return optionSearchLimit;
	}

	public void setOptionSearchLimit(int optionSearchLimit) {
		this.optionSearchLimit = optionSearchLimit;
	}
	
	public int getMuTotal() {
		return muTotal;
	}

	public void updateIgnoredTestcase(String testCaseName) {
		ignoredTestCaseFiles.addTestCase(testCaseName);
	}
}
