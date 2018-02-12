package sav.strategies.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import sav.common.core.utils.CollectionUtils;

public class RequestExecution implements Runnable {
	private final JUnitCore core;
	private Request request;
	private boolean isPassed;
	private List<Failure> failures;
	
	public RequestExecution() {
		core = new JUnitCore();
	}
	
	public void run() {
		try {
			Result result = core.run(request);
			failures = result.getFailures();
			this.isPassed = (result.getFailureCount() <= 0);
		} catch (Throwable e) {
			isPassed = false;
			failures.add(new Failure(null, e));
		}
	}
	
	public boolean getResult(){
		return isPassed;
	}
	
	public List<Failure> getFailures() {
		return CollectionUtils.initIfEmpty(failures);
	}
	
	public void setRequest(Request request){
		this.request = request;
	}

	@SuppressWarnings("unchecked")
	public static List<Failure> getFailureTrace(Class<?> targetClass,
			Runnable targetInstance) {
		try {
			Method method = targetClass.getMethod("getFailures");
			return (List<Failure>) method.invoke(targetInstance);
		} catch (Exception ex) {
			return new ArrayList<Failure>();
		}
	}

}
