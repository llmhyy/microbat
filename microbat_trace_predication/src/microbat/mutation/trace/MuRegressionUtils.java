package microbat.mutation.trace;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.util.MicroBatUtil;
import sav.common.core.Constants;
import sav.common.core.utils.FileUtils;

public class MuRegressionUtils {
	private MuRegressionUtils(){}
	
	public static List<String> getMuTraceExecs(String targetProject) {
		String folderPath = sav.common.core.utils.FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), targetProject);
		File folder = new File(folderPath);
		if (!folder.exists() || !folder.isDirectory()) {
			return Collections.emptyList();
		}
		List<String> allFiles = new ArrayList<>();
		collectBugExecFiles(folder, allFiles);
		return allFiles;
	}
	
	public static String extractMuBugId(String execFilePath) {
		int muIdx = execFilePath.indexOf("mu_");
		if (muIdx < 0) {
			return "";
		}
		String bugId = execFilePath.substring(muIdx + 3 /*mu_.length*/);
		bugId = bugId.substring(0, bugId.indexOf(File.separator));
		return bugId;
	}

	private static void collectBugExecFiles(File folder, List<String> allFiles) {
		folder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (!dir.getName().startsWith("mu_")) {
					return false;
				}
				if (name.equals("bug.exec")) {
					allFiles.add(FileUtils.getFilePath(dir.getAbsolutePath(), name));
					return true;
				}
				return false;
			}
		});
		for (File subFile : folder.listFiles()) {
			if (subFile.isDirectory()) {
				collectBugExecFiles(subFile, allFiles);
			}
		}
	}
	
	public static String getMuBugId(String mutationFilePath) {
		int endIdx = mutationFilePath.lastIndexOf(Constants.FILE_SEPARATOR);
		int startIdx = mutationFilePath.substring(0, endIdx).lastIndexOf(Constants.FILE_SEPARATOR) + 1;
		// org.apache.commons.math.analysis.interpolation.BicubicSplineInterpolator_82_13_1
		String className = mutationFilePath.substring(startIdx, endIdx);
		startIdx = className.lastIndexOf(".") + 1;
		return "mu_" + className.substring(startIdx);
	}
	
	public static void fillMuBkpJavaFilePath(Trace buggyTrace, String muJFilePath, String muClassName) {
		for (TraceNode node : buggyTrace.getExecutionList()) {
			BreakPoint point = node.getBreakPoint();
			if (muClassName.equals(point.getDeclaringCompilationUnitName())) {
				point.setFullJavaFilePath(muJFilePath);
			}
		}
	}
}
