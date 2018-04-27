package microbat.mutation.trace.handlers;

import java.io.IOException;
import java.util.List;

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
import microbat.util.IProjectUtils;
import microbat.util.JavaUtil;
import microbat.util.WorkbenchUtils;

public class EvaluateMutationRegressionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Evaluate Mutations") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					MutationRegressionSettings mutationSettings = MutationRegressionPreference.getMutationRegressionSettings();
					if (mutationSettings.isRunAllProjectsInWorkspace()) {
						String[] allProjects = WorkbenchUtils.getProjectsInWorkspace();
						for (String targetProject : allProjects) {
							evaluateMutations(targetProject, mutationSettings, monitor);
						}
					} else {
						String targetProject = mutationSettings.getTargetProject();
						evaluateMutations(targetProject, mutationSettings, monitor);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("Complete Mutation Simulator!");
				return Status.OK_STATUS; 
			}

		};
		job.schedule();
		return null;
	}

	private void evaluateMutations(String targetProject, MutationRegressionSettings mutationSettings,
			IProgressMonitor monitor) throws IOException {
		List<String> allBugIds = MutationCase.loadAllMutationBugIds(targetProject,
				mutationSettings.getMutationOutputSpace());
		MutationEvaluator mutationEvaluator = new MutationEvaluator();
		AnalysisParams analysisParams = new AnalysisParams(mutationSettings);
		MutationExperimentMonitor mutationMonitor = new MutationExperimentMonitor(monitor, targetProject, analysisParams);
		String projectFolder = IProjectUtils.getProjectFolder(JavaUtil.getSpecificJavaProjectInWorkspace(targetProject));
		for (String bugId : allBugIds) {
			try {
				MutationCase mutationCase = MutationCase.load(targetProject, bugId,
						mutationSettings.getMutationOutputSpace(), analysisParams, projectFolder);
				mutationCase.getTestcaseParams().setAnalysisParams(analysisParams);
				mutationEvaluator.runSingleMutationTrial(mutationCase, mutationMonitor);
				if (monitor.isCanceled()) {
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
