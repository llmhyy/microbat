package microbat.concurrent.generators;

import java.util.List;

import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.preference.AnalysisScopePreference;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;

public class ExecutionInfo {
		final AppJavaClassPath appJavaClassPath;
		final List<String> includedClass;
		final List<String> excludedClass;
		public ExecutionInfo(AppJavaClassPath classPath, List<String> includedClass, List<String> excludedClass) {
			this.includedClass = includedClass;
			this.appJavaClassPath = classPath;
			this.excludedClass = excludedClass;
		}
		
		public static ExecutionInfo getFromMicrobat() {
			final AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
			if (Settings.isRunTest) {
				appClassPath.setOptionalTestClass(Settings.launchClass);
				appClassPath.setOptionalTestMethod(Settings.testMethod);
				appClassPath.setLaunchClass(TestCaseAnalyzer.TEST_RUNNER);
				appClassPath.setTestCodePath(MicroBatUtil.getSourceFolder(Settings.launchClass, Settings.projectName));
			}
			List<String> srcFolders = MicroBatUtil.getSourceFolders(Settings.projectName);
			appClassPath.setSourceCodePath(appClassPath.getTestCodePath());
			for (String srcFolder : srcFolders) {
				if (!srcFolder.equals(appClassPath.getTestCodePath())) {
					appClassPath.getAdditionalSourceFolders().add(srcFolder);
				}
			}
			List<String> includedClassNames = AnalysisScopePreference.getIncludedLibList();
			List<String> excludedClassNames = AnalysisScopePreference.getExcludedLibList();
			return new ExecutionInfo(appClassPath, includedClassNames, excludedClassNames);
		}
		protected String generateTraceDir(AppJavaClassPath appPath) {
			String traceFolder;
			if (appPath.getOptionalTestClass() != null) {
				traceFolder = FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), 
						Settings.projectName,
						appPath.getOptionalTestClass(), 
						appPath.getOptionalTestMethod());
			} else {
				traceFolder = FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), 
						Settings.projectName, 
						appPath.getLaunchClass()); 
			}
			FileUtils.createFolder(traceFolder);
			return traceFolder;
		}
		
		public InstrumentationExecutor getDefaultExecutor() {
			return new InstrumentationExecutor(appJavaClassPath, generateTraceDir(appJavaClassPath), 
					includedClass, excludedClass);
		}
		
	}