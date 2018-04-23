package microbat.mutation.trace.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import microbat.mutation.trace.MutationEvaluator;
import microbat.mutation.trace.dto.AnalysisParams;
import microbat.mutation.trace.dto.MutationCase;
import microbat.mutation.trace.preference.MutationRegressionPreference;
import microbat.mutation.trace.preference.MutationRegressionSettings;
import microbat.mutation.trace.report.MutationExperimentMonitor;

public class RunSingleMutationHandler  extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Evaluate Mutations") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					runSingleMutation(monitor);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

	private void runSingleMutation(IProgressMonitor monitor) throws IOException {
		MutationRegressionSettings settings = MutationRegressionPreference.getMutationRegressionSettings();
		AnalysisParams analysisParams = new AnalysisParams(settings);
		MutationCase mutationCase = MutationCase.load(settings.getTargetProject(), settings.getBugId(),
				settings.getMutationOutputSpace(), analysisParams);
		MutationEvaluator evaluator = new MutationEvaluator();
		MutationExperimentMonitor mutationMonitor = new MutationExperimentMonitor(monitor, settings.getTargetProject(), analysisParams);
		evaluator.runSingleMutationTrial(mutationCase, mutationMonitor);
	}
}
