package microbat.instrumentation.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sav.strategies.dto.AppJavaClassPath;

public class ApplicationUtility {
	
	public static List<String> lookupAppBinaryFolders(AppJavaClassPath appClasspath) {
		List<String> appBinFolders = new ArrayList<>();
		String workingDir = getPath(appClasspath.getWorkingDirectory());
		for (String cp : appClasspath.getClasspaths()) {
			String path = getPath(cp);
			if (path.contains(workingDir)) {
				File binFolder = new File(cp);
				if (binFolder.exists() && binFolder.isDirectory()) {
					path = getDir(path);
					appBinFolders.add(path);
				}
			}
		}
		return appBinFolders;
	}
	
	private static String getDir(String path) {
		if (!path.endsWith("/")) {
			return path + "/";
		}
		return path;
	}
	
	private static String getPath(String cp) {
		String path = cp;
		path = path.replace("\\", "/");
		if(path.startsWith("/")){
			path = path.substring(1, path.length());
		}
		return path;
	}
}
