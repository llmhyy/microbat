package microbat.mutation.trace.handlers;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import microbat.Activator;
import microbat.agent.ExecTraceFileReader;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.mutation.trace.MuDiffMatcher;
import microbat.mutation.trace.MuRegression;
import microbat.mutation.trace.MuRegressionUtils;
import microbat.mutation.trace.preference.MuRegressionPreference;
import microbat.util.IResourceUtils;
import microbat.util.MicroBatUtil;
import sav.common.core.SavRtException;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;
import tregression.empiricalstudy.Regression;
import tregression.empiricalstudy.TestCase;
import tregression.model.PairList;
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
			MuRegression muRegression = loadMuRegression(projectName, bugExecPath);
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
			fillMuBkpJavaFilePath(buggyTrace, muJFilePath, muRegression.getMutationClassName());
			regression.fillMissingInfor(correctTrace, fixClasspath);
			regression.fillMissingInfor(buggyTrace, buggyClasspath);
			
			PairList pairList = buildPairList(correctTrace, buggyTrace, diffMatcher);
			Visualizer visualizer = new Visualizer();
			visualizer.visualize(buggyTrace, correctTrace, pairList, diffMatcher);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		return null;
	}

	private MuRegression loadMuRegression(String projectName, String bugExec) throws SQLException {
		String[] bugExecFrags = bugExec.replace("\\", "/").split("/");
		/* fix.exec path */
		String tcFolder = FileUtils.getFilePath(Arrays.copyOf(bugExecFrags, bugExecFrags.length - 2));
		String fixExec = FileUtils.getFilePath(tcFolder, FIX_EXEC_FILE_NAME);
		
		/* testcase */
		TestCase tc = new TestCase(bugExecFrags[bugExecFrags.length - 3]);
		
		/* class Simple Name */
		String classSimpleName = bugExecFrags[bugExecFrags.length - 2].split("_")[1];
		
		File muFile = FileUtils.getFileEndWith(
				FileUtils.getFilePath(CollectionUtils.toArrayList(bugExecFrags, bugExecFrags.length - 1)),
				classSimpleName + ".java");
		if (muFile == null) {
			throw new SavRtException("Cannot file mutated java file");
		}
		/* class Name */
		String className = muFile.getName();
		int eIdx = className.lastIndexOf(".");
		if (eIdx > 0) {
			className = className.substring(0, eIdx);
		}
		/* original java file */
		File orgFile = new File(FileUtils.getFilePath(tcFolder, className + ".java"));
		
		/* build trace from exec files */
		ExecTraceFileReader execTraceReader = new ExecTraceFileReader();
		Trace buggyTrace = execTraceReader.read(bugExec);
		buggyTrace.setSourceVersion(true);
		Trace fixTrace = execTraceReader.read(fixExec);
		fixTrace.setSourceVersion(false);
		Regression regression = new Regression(buggyTrace, fixTrace, null);
		regression.setTestCase(tc.testClass, tc.testMethod);
		
		/* MuRegression */
		MuRegression muRegression = new MuRegression();
		muRegression.setRegression(regression);
		muRegression.setMutationFile(muFile.getAbsolutePath());
		muRegression.setMutationClassName(className);
		muRegression.setOrgFile(orgFile.getAbsolutePath());
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
	
	public void fillMuBkpJavaFilePath(Trace buggyTrace, String muJFilePath, String muClassName) {
		for (TraceNode node : buggyTrace.getExecutionList()) {
			BreakPoint point = node.getBreakPoint();
			if (muClassName.equals(point.getDeclaringCompilationUnitName())) {
				point.setFullJavaFilePath(muJFilePath);
			}
		}
	}
	
	private AppJavaClassPath initAppClasspath(String projectName) {
		AppJavaClassPath appClasspath = MicroBatUtil.constructClassPaths(projectName);
		return appClasspath;
	}
}
