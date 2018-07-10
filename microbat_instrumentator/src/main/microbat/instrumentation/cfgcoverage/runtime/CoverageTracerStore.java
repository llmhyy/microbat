package microbat.instrumentation.cfgcoverage.runtime;

import microbat.instrumentation.runtime.TracerStore;

public class CoverageTracerStore extends TracerStore<CoverageTracer> {

	@Override
	protected CoverageTracer initTracer(long threadId) {
		return new CoverageTracer(threadId);
	}

}
