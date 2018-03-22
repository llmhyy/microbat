package microbat.instrumentation.runtime;

import microbat.instrumentation.instr.ClassLoaderInstrumenter;

public class ClassLoaderHandler extends EmptyExecutionTracer {
	private static final IExecutionTracer instance = new ClassLoaderHandler();
	private boolean tracerLockPreserve;

	@Override
	public void _hitLine(int line, String className, String methodSignature) {
		if (line == ClassLoaderInstrumenter.ENTER_MARKER) {
			tracerLockPreserve = ExecutionTracer.glock();
		} else if (line == ClassLoaderInstrumenter.EXIT_MARKER) {
			ExecutionTracer.gUnlock(tracerLockPreserve);
		}
	}
	
	public static IExecutionTracer getInstance() {
		return instance;
	}
}
