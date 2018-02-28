/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sav.common.core.SavException;
import sav.common.core.utils.CollectionBuilder;
import sav.common.core.utils.StringUtils;

/**
 * @author LLT
 * 
 */
public class JavaCompiler {
	private Logger log = LoggerFactory.getLogger(JavaCompiler.class);
	private VMConfiguration vmConfig;

	public JavaCompiler(VMConfiguration vmConfig) {
		setVmConfig(vmConfig);
	}

	public boolean compile(String targetFolder, File... javaFiles)
			throws SavException {
		return compile(targetFolder, Arrays.asList(javaFiles));
	}

	public boolean compile(String targetFolder, Collection<File> javaFiles)
			throws SavException {
		CollectionBuilder<String, List<String>> builder = new CollectionBuilder<String, List<String>>(
				new ArrayList<String>())
				.append(VmRunnerUtils.buildJavaCPrefix(vmConfig))
				.append("-classpath").append(vmConfig.getClasspathStr()).append("-d")
				.append(targetFolder);
		for (File mutatedFile : javaFiles) {
			builder.append(mutatedFile.getAbsolutePath());
		}
		builder.append("-g")
			.append("-nowarn");
		VMRunner vmRunner = VMRunner.getDefault();
		vmRunner.setLog(vmConfig.isVmLogEnable());
		boolean success = vmRunner.startAndWaitUntilStop(builder.toCollection());
		if (!success ) {
			String errorMsg = vmRunner.getProccessError();
			throw new SavException("compilation error: " + errorMsg);
		} else {
			String errorMsg = vmRunner.getProccessError();
			if (!StringUtils.isEmpty(errorMsg)) {
				log.warn(errorMsg);
			}
		}
		return success;
	}

	public void setVmConfig(VMConfiguration vmConfig) {
		this.vmConfig = vmConfig;
	}

}
