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
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new SavRtException("cannot create file " + fileName);
			}
		}
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
}
