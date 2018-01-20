/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import sav.common.core.Constants;
import sav.common.core.SavRtException;

/**
 * @author LLT
 *
 */
public class FileUtils {
	private FileUtils(){}
	
	public static File getFileInTempFolder(String fileName) {
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(tmpdir, fileName);
		return file;
	}
	
	public static void appendFile(String fileName, String content) {
		File file = getFileCreateIfNotExist(fileName);
		FileOutputStream stream;
		try {
			stream = new FileOutputStream(file, true);
			stream.write(content.getBytes());
			stream.close();
		} catch (FileNotFoundException e) {
			throw new SavRtException(e);
		} catch (IOException e) {
			throw new SavRtException(e);
		}
	}
	
	public static File getFileCreateIfNotExist(String path) {
		File file = new File(path);
		if (!file.exists()) {
			File folder = file.getParentFile();
			if (!folder.exists()) {
				folder.mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new SavRtException(e);
			}
		}
		return file;
	}
	
	public static String getFilePath(String... fragments) {
		return StringUtils.join(Arrays.asList(fragments), Constants.FILE_SEPARATOR);
	}
	
	public static String copyFileToFolder(String sourceFile, String destFolder, boolean preserveFileDate) {
		String fileName = sourceFile.substring(sourceFile.lastIndexOf(Constants.FILE_SEPARATOR) + 1, sourceFile.length());
		String destFile = getFilePath(destFolder, fileName);
		try {
			org.apache.commons.io.FileUtils.copyFile(new File(sourceFile), new File(destFile), preserveFileDate);
		} catch (IOException e) {
			throw new SavRtException(e);
		}
		return destFile;
	}
	
	public static void copyFile(String srcFile, String destFile, boolean preserveFileDate) {
		try {
			org.apache.commons.io.FileUtils.copyFile(new File(srcFile), new File(destFile), preserveFileDate);
		} catch (IOException e) {
			throw new SavRtException(e);
		}
	}
	
	public static void deleteAllFiles(String folderPath) {
		File folder = new File(folderPath);
		if (!folder.exists() || !folder.isDirectory()) {
			return;
		}
		File[] files = folder.listFiles();
		if (!CollectionUtils.isEmpty(files)) {
			deleteFiles(Arrays.asList(files));
		}
	}
	
	public static void deleteFiles(List<File> files) {
		for (File file : CollectionUtils.nullToEmpty(files)) {
			file.delete();
		}
	}
}
