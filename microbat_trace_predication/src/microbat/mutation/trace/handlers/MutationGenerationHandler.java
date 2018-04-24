package microbat.mutation.trace.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import microbat.mutation.trace.MutationGenerator;
import microbat.mutation.trace.dto.AnalysisParams;
import microbat.mutation.trace.preference.MutationRegressionPreference;
import microbat.mutation.trace.preference.MutationRegressionSettings;
import microbat.mutation.trace.report.MutationExperimentMonitor;
import microbat.util.JavaUtil;
import microbat.util.WorkbenchUtils;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MutationGenerationHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Generate Mutations") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					MutationRegressionSettings mutationSettings = MutationRegressionPreference.getMutationRegressionSettings();
					if (mutationSettings.isRunAllProjectsInWorkspace()) {
						String[] allProjects = WorkbenchUtils.getProjectsInWorkspace();
						for (String targetProject : allProjects) {
							generateMutations(targetProject, mutationSettings, monitor);
						}
					} else {
						String targetProject = mutationSettings.getTargetProject();
						generateMutations(targetProject, mutationSettings, monitor);
					}
				} catch (JavaModelException | IOException e) {
					e.printStackTrace();
				}
				System.out.println("Complete Mutation Generation!");
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

	private void generateMutations(String targetProject, MutationRegressionSettings mutationSettings, IProgressMonitor monitor) throws IOException, JavaModelException {
		AnalysisParams analysisParams = new AnalysisParams(mutationSettings);
		MutationExperimentMonitor experimentMonitor = new MutationExperimentMonitor(monitor, targetProject,
				analysisParams);
		MutationGenerator mutationGenerator = new MutationGenerator();
		IPackageFragmentRoot testRoot = JavaUtil.findTestPackageRootInProject(targetProject);
		if (testRoot == null) {
			return;
		}
		for (IJavaElement element : testRoot.getChildren()) {
			if (element instanceof IPackageFragment) {
				mutationGenerator.generateMutations((IPackageFragment) element, analysisParams, experimentMonitor);
			}
		}
	}
}
