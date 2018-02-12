/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.vmrunner;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sav.common.core.SavException;
import sav.commons.AbstractTest;
import sav.commons.TestConfiguration;
import sav.commons.vmrunner.testdata.VmRunnerTestdata;
import sav.commons.vmrunner.testdata.VmRunnerTestdataLoop;
import sav.strategies.vm.VMConfiguration;
import sav.strategies.vm.VMRunner;

/**
 * @author LLT
 *
 */
public class VmRunnerTest extends AbstractTest {
	private VMConfiguration vmConfig;
	private VMRunner vmRunner;
	
	@Before
	public void setup() {
		vmConfig = new VMConfiguration();
		vmConfig.setJavaHome(TestConfiguration.JAVA_HOME);
		vmConfig.addClasspath(TestConfiguration.SAV_COMMONS_TEST_TARGET);
		vmConfig.setDebug(false);
		vmRunner = new VMRunner();
	}
	
	public void teststartAndWaitUntilStop_redirect() throws Exception {
		vmConfig.setLaunchClass(VmRunnerTestdata.class.getName());
		File file = File.createTempFile("vmRunnerTest", "txt");
		file.deleteOnExit();
//		Redirect redirect = Redirect.to(file);
//		vmRunner.setRedirect(redirect);
		vmRunner.startAndWaitUntilStop(vmConfig);
		String fileContent = FileUtils.readFileToString(file);
		Assert.assertTrue(!StringUtils.isEmpty(fileContent));
	}
	
	@Test
	public void teststartAndWaitUntilStop() throws Exception {
		vmConfig.setLaunchClass(VmRunnerTestdata.class.getName());
		vmRunner.startAndWaitUntilStop(vmConfig);
		File file = sav.common.core.utils.FileUtils
				.getFileInTempFolder(VmRunnerTestdata.FILE_NAME);
		file.deleteOnExit();
		Assert.assertTrue(file.exists());
		String fileContent = FileUtils.readFileToString(file);
		Assert.assertTrue(!StringUtils.isEmpty(fileContent));
	}
	
	@Test
	public void testTimeout() throws SavException {
		vmConfig.setLaunchClass(VmRunnerTestdataLoop.class.getName());
		vmRunner.setTimeout(10, TimeUnit.SECONDS);
		vmRunner.startAndWaitUntilStop(vmConfig);
	}
}
