package microbat.concurrent.generators;

import microbat.model.trace.Trace;
import sav.strategies.dto.AppJavaClassPath;

public abstract class ConcurrentTraceGenerator {

	abstract Trace generateSequentialTrace(AppJavaClassPath classPath);
}
