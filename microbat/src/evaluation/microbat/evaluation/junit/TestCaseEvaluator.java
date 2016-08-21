package microbat.evaluation.junit;

import java.io.IOException;
import java.net.MalformedURLException;

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
	
	public void runEvaluation() throws JavaModelException, IOException{
		ignoredTestCaseFiles = new IgnoredTestCaseFiles();
		parsedTrials = new ParsedTrials();
		
		int flag = ALL;
		boolean isLimitTrialNum = false;
		
		runEvaluation(flag, isLimitTrialNum);
	}
	
	private void runEvaluation(int flag, boolean isLimitTrialNum) throws JavaModelException, IOException{
		TestCaseAnalyzer analyzer = new TestCaseAnalyzer();
		
		if(flag == ALL){
			ExcelReporter reporter = new ExcelReporter(Settings.projectName, this.unclearRates);
			IPackageFragmentRoot testRoot = JavaUtil.findTestPackageRootInProject();
			
			for(IJavaElement element: testRoot.getChildren()){
				if(element instanceof IPackageFragment){
					analyzer.runEvaluation((IPackageFragment)element, reporter, isLimitTrialNum, 
							ignoredTestCaseFiles, parsedTrials, trialNumPerTestCase, unclearRates);				
				}
			}
		}
		else if(flag == TRIAL){
			runSingeTrial();
		}
		else if(flag == TEST_CASE){
			ExcelReporter reporter = new ExcelReporter(Settings.projectName, this.unclearRates);
			String testClassName = "org.apache.commons.math.analysis.integration.RombergIntegratorTest";
			String testMethodName = "testSinFunction";
			analyzer.runEvaluationForSingleTestCase(testClassName, testMethodName, reporter, false,
					ignoredTestCaseFiles, parsedTrials, trialNumPerTestCase, unclearRates);
		}
		
	}

	private void runSingeTrial(){
		TestCaseAnalyzer analyzer = new TestCaseAnalyzer();
		
		//TODO BUG TimeOutException in JVM
//		String testClassName = "org.apache.commons.math.analysis.interpolation.LinearInterpolatorTest";
//		String testMethodName = "testInterpolateLinear";
//		String mutationFile = "C:\\Users\\YUNLIN~1\\AppData\\Local\\Temp\\"
//				+ "apache-common-math-2.2\\2081_22_1\\MathUtils.java";
//		String mutatedClass = "org.apache.commons.math.util.MathUtils";
		
//		String testClassName = "test.SimpleCalculatorTest";
//		String testMethodName = "testCalculator";
//		String mutationFile = "C:\\microbat_evaluation\\mutation\\110_29_1\\SimpleCalculator.java";
//		double unclearRate = 0;
//		boolean enableLoopInference = false;
//		boolean isReuseTrace = true;
		
		String testClassName = "org.apache.commons.collections.TestMultiHashMap";
		String testMethodName = "testTotalSize";
		String mutationFile = "C:\\microbat_evaluation\\apache-collections-3.2.2\\159_21_1\\MultiHashMap.java";
		double unclearRate = 0;
		boolean enableLoopInference = false;
		boolean isReuseTrace = true;
		
		try {
			analyzer.runEvaluationForSingleTrial(testClassName, testMethodName, mutationFile, 
					unclearRate, enableLoopInference, isReuseTrace);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
