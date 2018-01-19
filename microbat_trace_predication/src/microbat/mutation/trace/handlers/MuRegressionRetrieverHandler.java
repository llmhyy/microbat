package microbat.mutation.trace.handlers;

import java.net.URI;
import java.sql.SQLException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;

import microbat.Activator;
import microbat.model.trace.Trace;
import microbat.mutation.trace.MuDiffMatcher;
import microbat.mutation.trace.MuRegression;
import microbat.mutation.trace.MuRegressionRetriever;
import microbat.mutation.trace.preference.MuRegressionPreference;
import microbat.util.JavaUtil;
import sav.strategies.dto.AppJavaClassPath;
import tregression.empiricalstudy.Regression;
import tregression.model.PairList;
import tregression.views.Visualizer;

public class MuRegressionRetrieverHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String projectName = Activator.getDefault().getPreferenceStore().getString(MuRegressionPreference.TARGET_PROJECT_KEY);;
		String bugId = Activator.getDefault().getPreferenceStore().getString(MuRegressionPreference.BUG_ID_KEY);;
		
		try {
			MuRegression muRegression = new MuRegressionRetriever().retrieveRegression(projectName, bugId);
			Regression regression = muRegression.getRegression();
			Trace buggyTrace = regression.getBuggyTrace();
			Trace correctTrace = regression.getCorrectTrace();

			AppJavaClassPath buggyApp = initAppClasspath();
			AppJavaClassPath fixApp = initAppClasspath();
			buggyTrace.setAppJavaClassPath(buggyApp);
			correctTrace.setAppJavaClassPath(fixApp);
			String sourceFolder = getSourceFolder(projectName, regression.getTestClass());
			MuDiffMatcher diffMatcher = new MuDiffMatcher(sourceFolder, muRegression.getOrginalFile().getAbsolutePath(),
					muRegression.getMutationFile().getAbsolutePath());
			diffMatcher.matchCode();
			PairList pairList = regression.getPairList();
//			regression.fillMissingInfor(config, buggyPath, fixPath);
			Visualizer visualizer = new Visualizer();
			visualizer.visualize(buggyTrace, correctTrace, pairList, diffMatcher);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		return null;
	}
	
	private String getSourceFolder(String projectName, String cName) {
		ICompilationUnit unit = JavaUtil.findICompilationUnitInProject(cName, projectName);
		URI uri = unit.getResource().getLocationURI();
		String sourceFolderPath = uri.toString();
		cName = cName.replace(".", "/") + ".java";
		sourceFolderPath = sourceFolderPath.substring(0, sourceFolderPath.indexOf(cName));
		sourceFolderPath = sourceFolderPath.substring(5, sourceFolderPath.length());
		return sourceFolderPath;
	}

	private AppJavaClassPath initAppClasspath() {
		AppJavaClassPath appClasspath = new AppJavaClassPath();
		return appClasspath;
	}
}
