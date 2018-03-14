package microbat.mutation.trace.handlers;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import microbat.Activator;
import microbat.agent.ExecTraceFileReader;
import microbat.model.trace.Trace;
import microbat.mutation.trace.MuBugInfo;
import microbat.mutation.trace.MuDiffMatcher;
import microbat.mutation.trace.MuRegression;
import microbat.mutation.trace.MuRegressionUtils;
import microbat.mutation.trace.preference.MuRegressionPreference;
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

public class MuRegressionRetrieverHandler extends AbstractHandler {
	private static final String FIX_EXEC_FILE_NAME = "fix.exec";
	private static final String MU_EXEC_FILE_NAME = "bug.exec";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String projectName = Activator.getDefault().getPreferenceStore().getString(MuRegressionPreference.TARGET_PROJECT_KEY);
		String bugExecPath = Activator.getDefault().getPreferenceStore().getString(MuRegressionPreference.BUG_ID_KEY);
		String bugId = MuRegressionUtils.extractMuBugId(bugExecPath);
		try {
			MuBugInfo muBugInfo = MuBugInfo.parse(bugExecPath);
			MuRegression muRegression = loadMuRegression(projectName, muBugInfo);
			Regression regression = muRegression.getRegression();
			Trace buggyTrace = regression.getBuggyTrace();
			Trace correctTrace = regression.getCorrectTrace();

			AppJavaClassPath buggyClasspath = initAppClasspath(projectName);
			AppJavaClassPath fixClasspath = initAppClasspath(projectName);
			buggyTrace.setAppJavaClassPath(buggyClasspath);
			correctTrace.setAppJavaClassPath(fixClasspath);
			
			/* init path for diffMatcher */
			String muPath = IResourceUtils.getFolderPath(projectName, "microbat/mutation/" + bugId + "/bug");
//			String orgPath = IResourceUtils.getFolderPath(projectName, "microbat/mutation/" + bugId + "/fix");
//			String srcFolder = "src";
			String orgPath = IResourceUtils.getProjectPath(projectName);
			String srcFolder = IResourceUtils.getRelativeSourceFolderPath(orgPath, projectName,
					muRegression.getMutationClassName());
			String testFolder = IResourceUtils.getRelativeSourceFolderPath(orgPath, projectName, regression.getTestClass());
			String orgJFilePath = ClassUtils.getJFilePath(FileUtils.getFilePath(orgPath, srcFolder), muRegression.getMutationClassName());
			String muJFilePath = ClassUtils.getJFilePath(FileUtils.getFilePath(muPath, srcFolder), muRegression.getMutationClassName());
			FileUtils.copyFile(muRegression.getMutationFile(), muJFilePath, true);
			
			MuDiffMatcher diffMatcher = new MuDiffMatcher(srcFolder, orgJFilePath, muJFilePath);
			diffMatcher.setBuggyPath(muPath);
			diffMatcher.setFixPath(orgPath);
			diffMatcher.setTestFolderName(testFolder);
			diffMatcher.matchCode();
			// fill breakpoint
			buggyClasspath.setSourceCodePath(FileUtils.getFilePath(orgPath, srcFolder));
			buggyClasspath.setTestCodePath(FileUtils.getFilePath(orgPath, testFolder));
			fixClasspath.setSourceCodePath(FileUtils.getFilePath(orgPath, srcFolder));
			fixClasspath.setTestCodePath(FileUtils.getFilePath(orgPath, testFolder));
			MuRegressionUtils.fillMuBkpJavaFilePath(buggyTrace, muJFilePath, muRegression.getMutationClassName());
			Regression.fillMissingInfor(correctTrace, fixClasspath);
			Regression.fillMissingInfor(buggyTrace, buggyClasspath);
			
			PairList pairList = buildPairList(correctTrace, buggyTrace, diffMatcher);
			Visualizer visualizer = new Visualizer();
			visualizer.visualize(buggyTrace, correctTrace, pairList, diffMatcher);
			
			try {
				EmpiricalTrial trial = simulate(buggyTrace, correctTrace, pairList, diffMatcher);
				System.out.println(trial);
			} catch (SimulationFailException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		return null;
	}
	
	private EmpiricalTrial simulate(Trace buggyTrace, Trace correctTrace, PairList pairList, DiffMatcher diffMatcher)
			throws SimulationFailException {
		long time1 = System.currentTimeMillis();
		System.out.println("start simulating debugging...");
		Simulator simulator = new Simulator();
		simulator.prepare(buggyTrace, correctTrace, pairList, diffMatcher);
//		TraceNode node = buggyTrace.getExecutionList().get(8667);
//		simulator.setObservedFault(node);

		RootCauseFinder rootcauseFinder = new RootCauseFinder();
		rootcauseFinder.setRootCauseBasedOnDefects4J(pairList, diffMatcher, buggyTrace, correctTrace);

		if (rootcauseFinder.getRealRootCaseList().isEmpty()) {
			EmpiricalTrial trial = EmpiricalTrial.createDumpTrial("cannot find real root cause");
			if (buggyTrace.isMultiThread() || correctTrace.isMultiThread()) {
				trial.setMultiThread(true);
				StepOperationTuple tuple = new StepOperationTuple(simulator.getObservedFault(),
						new UserFeedback(UserFeedback.UNCLEAR), simulator.getObservedFault(), DebugState.UNCLEAR);
				trial.getCheckList().add(tuple);
			}

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

	private MuRegression loadMuRegression(String projectName, MuBugInfo muBugInfo) throws SQLException {
		/* build trace from exec files */
		ExecTraceFileReader execTraceReader = new ExecTraceFileReader();
		Trace buggyTrace = execTraceReader.read(muBugInfo.getBugExec());
		buggyTrace.setSourceVersion(true);
		Trace fixTrace = execTraceReader.read(muBugInfo.getFixExec());
		fixTrace.setSourceVersion(false);
		Regression regression = new Regression(buggyTrace, fixTrace, null);
		regression.setTestCase(muBugInfo.getTc().testClass, muBugInfo.getTc().testMethod);
		
		/* MuRegression */
		MuRegression muRegression = new MuRegression();
		muRegression.setRegression(regression);
		muRegression.setMutationFile(muBugInfo.getMuFile().getAbsolutePath());
		muRegression.setMutationClassName(muBugInfo.getClassName());
		muRegression.setOrgFile(muBugInfo.getOrgFile().getAbsolutePath());
		return muRegression;
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
