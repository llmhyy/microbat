/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.security.InvalidParameterException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sav.common.core.Constants;
import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 *
 */
public class IResourceUtils {
	private static Logger log = LoggerFactory.getLogger(IResourceUtils.class);

	private IResourceUtils() {
	}

	private static String DROPINS_DIR;
	private static String ECLIPSE_ROOT_DIR;

	static {
		initEclipseDirs();
	}

	/**
	 * Last_update Aug 18, 2020 3:14:14 PM Last_Modifier SongXuezhi
	 * 
	 * @since LLT Issue #190 Release version not working in Mac os, that cuz the
	 *        method return a wrong dropins path.In fact,the dropins folder is just
	 *        used by developer. When test performance during development,we need
	 *        copy instrumentor.jar to dropins. In the release version,this folder
	 *        is not necessary. Here we first search in the limited path according
	 *        to the system category, if it fails, we traverse and search
	 */
	private static void initEclipseDirs() {
		String eclipseExecutablePath = System.getProperty("eclipse.launcher");
		File dirRoot = null;
		String systemName = System.getProperty("os.name").toLowerCase();
		if (systemName.contains("mac")) {
			/**
			 * In mac os the Eclipse root path is "../Eclipse.app/Contents/", the dropins
			 * path is "../Eclipse.app/Contents/Eclipse/dropins", the eclipse launcher file
			 * is "../Eclipse.app/Contents/MacOS/eclipse", So we fisrt locate
			 * "../Eclipse.app/Contents/" from eclipse.launcher, and see as root dir
			 */
			dirRoot = new File(eclipseExecutablePath).getParentFile().getParentFile();
			ECLIPSE_ROOT_DIR = dirRoot.getAbsolutePath() + File.separator + "Eclipse";
			DROPINS_DIR = ECLIPSE_ROOT_DIR + File.separator + "dropins";

		} else if (systemName.contains("windows")) {
			/**
			 * In windows the Eclipse root path is ../eclipse/ ,the dropins path is
			 * ../eclipse/dropins ,the eclipse launcher file is ../eclipse/eclipse.exe, So
			 * we just locate "../eclipse" from eclipse.launcher
			 */
			dirRoot = new File(eclipseExecutablePath).getParentFile();
			ECLIPSE_ROOT_DIR = dirRoot.getAbsolutePath();
			DROPINS_DIR = ECLIPSE_ROOT_DIR + File.separator + "dropins";
		} else {
			/**
			 * Under installation with tar file, root path is ../eclipse,
			 * launcher file is ./eclipse, dropins at ./dropins
			 */
			dirRoot = new File(eclipseExecutablePath);
			ECLIPSE_ROOT_DIR = dirRoot.getAbsolutePath();
			DROPINS_DIR = ECLIPSE_ROOT_DIR + File.separator + "dropins";
		}
		File dropinsDir = new File(DROPINS_DIR);
		if (!dropinsDir.exists() && !dropinsDir.isDirectory()) {
			dropinsDir = searchDropinsDir(eclipseExecutablePath);
			DROPINS_DIR = dropinsDir.getAbsolutePath();
			ECLIPSE_ROOT_DIR = dropinsDir.getParentFile().getAbsolutePath();
		}
		ConsoleUtils.printMessage("ECLIPSE_ROOT_DIR: "+ECLIPSE_ROOT_DIR);
	}

	/**
	 * 
	 * Last_update Aug 24, 2020 1:16:55 AM Last_Modifier knightsong
	 */
	private static File searchDropinsDir(String eclipseExecutablePath) {
		File dropinsDir = null;
		/* try the best to look up dropins folder */
		File dir = new File(eclipseExecutablePath).getParentFile();
		while (!dir.exists()) {
			dir = dir.getParentFile();
		}
		while (dir != null) {
			File[] dropinsFolders = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return "dropins".equals(name);
				}
			});
			if (CollectionUtils.isEmpty(dropinsFolders)) {
				dir = dir.getParentFile();
			} else {
				dropinsDir = dropinsFolders[0];
				break;
			}
		}
		if (!dropinsDir.exists() || !dropinsDir.isDirectory()) {
			throw new SavRtException("Cannot find dropins folder!");
		}
		return dropinsDir;
	}

	public static String getEclipseRootDir() {
		return ECLIPSE_ROOT_DIR;
	}

	public static String getDropinsDir() {
		return DROPINS_DIR;
	}

	public static String getResourceAbsolutePath(String pluginId, String resourceRelativePath) throws SavRtException {
		try {
			String resourceUrl = getResourceUrl(pluginId, resourceRelativePath);
			URL fileURL = new URL(resourceUrl);
			URL resolve = FileLocator.resolve(fileURL);
			URI uri = resolve.toURI();
			File file = new File(uri);
			return file.getAbsolutePath();
		} catch (Exception e1) {
			log.error(e1.getMessage());
			throw new SavRtException(e1);
		}
	}

	private static String getResourceUrl(String pluginId, String resourceRelativePath) {
		StringBuilder sb = new StringBuilder("platform:/plugin/").append(pluginId).append("/")
				.append(resourceRelativePath);
		return sb.toString();
	}

	public static IPath relativeToAbsolute(IPath relativePath) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(relativePath);
		if (resource != null) {
			return resource.getLocation();
		}
		return relativePath;
	}

	public static String getAbsolutePathOsStr(IPath relativePath) {
		IPath absolutePath = relativeToAbsolute(relativePath);
		return absolutePath.toOSString();
	}

	public static String getSourceFolderPath(String projectName, String cName) {
		ICompilationUnit unit = JavaUtil.findICompilationUnitInProject(cName, projectName);
		String javaFilePath = IResourceUtils.getAbsolutePathOsStr(unit.getPath());
		return javaFilePath.substring(0, javaFilePath.indexOf(cName.replace(".", Constants.FILE_SEPARATOR)) - 1);
	}

	public static String getRelativeSourceFolderPath(String projectFolder, String projectName, String cName) {
		String sourceFolderPath = getSourceFolderPath(projectName, cName);
		if (!sourceFolderPath.startsWith(projectFolder)) {
			throw new InvalidParameterException(
					String.format("ProjectFolder: %s, FullSourceFolderPath: %s ", projectFolder, sourceFolderPath));
		}
		return sourceFolderPath.substring(projectFolder.length(), sourceFolderPath.length());
	}

	public static String getProjectPath(String projectName) {
		return getAbsolutePathOsStr(JavaUtil.getSpecificJavaProjectInWorkspace(projectName).getFullPath());
	}

	public static String getFolderPath(String projectName, String folderName) {
		IProject project = JavaUtil.getSpecificJavaProjectInWorkspace(projectName);
		if (project == null || !project.exists() || !project.isOpen()) {
			throw new SavRtException(String.format("Project %s is not open!", projectName));
		}
		IFolder outputFolder = project.getFolder(folderName);
		if (!outputFolder.exists()) {
			try {
				outputFolder.create(IResource.NONE, true, null);
			} catch (CoreException e) {
				new SavRtException(e);
			}
		}
		return outputFolder.getLocation().toOSString();
	}

}
