package microbat.mutation.trace;

import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;

import microbat.agent.ExecTraceFileReader;
import microbat.model.trace.Trace;
import microbat.mutation.trace.dto.AnalysisParams;
import microbat.mutation.trace.dto.AnalysisTestcaseParams;
import microbat.mutation.trace.dto.MutationCase;
import microbat.mutation.trace.dto.MutationExecutionResult;
import microbat.mutation.trace.dto.SingleMutation;
import microbat.mutation.trace.preference.MutationRegressionPreference;
import microbat.mutation.trace.preference.MutationRegressionSettings;
import microbat.mutation.trace.report.BasicMutationExperimentMonitor;
import microbat.mutation.trace.report.IMutationExperimentMonitor;
import tregression.empiricalstudy.Regression;

public class MutationRegressionRetriever {
	
	
	public MuRegression retrieveRegression(String targetProject, String mutationBugId, 
			IProgressMonitor monitor, boolean useSliceBreaker, int breakerLimit)
			throws SQLException, IOException {
		MutationRegressionSettings mutationSettings = MutationRegressionPreference.getMutationRegressionSettings();
		AnalysisParams analysisParams = new AnalysisParams(mutationSettings);
		MutationCase mutationCase = MutationCase.load(targetProject, mutationBugId, mutationSettings.getMutationOutputSpace(), 
				analysisParams, null);
		IMutationExperimentMonitor experimentMonitor = new BasicMutationExperimentMonitor(monitor);
		
		MutationEvaluator evaluator = new MutationEvaluator();

		mutationCase.getTestcaseParams().setAnalysisParams(analysisParams);
		SingleMutation mutation = mutationCase.getMutation();
		if (mutationSettings.isRerun()) {
//			MutationExecutionResult executionResult = analyzer.runSingleMutationTrial(mutation,
//					mutationCase.getTestcaseParams(), experimentMonitor);
//			return buildRegression(mutationCase, executionResult);
			return null;
		} else {
			ExecTraceFileReader execTraceReader = new ExecTraceFileReader();
			MutationExecutionResult executionResult = new MutationExecutionResult();
			executionResult.bugTrace = execTraceReader.read(mutationCase.getBugTraceExec());
			executionResult.bugTrace.setSourceVersion(true);
			
			executionResult.correctTrace = execTraceReader.read(mutationCase.getCorrectTraceExec());
			executionResult.correctTrace.setSourceVersion(false);
			return buildRegression(mutationCase, executionResult);
		}
	}
	
	private MuRegression buildRegression(MutationCase mutationCase, MutationExecutionResult executionResult) {
		/* build trace from exec files */
		Trace buggyTrace = executionResult.getBugTrace();
		buggyTrace.setSourceVersion(true);
		Trace fixTrace = executionResult.getCorrectTrace();
		fixTrace.setSourceVersion(false);
		Regression regression = new Regression(buggyTrace, fixTrace, null);
		AnalysisTestcaseParams tcParams = mutationCase.getTestcaseParams();
		regression.setTestCase(tcParams.getJunitClassName(), tcParams.getTestMethod());
		
		/* MuRegression */
		MuRegression muRegression = new MuRegression();
		muRegression.setRegression(regression);
		SingleMutation mutation = mutationCase.getMutation();
		muRegression.setMutationFile(mutation.getFile().getAbsolutePath());
		muRegression.setMutationClassName(mutation.getMutatedClass());
		muRegression.setMutationCase(mutationCase);
		return muRegression;
	}
}
