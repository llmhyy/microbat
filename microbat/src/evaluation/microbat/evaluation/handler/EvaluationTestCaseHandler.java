package microbat.evaluation.handler;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;

import microbat.evaluation.io.ExcelReporter;
import microbat.evaluation.io.IgnoredTestCaseFiles;
import microbat.evaluation.junit.ParsedTrials;
import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.util.Settings;

public class EvaluationTestCaseHandler extends AbstractHandler {

	private IgnoredTestCaseFiles ignoredTestCaseFiles;
	private ParsedTrials parsedTrials;
	
	private int trialNumPerTestCase = 3;
	private double[] unclearRates = {0, 0.005, 0.01, 0.05, 0.1, -1};
	
	
	private boolean isLimitTrialNum = false;
	private int optionSearchLimit = 100;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ignoredTestCaseFiles = new IgnoredTestCaseFiles();
		parsedTrials = new ParsedTrials();
		
		Job job = new Job("Do evaluation") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				TestCaseAnalyzer analyzer = new TestCaseAnalyzer();
				try {
					ExcelReporter reporter = new ExcelReporter(Settings.projectName, unclearRates);
					String testClassName = "org.apache.commons.math.analysis.interpolation.DividedDifferenceInterpolatorTest";
					String testMethodName = "testExpm1Function";
					analyzer.runEvaluationForSingleTestCase(testClassName, testMethodName, reporter, isLimitTrialNum,
							ignoredTestCaseFiles, parsedTrials, trialNumPerTestCase, unclearRates, optionSearchLimit);
				} catch (JavaModelException | IOException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		
		return null;
	}

}
