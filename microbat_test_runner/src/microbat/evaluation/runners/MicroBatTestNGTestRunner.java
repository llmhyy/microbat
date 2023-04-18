package microbat.evaluation.runners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class MicroBatTestNGTestRunner extends TestRunner {
	public void runTest(final String className, final String methodName){
		TestListenerAdapter tla = new TestListenerAdapter() {
			@Override
			public void onStart(ITestContext context) {
				super.onStart(context);
				$testStarted(className, methodName);
			}
			
			@Override
			public void onTestFailure(ITestResult tr) {
				failureMessage = tr.getThrowable().getMessage();
				super.onTestFailure(tr);
			}
			
			@Override
			public void onFinish(ITestContext context) {
				$testFinished(className, methodName);
				super.onFinish(context);
			}
		};
		List<XmlInclude> testMethods = new ArrayList<>();
		XmlInclude testMethod = new XmlInclude(methodName);
		testMethods.add(testMethod);
		XmlClass xmlClass = new XmlClass(className);
		xmlClass.setIncludedMethods(testMethods);
		List<XmlClass> classes = new ArrayList<XmlClass>();
		classes.add(xmlClass);

		XmlSuite suite = new XmlSuite();
		
		XmlTest test = new XmlTest(suite);
		
		test.setXmlClasses(classes) ;

		TestNG testNG = new TestNG();
        testNG.setXmlSuites(Collections.singletonList(suite));
        testNG.setUseDefaultListeners(false);
        testNG.addListener(tla);
		testNG.run();
		setSuccessful(!testNG.hasFailure());

		
		System.currentTimeMillis();
		System.out.println("is successful? " + successful);
		System.out.println(this.failureMessage);
		$exitProgram(successful + ";" + this.failureMessage);
	}
}

