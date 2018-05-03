package microbat.mutation.trace.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import microbat.model.trace.Trace;
import microbat.mutation.trace.MuDiffMatcher;
import microbat.mutation.trace.MutationEvaluator;
import microbat.mutation.trace.dto.AnalysisParams;
import microbat.mutation.trace.dto.MutationCase;
import microbat.mutation.trace.dto.MutationExecutionResult;
import microbat.mutation.trace.dto.SingleMutation;
import microbat.mutation.trace.preference.MutationRegressionPreference;
import microbat.mutation.trace.preference.MutationRegressionSettings;
import microbat.mutation.trace.report.MutationExperimentMonitor;
import microbat.util.IProjectUtils;
import microbat.util.IResourceUtils;
import microbat.util.JavaUtil;
import sav.common.core.utils.ClassUtils;
import tregression.model.PairList;
import tregression.separatesnapshots.DiffMatcher;
import tregression.tracematch.ControlPathBasedTraceMatcher;
import tregression.views.Visualizer;

public class RunSingleMutationHandler  extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Evaluate Mutations") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					runSingleMutation(monitor);
					System.out.println("Finish!");
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
		String projectFolder = IProjectUtils.getProjectFolder(JavaUtil.getSpecificJavaProjectInWorkspace(settings.getTargetProject()));
		MutationCase mutationCase = MutationCase.load(settings.getTargetProject(), settings.getBugId(),
				settings.getMutationOutputSpace(), analysisParams, projectFolder);
		MutationEvaluator evaluator = new MutationEvaluator();
		MutationExperimentMonitor mutationMonitor = new MutationExperimentMonitor(monitor, settings.getTargetProject(), analysisParams);
		MutationExecutionResult result = evaluator.runSingleMutationTrial(mutationCase, mutationMonitor);
		
		System.out.println(result.getTrial());
		
		/* init path for diffMatcher */
		MuDiffMatcher diffMatcher = initDiffMatcher(mutationCase);
		
		Visualizer visualizer = new Visualizer();
		Trace buggyTrace = result.getBugTrace();
		Trace correctTrace = result.getCorrectTrace();
		PairList pairList = buildPairList(correctTrace, buggyTrace, diffMatcher);
		visualizer.visualize(buggyTrace, correctTrace, pairList, diffMatcher);
	}

	public static MuDiffMatcher initDiffMatcher(MutationCase mutationCase) {
		String targetProject = mutationCase.getTestcaseParams().getProjectName();
		String orgPath = IResourceUtils.getProjectPath(targetProject);
		SingleMutation mutation = mutationCase.getMutation();
		String srcFolder = mutation.getSourceFolder();
		String testFolder = IResourceUtils.getRelativeSourceFolderPath(orgPath, targetProject, mutationCase.getTestcaseParams().getJunitClassName());
		String orgJFilePath = ClassUtils.getJFilePath(srcFolder, mutation.getMutatedClass());
		String muJFilePath = mutation.getFile().getAbsolutePath();
		
		MuDiffMatcher diffMatcher = new MuDiffMatcher(srcFolder, orgJFilePath, muJFilePath);
		diffMatcher.setBuggyPath(orgPath);
		diffMatcher.setFixPath(orgPath);
		diffMatcher.setTestFolderName(testFolder);
		diffMatcher.matchCode();
		return diffMatcher;
	}
	
	public static PairList buildPairList(Trace correctTrace, Trace buggyTrace, DiffMatcher diffMatcher) {
		/* PairList */
		System.out.println("start matching trace..., buggy trace length: " + buggyTrace.size()
				+ ", correct trace length: " + correctTrace.size());
		long time1 = System.currentTimeMillis();

		ControlPathBasedTraceMatcher traceMatcher = new ControlPathBasedTraceMatcher();
		PairList pairList = traceMatcher.matchTraceNodePair(buggyTrace, correctTrace, diffMatcher);
		long time2 = System.currentTimeMillis();
		int matchTime = (int) (time2 - time1);
		System.out.println("finish matching trace, taking " + matchTime + "ms");
		return pairList;
	}
}
