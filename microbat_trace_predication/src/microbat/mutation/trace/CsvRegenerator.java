package microbat.mutation.trace;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.mutation.trace.dto.AnalysisTestcaseParams;
import microbat.mutation.trace.dto.BackupClassFiles;
import microbat.mutation.trace.dto.MutationCase;
import microbat.mutation.trace.dto.MutationExecutionResult;
import microbat.mutation.trace.dto.SingleMutation;
import microbat.mutation.trace.dto.TraceExecutionInfo;
import microbat.mutation.trace.handlers.RunSingleMutationHandler;
import microbat.mutation.trace.report.IMutationExperimentMonitor;
import tregression.SimulationFailException;
import tregression.empiricalstudy.RootCauseFinder;
import tregression.empiricalstudy.Simulator;
import tregression.model.PairList;

public class CsvRegenerator extends MutationEvaluator {
	private MutationGenerator generator = new MutationGenerator();

	
	public void regenerate(MutationCase mutationCase, IMutationExperimentMonitor monitor) {
		System.out.println("Start Mutation case: " + mutationCase.getMutation().getMutationBugId());
		if (!mutationCase.isValid()) {
			return;
		}
			
		SingleMutation mutation = mutationCase.getMutation();
		AnalysisTestcaseParams params = mutationCase.getTestcaseParams();
		MutationExecutionResult result = new MutationExecutionResult();

		TraceExecutionInfo correctTraceInfo = restoreTrace(mutationCase.getCorrectTraceExec(),
				mutationCase.getCorrectPrecheckPath(), mutationCase.getTestcaseParams().getProjectName(),
				MuRegressionUtils.createProjectClassPath(params), false);
		Trace correctTrace = correctTraceInfo.getTrace();
		result.setCorrectTrace(correctTrace);
		
		TraceExecutionInfo mutationTraceInfo = restoreTrace(mutationCase.getBugTraceExec(),
				mutationCase.getBugPrecheckPath(), mutationCase.getTestcaseParams().getProjectName(),
				MuRegressionUtils.createProjectClassPath(params), true);
		Trace mutationTrace = mutationTraceInfo.getTrace();
		MuRegressionUtils.fillMuBkpJavaFilePath(mutationTrace, mutation.getMutationJavaFile(),
				mutation.getMutatedClass());
		result.setBugTrace(mutationTrace);

		AppJavaClassPathWrapper.wrapAppClassPath(mutationTrace, correctTrace, params.getBkClassFiles());
		
		MuDiffMatcher diffMatcher = RunSingleMutationHandler.initDiffMatcher(mutationCase);
		PairList pairList = RunSingleMutationHandler.buildPairList(correctTrace, mutationTrace, diffMatcher);
		try {
			Simulator simulator = new Simulator(params.getAnalysisParams().isUseSliceBreaker(), false,
					params.getAnalysisParams().getBreakerLimit());
			simulator.prepare(mutationTrace, correctTrace, pairList, diffMatcher);
			RootCauseFinder rootcauseFinder = new RootCauseFinder();
			rootcauseFinder.checkRootCause(simulator.getObservedFault(), mutationTrace, correctTrace, pairList, diffMatcher);
			TraceNode rootCause = rootcauseFinder.retrieveRootCause(pairList, diffMatcher, mutationTrace, correctTrace);
			if (rootCause != null) {
				generator.runSimulator(mutation, params, mutationTrace, correctTrace, diffMatcher, pairList);
			}
		} catch (Exception e) {
			params.recoverOrgMutatedClassFile();
		}
	}
	
}
