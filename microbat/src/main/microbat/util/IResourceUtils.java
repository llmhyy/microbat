/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.util;

import java.io.File;
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
import org.eclipse.jdt.core.IJavaProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sav.common.core.Constants;
import sav.common.core.SavRtException;

/**
 * @author LLT
 *
 */
public class IResourceUtils {
	private static Logger log = LoggerFactory.getLogger(IResourceUtils.class);
	private IResourceUtils(){}
	
	public static void getTestTarget(IJavaProject project) {
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
		return javaFilePath.substring(0, javaFilePath.indexOf(cName.replace(".", Constants.FILE_SEPARATOR)));
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
