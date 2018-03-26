package microbat.mutation.trace.handlers;

import java.io.File;
import java.io.FilenameFilter;
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

import microbat.mutation.trace.MutationExperimentator;
import microbat.mutation.trace.dto.AnalysisParams;
import microbat.mutation.trace.preference.MutationRegressionPreference;
import microbat.mutation.trace.report.MutationExperimentMonitor;
import microbat.util.IResourceUtils;
import microbat.util.JavaUtil;
import sav.common.core.utils.FileUtils;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MutationGenerationHandler extends AbstractHandler {
	@Deprecated
	public static final String TMP_DIRECTORY;

	static {
		File resultFolder = new File(
				IResourceUtils.getResourceAbsolutePath("microbat_trace_predication", "mutation_result"));
		TMP_DIRECTORY = resultFolder.getAbsolutePath();
		FileUtils.deleteAllFiles(TMP_DIRECTORY, new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return !".gitkeep".equals(name);
			}
		});
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AnalysisParams analysisParams = new AnalysisParams();
		Job job = new Job("Do evaluation") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String targetProject = MutationRegressionPreference.getTargetProject();
				try {
					MutationExperimentMonitor experimentMonitor = new MutationExperimentMonitor(monitor, targetProject,
							analysisParams);
					MutationExperimentator analyzer = new MutationExperimentator();
					IPackageFragmentRoot testRoot = JavaUtil.findTestPackageRootInProject(targetProject);

					for (IJavaElement element : testRoot.getChildren()) {
						if (element instanceof IPackageFragment) {
							analyzer.runEvaluation((IPackageFragment) element, analysisParams, experimentMonitor);
						}
					}
				} catch (JavaModelException | IOException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

}
