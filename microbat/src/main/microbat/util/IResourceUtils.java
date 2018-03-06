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
import sav.common.core.utils.FileUtils;

/**
 * @author LLT
 *
 */
public class IResourceUtils {
	private static Logger log = LoggerFactory.getLogger(IResourceUtils.class);
	private IResourceUtils(){}
	private static String DROPINS_DIR;
	private static String ECLIPSE_ROOT_DIR;
	
	static {
		initEclipseDirs();
	}
	
	private static void initEclipseDirs() {
		String eclipseExecutablePath = System.getProperty("eclipse.launcher");
		String userDir = eclipseExecutablePath.substring(0, eclipseExecutablePath.lastIndexOf("eclipse") - 1);
		File dropinsDir = new File(FileUtils.getFilePath(userDir, "dropins"));
		
		/* try the best to look up dropins folder */
		if (!dropinsDir.exists() || !dropinsDir.isDirectory()) {
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
		}
		if (!dropinsDir.exists() || !dropinsDir.isDirectory()) {
			throw new SavRtException("Cannot find dropins folder!");
		}
		
		DROPINS_DIR = dropinsDir.getAbsolutePath();
		ECLIPSE_ROOT_DIR = dropinsDir.getParentFile().getAbsolutePath();
	}
	
	public static String getEclipseRootDir() {
		return ECLIPSE_ROOT_DIR;
	}
	
	public static String getDropinsDir() {
		return DROPINS_DIR;
	}
	
	public static String getResourceAbsolutePath(String pluginId, String resourceRelativePath)
			throws SavRtException {
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
