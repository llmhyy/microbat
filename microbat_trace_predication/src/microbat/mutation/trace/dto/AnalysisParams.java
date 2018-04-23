package microbat.mutation.trace.dto;

import java.util.List;

import microbat.evaluation.io.IgnoredTestCaseFiles;
import microbat.mutation.mutation.MutationType;
import microbat.mutation.trace.preference.MutationRegressionSettings;
import tregression.junit.ParsedTrials;

public class AnalysisParams {
	private IgnoredTestCaseFiles ignoredTestCaseFiles = new IgnoredTestCaseFiles();
	private ParsedTrials parsedTrials;
	private int trialNumPerTestCase = 3;
	private double[] unclearRates = { 0 };
	private boolean isLimitTrialNum = false;
	private int optionSearchLimit = 100;
	private int muTotal = 10;
	private List<MutationType> mutationTypes;
	private boolean useSliceBreaker = true;
	private int breakerLimit = 3;
	private final int stepLimit = 10000;
	private final long executionTimeout = 30000l;
	private String mutationOutputSpace;
	
	public AnalysisParams(MutationRegressionSettings mutationSettings) {
		this.mutationTypes = mutationSettings.getMutationTypes();
		this.mutationOutputSpace = mutationSettings.getMutationOutputSpace();
	}

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
	
	public List<MutationType> getMutationTypes() {
		return mutationTypes;
	}
	
	public void setMutationTypes(List<MutationType> mutationTypes) {
		this.mutationTypes = mutationTypes;
	}

	public boolean isUseSliceBreaker() {
		return useSliceBreaker;
	}

	public void setUseSliceBreaker(boolean useSliceBreaker) {
		this.useSliceBreaker = useSliceBreaker;
	}

	public int getBreakerLimit() {
		return breakerLimit;
	}

	public void setBreakerLimit(int breakerLimit) {
		this.breakerLimit = breakerLimit;
	}

	public int getStepLimit() {
		return stepLimit;
	}

	public long getExecutionTimeout() {
		return executionTimeout;
	}
	
	public String getMutationOutputSpace() {
		return mutationOutputSpace;
	}
}
