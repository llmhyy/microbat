package microbat.mutation.trace.handlers;

import java.sql.SQLException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import microbat.Activator;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.mutation.trace.MuDiffMatcher;
import microbat.mutation.trace.MuRegression;
import microbat.mutation.trace.MuRegressionRetriever;
import microbat.mutation.trace.preference.MutationRegressionPreference;
import microbat.util.IResourceUtils;
import microbat.util.MicroBatUtil;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;
import tregression.empiricalstudy.Regression;
import tregression.model.PairList;
import tregression.views.Visualizer;

public class MuRegressionRetrieverHandlerOld extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String projectName = Activator.getDefault().getPreferenceStore().getString(MutationRegressionPreference.TARGET_PROJECT_KEY);
		String bugId = Activator.getDefault().getPreferenceStore().getString(MutationRegressionPreference.BUG_ID_KEY);
		
		try {
			MuRegression muRegression = new MuRegressionRetriever().retrieveRegression(projectName, bugId);
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
//			FileUtils.writeFile(orgJFilePath, muRegression.getOrginalCode());
			FileUtils.writeFile(muJFilePath, muRegression.getMutationCode());
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
			regression.fillMissingInfo(correctTrace, fixClasspath);
			regression.fillMissingInfo(buggyTrace, buggyClasspath);
			PairList pairList = regression.getPairList();
			Visualizer visualizer = new Visualizer();
			visualizer.visualize(buggyTrace, correctTrace, pairList, diffMatcher);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		return null;
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
