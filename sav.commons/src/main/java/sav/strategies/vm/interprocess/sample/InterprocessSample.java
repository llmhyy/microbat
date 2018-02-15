/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm.interprocess.sample;

import sav.common.core.SavException;
import sav.common.core.utils.SingleTimer;

/**
 * @author LLT
 *
 */
public class InterprocessSample {

	public static void main(String[] args) throws SavException {
		SampleInputWriter inputWriter = new SampleInputWriter();
		SampleOutputReader outputReader = new SampleOutputReader();
		SamplePythonVmRunner vmRunner = new SamplePythonVmRunner(inputWriter, outputReader);
		SingleTimer timer = SingleTimer.start("test GAN");
		try {
			SampleOutput output = null;
			vmRunner.start();
			inputWriter.sendData(new SampleInput(234, "lylytran"));
			output = outputReader.readOutput();
			System.out.println(output);

			inputWriter.sendData(new SampleInput(456, "user1"));
			output = outputReader.readOutput();
			System.out.println(output);
			
			inputWriter.sendData(new SampleInput(56456, "user2"));
			output = outputReader.readOutput();
			System.out.println(output);
			System.out.println(timer.getResult());
		} finally {
			vmRunner.stop();
		}
	}
}
