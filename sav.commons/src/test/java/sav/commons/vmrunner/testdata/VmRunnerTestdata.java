/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.vmrunner.testdata;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author LLT
 *
 */
public class VmRunnerTestdata {
	public static final String FILE_NAME = "vmRunnerTestdata.txt";

	public static void main(String[] args) throws Exception {
		Thread.sleep(1000);
		String text = "Running "  + VmRunnerTestdata.class.getSimpleName();
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(tmpdir, FILE_NAME);
		FileOutputStream stream = new FileOutputStream(file);
		stream.write(text.getBytes());
		System.out.println(text);
	}
}
