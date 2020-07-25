package microbat.instrumentation;

import microbat.model.trace.Trace;
import sav.strategies.dto.AppJavaClassPath;

/**
 * 
 * @author LLT
 *
 */
public class ExecutionContext {
	private AppJavaClassPath appJavaClassPath;
	
	public ExecutionContext(AppJavaClassPath appJavaClassPath) {
		this.appJavaClassPath = appJavaClassPath;
	}
	 
	public AppJavaClassPath getAppJavaClassPath() {
		return appJavaClassPath;
	}

	public Trace newTrace() {
		return new Trace(appJavaClassPath);
	}
}
