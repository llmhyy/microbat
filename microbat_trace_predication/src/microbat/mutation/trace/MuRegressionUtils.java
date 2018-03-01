package microbat.mutation.trace;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import microbat.util.MicroBatUtil;
import sav.common.core.Constants;
import sav.common.core.utils.FileUtils;

public class MuRegressionUtils {
	private MuRegressionUtils(){}
	
	public static Map<String, String> getMuBugIds(String targetProject) {
		Map<String, String> result = new LinkedHashMap<>();
		String folderPath = sav.common.core.utils.FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), targetProject);
		File folder = new File(folderPath);
		if (!folder.exists() || !folder.isDirectory()) {
			return result;
		}
		List<String> allFiles = new ArrayList<>();
		collectBugExecFiles(folder, allFiles);
		for (String path : allFiles) {
			result.put(extractMuBugId(path), path);
		}
		return result;
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
}
