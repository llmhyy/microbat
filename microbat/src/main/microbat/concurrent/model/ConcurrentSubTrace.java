package microbat.concurrent.model;

import microbat.model.trace.Trace;
import sav.strategies.dto.AppJavaClassPath;

public class ConcurrentSubTrace extends Trace {

	private final ConcurrentTrace linkedConcurrentTrace;
	
	public ConcurrentSubTrace(ConcurrentTrace linkedTrace, AppJavaClassPath appJavaClassPath) {
		super(appJavaClassPath);
		this.linkedConcurrentTrace = linkedTrace;
		// TODO Auto-generated constructor stub
	}
	
	

}
