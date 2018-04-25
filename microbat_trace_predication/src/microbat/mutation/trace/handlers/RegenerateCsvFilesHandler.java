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

import microbat.mutation.trace.CsvRegenerator;
import microbat.mutation.trace.dto.AnalysisParams;
import microbat.mutation.trace.dto.MutationCase;
import microbat.mutation.trace.preference.MutationRegressionPreference;
import microbat.mutation.trace.preference.MutationRegressionSettings;
import microbat.mutation.trace.report.MutationExperimentMonitor;
import microbat.util.IProjectUtils;
import microbat.util.JavaUtil;
import microbat.util.WorkbenchUtils;

public class RegenerateCsvFilesHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Regenerate csv files") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					MutationRegressionSettings mutationSettings = MutationRegressionPreference.getMutationRegressionSettings();
					if (mutationSettings.isRunAllProjectsInWorkspace()) {
						String[] allProjects = WorkbenchUtils.getProjectsInWorkspace();
						for (String targetProject : allProjects) {
							rerun(targetProject, mutationSettings, monitor);
						}
					} else {
						String targetProject = mutationSettings.getTargetProject();
						rerun(targetProject, mutationSettings, monitor);
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

	private void rerun(String targetProject, MutationRegressionSettings mutationSettings,
			IProgressMonitor monitor) throws IOException {
		AnalysisParams analysisParams = new AnalysisParams(mutationSettings);
		MutationExperimentMonitor mutationMonitor = new MutationExperimentMonitor(monitor, targetProject, analysisParams);
		String projectFolder = IProjectUtils.getProjectFolder(JavaUtil.getSpecificJavaProjectInWorkspace(targetProject));
		CsvRegenerator generator = new CsvRegenerator();
		List<MutationCase> mutationCases = MutationCase.loadAllMutationCases(targetProject, mutationSettings.getMutationOutputSpace(), analysisParams, projectFolder);
		for (MutationCase mutationCase : mutationCases) {
			try {
				if (monitor.isCanceled()) {
					return;
				}
				if (!mutationCase.isValid()) {
					continue;
				}
				mutationCase.getTestcaseParams().setAnalysisParams(analysisParams);
				generator.regenerate(mutationCase, mutationMonitor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
