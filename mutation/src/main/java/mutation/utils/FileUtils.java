/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.utils;

import java.io.File;
import java.io.IOException;

import sav.common.core.SavRtException;
import sav.common.core.utils.Assert;

/**
 * @author LLT
 *
 */
public class FileUtils {
	
	public static File createFolder(File parent, String child) {
		Assert.assertTrue(parent.isDirectory(), parent + " is not a folder");
		return createFolder(parent.getAbsolutePath(), child);
	}
	
	public static File createFolder(String parent, String child) {
		File folder = new File(parent, child);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}

	public static File createTempFolder(String baseName) {
		try {
			File file = File.createTempFile(baseName, "");
			file.delete();
			file.mkdir();
			return file;
		} catch (IOException e) {
			throw new SavRtException("cannot create temp dir");
		}
	}
	
    public static File copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) throws IOException {
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && destDir.isDirectory() == false) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        File targetFile = new File(destDir, srcFile.getName());
		copyFile(srcFile, targetFile, preserveFileDate);
		return targetFile;
    }

	public static void copyFile(File srcFile, File targetFile,
			boolean preserveFileDate) throws IOException {
		org.apache.commons.io.FileUtils.copyFile(srcFile, targetFile, preserveFileDate);
	}
	
	public static boolean doesFileExist (String path) {
		File file = new File(path);
		return file.exists();
	}
}
