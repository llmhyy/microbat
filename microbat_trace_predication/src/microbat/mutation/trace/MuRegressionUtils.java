package microbat.mutation.trace;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;

import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.mutation.trace.dto.AnalysisTestcaseParams;
import microbat.util.IResourceUtils;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;

public class MuRegressionUtils {
	public static final String TEST_RUNNER = "microbat.evaluation.junit.MicroBatTestRunner";
	
	private MuRegressionUtils(){}
	
	public static void fillMuBkpJavaFilePath(Trace buggyTrace, String muJFilePath, String muClassName) {
		for (TraceNode node : buggyTrace.getExecutionList()) {
			BreakPoint point = node.getBreakPoint();
			if (muClassName.equals(point.getDeclaringCompilationUnitName())) {
				point.setFullJavaFilePath(muJFilePath);
			}
		}
	}
	
	public static AppJavaClassPath createProjectClassPath(AnalysisTestcaseParams params){
		AppJavaClassPath classPath = MicroBatUtil.constructClassPaths(params.getProjectName());
		classPath.setTestCodePath(getSourceFolder(params.getJunitClassName(), params.getProjectName()));
		List<String> srcFolders = MicroBatUtil.getSourceFolders(params.getProjectName());
		classPath.setSourceCodePath(classPath.getTestCodePath());
		for (String srcFolder : srcFolders) {
			if (!srcFolder.equals(classPath.getTestCodePath())) {
				classPath.getAdditionalSourceFolders().add(srcFolder);
			}
		}
		
		String userDir = System.getProperty("user.dir");
		String junitDir = userDir + File.separator + "dropins" + File.separator + "junit_lib";
		
		String junitPath = junitDir + File.separator + "junit.jar";
		String hamcrestCorePath = junitDir + File.separator + "org.hamcrest.core.jar";
		String testRunnerPath = junitDir  + File.separator + "testrunner.jar";
		
		classPath.addClasspath(junitPath);
		classPath.addClasspath(hamcrestCorePath);
		classPath.addClasspath(testRunnerPath);
		
		classPath.addClasspath(junitDir);
		
		classPath.setOptionalTestClass(params.getJunitClassName());
		classPath.setOptionalTestMethod(params.getTestMethod());
		
		classPath.setLaunchClass(TEST_RUNNER);
		return classPath;
	}
	
	public static String getSourceFolder(String cName, String projectName) {
		ICompilationUnit unit = JavaUtil.findICompilationUnitInProject(cName, projectName);
		IPath uri = unit.getResource().getFullPath();
		String sourceFolderPath = IResourceUtils.getAbsolutePathOsStr(uri);
		cName = cName.substring(0, cName.lastIndexOf(".")).replace(".", File.separator);
		sourceFolderPath = sourceFolderPath.substring(0, sourceFolderPath.indexOf(cName) - 1);
		return sourceFolderPath;
	}
	
	public static String getMutationCaseFilePath(String projectName, String mutationOutputSpace) {
		return FileUtils.getFilePath(mutationOutputSpace, "mutation", projectName, "mutationCases.csv");
	}
	
	public static String getValidMutationCaseFilePath(String projectName, String mutationOutputSpace) {
		return FileUtils.getFilePath(mutationOutputSpace, "mutation", projectName, "validMutationCases.csv");
	}
	
	public static String getAnalysisOutputFolder(String mutationOutputSpace, String projectName, String junitClassName,
			String testMethod) {
		return FileUtils.getFilePath(mutationOutputSpace, "mutation", projectName,
				ClassUtils.getSimpleName(junitClassName), testMethod);
	}
	
}
