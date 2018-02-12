package microbat.evaluation.handler;

import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;

import microbat.evaluation.junit.TestCaseAnalyzer;

public class EvaluationTrialHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Do evaluation") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				runSingeTrial();
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		
		return null;
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
//		double unclearRate = 0.1;
//		boolean enableLoopInference = true;
//		boolean isReuseTrace = false;
//		int optionSearchLimit = 1000;
		
		String testCase = "org.apache.commons.math.linear.SparseFieldMatrixTest#testScalarAdd";
		String mutationFile = "C:\\microbat_evaluation\\apache-common-math-2.2\\"
				+ "org.apache.commons.math.util.MathUtils_920_17_1\\MathUtils.java";
		
//		String testCase = "org.apache.commons.math.analysis.ComposableFunctionTest#testCollector";
//		String mutationFile = "C:\\microbat_evaluation\\apache-common-math-2.2\\org.apache.commons.math.util.FastMath_1267_19_1\\FastMath.java";
		double unclearRate = -1;
		boolean enableLoopInference = true;
		boolean isReuseTrace = true;
		int optionSearchLimit = 1000;
		
		try {
			String testClassName = testCase.substring(0, testCase.indexOf("#"));
			String testMethodName = testCase.substring(testCase.indexOf("#")+1);
			analyzer.runEvaluationForSingleTrial(testClassName, testMethodName, mutationFile, 
					unclearRate, enableLoopInference, isReuseTrace, optionSearchLimit);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
