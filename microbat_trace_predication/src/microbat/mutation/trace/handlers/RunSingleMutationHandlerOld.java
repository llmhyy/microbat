package microbat.mutation.trace.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import microbat.model.trace.Trace;
import microbat.mutation.trace.AppJavaClassPathWrapper;
import microbat.mutation.trace.MuDiffMatcher;
import microbat.mutation.trace.MuRegression;
import microbat.mutation.trace.MuRegressionUtils;
import microbat.mutation.trace.MutationRegressionRetriever;
import microbat.mutation.trace.dto.BackupClassFiles;
import microbat.mutation.trace.preference.MutationRegressionPreference;
import microbat.mutation.trace.preference.MutationRegressionSettings;
import microbat.recommendation.DebugState;
import microbat.recommendation.UserFeedback;
import microbat.util.IResourceUtils;
import microbat.util.MicroBatUtil;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;
import tregression.SimulationFailException;
import tregression.empiricalstudy.EmpiricalTrial;
import tregression.empiricalstudy.Regression;
import tregression.empiricalstudy.RootCauseFinder;
import tregression.empiricalstudy.Simulator;
import tregression.empiricalstudy.solutionpattern.PatternIdentifier;
import tregression.model.PairList;
import tregression.model.StepOperationTuple;
import tregression.separatesnapshots.DiffMatcher;
import tregression.tracematch.ControlPathBasedTraceMatcher;
import tregression.views.Visualizer;

public class RunSingleMutationHandlerOld  extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean useSliceBreaker = true;
		boolean enableRandom = false;
		int breakerLimit = 3;
		
		
		Job job = new Job("Run single mutation") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				BackupClassFiles bkClassFiles = null;
				try {
					MutationRegressionSettings mutationSettings = MutationRegressionPreference.getMutationRegressionSettings();
					String muBugId = mutationSettings.getBugId();
					String targetProject = mutationSettings.getTargetProject();
					
					MutationRegressionRetriever retriever = new MutationRegressionRetriever();
					MuRegression muRegression = retriever.retrieveRegression(targetProject, muBugId, monitor, useSliceBreaker, breakerLimit);
					Regression regression = muRegression.getRegression();
					Trace buggyTrace = regression.getBuggyTrace();
					Trace correctTrace = regression.getCorrectTrace();

					AppJavaClassPath buggyClasspath = initAppClasspath(targetProject);
					AppJavaClassPath fixClasspath = initAppClasspath(targetProject);
					buggyTrace.setAppJavaClassPath(buggyClasspath);
					correctTrace.setAppJavaClassPath(fixClasspath);
					bkClassFiles = muRegression.getMutationCase().getTestcaseParams().getBkClassFiles();
					
					/* init path for diffMatcher */
					String orgPath = IResourceUtils.getProjectPath(targetProject);
					String srcFolder = IResourceUtils.getRelativeSourceFolderPath(orgPath, targetProject,
							muRegression.getMutationClassName());
					String testFolder = IResourceUtils.getRelativeSourceFolderPath(orgPath, targetProject, regression.getTestClass());
					String orgJFilePath = ClassUtils.getJFilePath(FileUtils.getFilePath(orgPath, srcFolder), muRegression.getMutationClassName());
					String muJFilePath = muRegression.getMutationFile();
					
					MuDiffMatcher diffMatcher = new MuDiffMatcher(srcFolder, orgJFilePath, muJFilePath);
					diffMatcher.setBuggyPath(orgPath);
					diffMatcher.setFixPath(orgPath);
					diffMatcher.setTestFolderName(testFolder);
					diffMatcher.matchCode();
					// fill breakpoint
					buggyClasspath.setSourceCodePath(FileUtils.getFilePath(orgPath, srcFolder));
					buggyClasspath.setTestCodePath(FileUtils.getFilePath(orgPath, testFolder));
					fixClasspath.setSourceCodePath(FileUtils.getFilePath(orgPath, srcFolder));
					fixClasspath.setTestCodePath(FileUtils.getFilePath(orgPath, testFolder));
					MuRegressionUtils.fillMuBkpJavaFilePath(buggyTrace, muJFilePath, muRegression.getMutationClassName());
					Regression.fillMissingInfo(correctTrace, fixClasspath);
					Regression.fillMissingInfo(buggyTrace, buggyClasspath);
					
					AppJavaClassPathWrapper.wrapAppClassPath(buggyTrace, correctTrace, bkClassFiles);
					
					PairList pairList = buildPairList(correctTrace, buggyTrace, diffMatcher);
					Visualizer visualizer = new Visualizer();
					visualizer.visualize(buggyTrace, correctTrace, pairList, diffMatcher);
					try {
						
						EmpiricalTrial trial = simulate(buggyTrace, correctTrace, pairList, 
								diffMatcher, useSliceBreaker, enableRandom, breakerLimit);
						System.out.println(trial);
					} catch (SimulationFailException e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (bkClassFiles != null) {
						bkClassFiles.restoreOrgClassFile();
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}


	private EmpiricalTrial simulate(Trace buggyTrace, Trace correctTrace, PairList pairList, 
			DiffMatcher diffMatcher, boolean useSlicer, boolean enableRandom, int breakerLimit)
			throws SimulationFailException {
		long time1 = System.currentTimeMillis();
		System.out.println("start simulating debugging...");
		Simulator simulator = new Simulator(useSlicer, enableRandom, breakerLimit);
		simulator.prepare(buggyTrace, correctTrace, pairList, diffMatcher);
//		TraceNode node = buggyTrace.getExecutionList().get(8667);
//		simulator.setObservedFault(node);

		RootCauseFinder rootcauseFinder = new RootCauseFinder();
		rootcauseFinder.setRootCauseBasedOnDefects4J(pairList, diffMatcher, buggyTrace, correctTrace);

		if (rootcauseFinder.getRealRootCaseList().isEmpty()) {
			EmpiricalTrial trial = EmpiricalTrial.createDumpTrial("cannot find real root cause");
//			if (buggyTrace.isMultiThread() || correctTrace.isMultiThread()) {
//			}
			trial.setMultiThread(true);
			StepOperationTuple tuple = new StepOperationTuple(simulator.getObservedFault(),
					new UserFeedback(UserFeedback.UNCLEAR), simulator.getObservedFault(), DebugState.UNCLEAR);
			trial.getCheckList().add(tuple);

			return trial;
		}

		if (simulator.getObservedFault() == null) {
			EmpiricalTrial trial = EmpiricalTrial.createDumpTrial("cannot find observable fault");
			return trial;
		}

		List<EmpiricalTrial> trials0 = simulator.detectMutatedBug(buggyTrace, correctTrace, diffMatcher, 0);

		long time2 = System.currentTimeMillis();
		int simulationTime = (int) (time2 - time1);
		System.out.println("finish simulating debugging, taking " + simulationTime / 1000 + "s");

		for (EmpiricalTrial trial : trials0) {
			trial.setTraceCollectionTime(buggyTrace.getConstructTime() + correctTrace.getConstructTime());
			trial.setBuggyTrace(buggyTrace);
			trial.setFixedTrace(correctTrace);
			trial.setPairList(pairList);
			trial.setDiffMatcher(diffMatcher);

			PatternIdentifier identifier = new PatternIdentifier();
			identifier.identifyPattern(trial);
		}

		EmpiricalTrial trial = trials0.get(0);
		return trial;
	}
	
	private PairList buildPairList(Trace correctTrace, Trace buggyTrace, DiffMatcher diffMatcher) {
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
	
	private AppJavaClassPath initAppClasspath(String projectName) {
		AppJavaClassPath appClasspath = MicroBatUtil.constructClassPaths(projectName);
		return appClasspath;
	}
}
