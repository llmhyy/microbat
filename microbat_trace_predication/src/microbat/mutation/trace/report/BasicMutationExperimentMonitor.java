package microbat.mutation.trace.report;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import microbat.mutation.trace.dto.AnalysisTestcaseParams;
import microbat.mutation.trace.dto.SingleMutation;
import microbat.mutation.trace.dto.TraceExecutionInfo;
import tregression.empiricalstudy.EmpiricalTrial;

public class BasicMutationExperimentMonitor implements IMutationExperimentMonitor {
	private IProgressMonitor progressMonitor;

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
	public void reportEmpiralTrial(List<EmpiricalTrial> trials0, AnalysisTestcaseParams params, SingleMutation mutation)
			throws IOException {
		// do nothing by default
	}

}
