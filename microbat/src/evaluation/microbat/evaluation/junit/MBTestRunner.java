package microbat.evaluation.junit;

import java.net.URLClassLoader;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class MBTestRunner {
	
	private boolean successful = false;
	private String failureMessage = "no fail";
	
	public MBTestRunner(){
		
	}
	
	public static void main(String[] args){
//		String[] classAndMethod = args[0].split("#");
		String className = args[0];
		String methodName = args[1];
		
		MBTestRunner testRunner = new MBTestRunner();
		testRunner.runTest(className, methodName);
	}
	
	
	public void runTest(String className, String methodName){
		Request request;
		try {
			request = Request.method(Class.forName(className), methodName);
			Result result = new JUnitCore().run(request);
			setSuccessful(result.wasSuccessful());
			
			List<Failure> failures = result.getFailures();
			for(Failure failure: failures){
				Throwable exception = failure.getException();
				this.failureMessage = exception.getMessage();
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		System.currentTimeMillis();
		System.out.println("is successful? " + successful);
	}
	
	public static boolean isTestSuccessful(String className, String methodName, URLClassLoader classLoader){
		Request request;
		try {
			if(classLoader == null){
				request = Request.method(Class.forName(className), methodName);				
			}
			else{
				request = Request.method(Class.forName(className, false, classLoader), methodName);
			}
			Result result = new JUnitCore().run(request);
			boolean successful = result.wasSuccessful();
			
			return successful;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}
}
