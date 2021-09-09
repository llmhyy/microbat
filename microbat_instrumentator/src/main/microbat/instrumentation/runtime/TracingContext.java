package microbat.instrumentation.runtime;

import sav.strategies.dto.AppJavaClassPath;

class TracingContext {
	private TracingState state;
	public LockedThreads lockedThreads;
	public int variableLayer = 2;
	public int stepLimit = Integer.MAX_VALUE;
	public int expectedSteps = Integer.MAX_VALUE;
	public AppJavaClassPath appJavaClassPath;
	public boolean avoidProxyToString = false;
	
	public TracingContext() {
		this.state = TracingState.INIT;
		this.lockedThreads = new LockedThreads();
	}

	public void shutdown() {
		state = TracingState.SHUTDOWN;
	}

	public void start() {
		state = TracingState.TEST_STARTED;
	}
	
	public void recording() {
		state = TracingState.RECORDING;
	}

	public boolean stateEquals(TracingState ts) {
		return this.state == ts;
	}

	public void setAppJavaClassPath(AppJavaClassPath appPath) {
		this.appJavaClassPath = appPath;
	}

	public void setVariableLayer(int variableLayer) {
		this.variableLayer = variableLayer;
	}

	public void setAvoidProxyToString(boolean avoidProxy) {
		this.avoidProxyToString = avoidProxy;
	}
}