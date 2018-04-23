package microbat.mutation.trace.report;

import java.io.IOException;
import java.util.List;

import microbat.mutation.trace.dto.AnalysisTestcaseParams;
import microbat.mutation.trace.dto.MutationTrace;
import microbat.mutation.trace.dto.SingleMutation;
import microbat.mutation.trace.dto.TraceExecutionInfo;
import tregression.empiricalstudy.EmpiricalTrial;

public interface IMutationExperimentMonitor {

	boolean isCanceled();

	void reportTrial(AnalysisTestcaseParams params, TraceExecutionInfo correctTraceInfo,
			TraceExecutionInfo traceExecInfo, SingleMutation mutation, boolean foundRootCause);

	void reportEmpiralTrial(String fileName, List<EmpiricalTrial> trials0, AnalysisTestcaseParams params, SingleMutation mutation)
			throws IOException;

	IMutationCaseChecker getMutationCaseChecker();

	void reportMutationCase(AnalysisTestcaseParams params, TraceExecutionInfo correctTrace,
			MutationTrace muTrace, SingleMutation mutation);

}
