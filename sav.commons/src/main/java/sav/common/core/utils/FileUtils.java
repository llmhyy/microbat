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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import sav.common.core.Constants;
import sav.common.core.Pair;
import sav.common.core.SavRtException;

/**
 * @author LLT
 *
 */
public class FileUtils {
	private static final String FILE_IDX_START_CH = "_";
	private static final int FILE_SEQ_START_IDX = 0;

	private FileUtils(){}
	
	public static File createTempFolder(String folderName) {
		File folder = getFileInTempFolder(folderName);
		if (folder.exists()) {
			if (folder.isDirectory()) {
				return folder;
			}
			throw new SavRtException(String.format("Cannot create temp folder: %s", folderName));
		}
		folder.mkdirs();
		return folder;
	}
	
	public static File createFolder(String folderPath) {
		File folder = new File(folderPath);
		if (folder.exists()) {
			if (folder.isDirectory()) {
				return folder;
			}
			throw new SavRtException(String.format("Path %s is not a folder!", folderPath));
		}
		folder.mkdirs();
		return folder;
	}
	
	public static File getFileInTempFolder(String fileName) {
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(tmpdir, fileName);
		return file;
	}
	
	public static void appendFile(String fileName, String content) {
		writeFile(fileName, content, true);
	}
	
	public static void writeFile(String fileName, String content) {
		writeFile(fileName, content, false);
	}
	
	public static void writeFile(String fileName, String content, boolean append) {
		File file = getFileCreateIfNotExist(fileName);
		FileOutputStream stream;
		try {
			stream = new FileOutputStream(file, append);
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
	
	public static String getFilePath(List<String> fragments) {
		return StringUtils.join(fragments, Constants.FILE_SEPARATOR);
	}
	
	public static String copyFileToFolder(String sourceFile, String destFolder, boolean preserveFileDate) {
		sourceFile = sourceFile.replace(Constants.FILE_SEPARATOR, "/");
		String fileName = sourceFile.substring(sourceFile.lastIndexOf("/") + 1, sourceFile.length());
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
	
	public static String backupFile(String fileName) {
		int idx = fileName.lastIndexOf(".");
		if (idx < 0) {
			return fileName + "_bk";
		}
		String newfile = new StringBuilder(fileName.substring(0, idx))
				.append("_bk").append(fileName.substring(idx)).toString();
		copyFile(fileName, newfile, true);
		return newfile;
	}
	
	public static void deleteFolder(File file) {
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			if (file.getAbsolutePath().length() > 260) {
				File newFile = new File(FileUtils.getFilePath(file.getParent(), "$"));
				file.renameTo(newFile);
				newFile.delete();
			} else {
				file.delete();
			}
		} else {
			for (File sub : file.listFiles()) {
				deleteFolder(sub);
			}
		}
		file.delete();
	}
	
	public static void deleteAllFiles(String folderPath) {
		deleteAllFiles(folderPath, null);
	}
	
	public static void deleteAllFiles(String folderPath, FilenameFilter filter) {
		File folder = new File(folderPath);
		if (!folder.exists() || !folder.isDirectory()) {
			return;
		}
		File[] files;
		if (filter != null) {
			files = folder.listFiles(filter);
		} else {
			files = folder.listFiles();
		}
		if (!CollectionUtils.isEmpty(files)) {
			deleteFiles(Arrays.asList(files));
		}
	}
	
	public static void deleteFiles(List<File> files) {
		for (File file : CollectionUtils.nullToEmpty(files)) {
			if (file.getAbsolutePath().length() > 260) {
				File newFile = new File(file.getAbsolutePath().substring(0, 250));
				file.renameTo(newFile);
				newFile.delete();
			} else {
				file.delete();
			}
		}
	}
	
	public static File createNewFileInSeq(String dir, final String filePrefix, final String fileSuffix) {
		int fileIdx = FILE_SEQ_START_IDX;
		Pair<File, Integer> lastFile = getLastFile(dir, filePrefix, fileSuffix);
		if (lastFile != null) {
			fileIdx = lastFile.b + 1;
		}
		String filepath = new StringBuilder(dir).append(File.separator)
							.append(filePrefix).append(FILE_IDX_START_CH).append(fileIdx).append(fileSuffix)
							.toString();
		return getFileCreateIfNotExist(filepath);
	}
	
	public static Pair<File, Integer> getLastFile(String dir, final String filePrefix, final String fileSuffix) {
		File folder = new File(dir);
		if (!folder.exists()) {
			return null;
		}
		File[] files = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File folder, String name) {
				return name.endsWith(fileSuffix) && name.startsWith(filePrefix);
			}
		});
		if (CollectionUtils.isEmpty(files)) {
			return null;
		}
		return getLastFile(files, fileSuffix);
	}
	
	public static Pair<File, Integer> getLastFile(File[] files, String suffix) {
		int lastIdx = -1;
		File lastFile = null;
		for (File file : files) {
			String fileName = file.getName();
			String fileIdxStr = fileName.substring(fileName.lastIndexOf(FILE_IDX_START_CH) + 1,
					fileName.indexOf(suffix));
			int fileIdx = lastIdx;
			try{
				fileIdx = Integer.valueOf(fileIdxStr);
			}
			catch(Exception e){
				fileIdx = lastIdx;
			}
			if (fileIdx > lastIdx) {
				lastIdx = fileIdx;
				lastFile = file;
			}
		}
		if (lastIdx < 0) {
			return null;
		}
		return Pair.of(lastFile, lastIdx);
	}
	
	public static File getFileEndWith(String folderPath, final String fileSuffix) {
		File folder = new File(folderPath);
		File[] matches = folder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(fileSuffix)) {
					return true;
				}
				return false;
			}
		});
		if (CollectionUtils.isEmpty(matches)) {
			return null;
		}
		return matches[0];
	}
	
	public static String lookupFile(String path, final String fileName) {
		File file = new File(path);
		if (file.isFile()) {
			return lookupFile(file.getParent(), fileName);
		} else {
			String[] matchedFiles = file.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.equals(fileName);
				}
			});
			if (CollectionUtils.isNotEmpty(matchedFiles)) {
				return matchedFiles[0];
			}
			for (File sub : file.listFiles()) {
				if (sub.isDirectory()) {
					String match = lookupFile(sub.getAbsolutePath(), fileName);
					if (match != null) {
						return match;
					}
				}
			}
			return null;
		}
	}
}
