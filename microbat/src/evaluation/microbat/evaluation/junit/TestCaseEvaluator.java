package microbat.evaluation.junit;

import java.io.IOException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import microbat.evaluation.io.ExcelReporter;
import microbat.evaluation.io.IgnoredTestCaseFiles;
import microbat.util.JavaUtil;
import microbat.util.Settings;

public class TestCaseEvaluator {
	
	private IgnoredTestCaseFiles ignoredTestCaseFiles;
	private ParsedTrials parsedTrials;
	
	private int trialNumPerTestCase = 3;
	private double[] unclearRates = {0, 0.005, 0.01, 0.05, 0.1, -1};
//	private double[] unclearRates = {0};
	
	public static final int ALL = 0;
	public static final int TRIAL = 1;
	public static final int TEST_CASE = 2;
	
	public void runEvaluation(int mode) throws JavaModelException, IOException{
		ignoredTestCaseFiles = new IgnoredTestCaseFiles();
		parsedTrials = new ParsedTrials();
		
		int flag = mode;
		boolean isLimitTrialNum = false;
		int optionSearchLimit = 100;
		
		runEvaluation(flag, isLimitTrialNum, optionSearchLimit);
	}
	
	private void runEvaluation(int flag, boolean isLimitTrialNum, int optionSearchLimit) throws JavaModelException, IOException{
		TestCaseAnalyzer analyzer = new TestCaseAnalyzer();
		
		if(flag == ALL){
			ExcelReporter reporter = new ExcelReporter(Settings.projectName, this.unclearRates);
			IPackageFragmentRoot testRoot = JavaUtil.findTestPackageRootInProject();
			
			for(IJavaElement element: testRoot.getChildren()){
				if(element instanceof IPackageFragment){
					analyzer.runEvaluation((IPackageFragment)element, reporter, isLimitTrialNum, 
							ignoredTestCaseFiles, parsedTrials, trialNumPerTestCase, unclearRates, optionSearchLimit);				
				}
			}
		}
		else if(flag == TEST_CASE){
			ExcelReporter reporter = new ExcelReporter(Settings.projectName, this.unclearRates);
			String testClassName = "org.apache.commons.math.analysis.interpolation.DividedDifferenceInterpolatorTest";
			String testMethodName = "testExpm1Function";
			analyzer.runEvaluationForSingleTestCase(testClassName, testMethodName, reporter, false,
					ignoredTestCaseFiles, parsedTrials, trialNumPerTestCase, unclearRates, optionSearchLimit);
		}
		
	}

	
}
