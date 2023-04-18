package microbat.evaluation.junit; // TODO: This is for backward compatibility e.g. Tregression. Ideally the package name should not have "junit" inside it, since it also handles testng

import microbat.evaluation.factory.MicroBatTestRunnerFactory;
import microbat.evaluation.runners.TestRunner;

public class MicroBatTestRunner {
	
	public static void main(String[] args){
		String className = args[0];
		String methodName = args[1];
		MicroBatTestRunnerFactory testRunnerFactory = new MicroBatTestRunnerFactory();
		TestRunner testRunner = testRunnerFactory.create(className, methodName);
		testRunner.runTest(className, methodName);
	}
}
