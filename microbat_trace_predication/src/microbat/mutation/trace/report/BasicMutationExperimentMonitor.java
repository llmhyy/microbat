package microbat.mutation.trace.report;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import microbat.mutation.trace.dto.AnalysisTestcaseParams;
import microbat.mutation.trace.dto.MutationTrace;
import microbat.mutation.trace.dto.SingleMutation;
import microbat.mutation.trace.dto.TraceExecutionInfo;
import tregression.empiricalstudy.EmpiricalTrial;

public class BasicMutationExperimentMonitor implements IMutationExperimentMonitor {
	private IProgressMonitor progressMonitor;
	private IMutationCaseChecker checker = new EmptyMutationCaseChecker();

	public BasicMutationExperimentMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	@Override
	public boolean isCanceled() {
		return progressMonitor.isCanceled();
	}

	@Override
	public void reportTrial(AnalysisTestcaseParams params, TraceExecutionInfo correctTraceInfo,
			TraceExecutionInfo traceExecInfo, SingleMutation mutation, boolean foundRootCause) {
		// do nothing by default
	}

	@Override
	public void reportEmpiralTrial(String fileName, List<EmpiricalTrial> trials0, AnalysisTestcaseParams params,
			SingleMutation mutation) throws IOException {
		// do nothing by default
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see microbat.mutation.trace.report.IMutationExperimentMonitor#
	 * getMutationCaseChecker()
	 */
	@Override
	public IMutationCaseChecker getMutationCaseChecker() {
		return checker;
	}

	@Override
	public void reportMutationCase(AnalysisTestcaseParams params, TraceExecutionInfo correctTrace,
			MutationTrace muTrace, SingleMutation mutation) {
		// TODO Auto-generated method stub

	}

}
