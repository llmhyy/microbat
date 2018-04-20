package microbat.mutation.trace;

import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;

import microbat.agent.ExecTraceFileReader;
import microbat.model.trace.Trace;
import microbat.mutation.trace.MutationExperimentator.MutationExecutionResult;
import microbat.mutation.trace.dto.AnalysisParams;
import microbat.mutation.trace.dto.AnalysisTestcaseParams;
import microbat.mutation.trace.dto.MutationCase;
import microbat.mutation.trace.dto.SingleMutation;
import microbat.mutation.trace.preference.MutationRegressionPreference;
import microbat.mutation.trace.report.BasicMutationExperimentMonitor;
import microbat.mutation.trace.report.IMutationExperimentMonitor;
import tregression.empiricalstudy.Regression;

public class MutationRegressionRetriever {
	
	
	public MuRegression retrieveRegression(String targetProject, String mutationBugId, 
			IProgressMonitor monitor, boolean useSliceBreaker, int breakerLimit)
			throws SQLException, IOException {
		MutationCase mutationCase = MutationCase.load(targetProject, mutationBugId);
		IMutationExperimentMonitor experimentMonitor = new BasicMutationExperimentMonitor(monitor);

		return buildRegression(mutationCase, experimentMonitor, useSliceBreaker, breakerLimit);
	}

	public MuRegression buildRegression(MutationCase mutationCase, IMutationExperimentMonitor experimentMonitor,
			boolean useSliceBreaker, int breakerLimit) {
		AnalysisParams analysisParams = new AnalysisParams();
		MutationExperimentator analyzer = new MutationExperimentator(useSliceBreaker, breakerLimit);

		mutationCase.getTestcaseParams().setAnalysisParams(analysisParams);
		SingleMutation mutation = mutationCase.getMutation();
		if (MutationRegressionPreference.getRerunFlag()) {
			MutationExecutionResult executionResult = analyzer.runSingleMutationTrial(mutation,
					mutationCase.getTestcaseParams(), experimentMonitor);
			return buildRegression(mutationCase, executionResult);
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
