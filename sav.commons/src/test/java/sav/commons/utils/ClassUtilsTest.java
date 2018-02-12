/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.utils;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import sav.common.core.utils.ClassUtils;
import sav.commons.AbstractTest;
import sav.commons.TestConfiguration;

/**
 * @author LLT
 *
 */
public class ClassUtilsTest extends AbstractTest {

	@Test
	public void testGetCompiledClassFiles() {
		String targetPath = TestConfiguration.SAV_COMMONS_TEST_TARGET;
		List<File> classFiles = ClassUtils.getCompiledClassFiles(targetPath, ClassUtilsTestdata.class.getName());
		Assert.assertEquals(2, classFiles.size());
	}
}
