package microbat.concurrent.generators;

import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.StepLimitException;
import microbat.concurrent.model.ConcurrentTrace;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;

public class TimeStampTraceGenerator extends ConcurrentTraceGenerator {

	private static final ConcurrentTrace EMPTY_TRACE = new ConcurrentTrace("");
	
	@Override
	Trace generateSequentialTrace(ExecutionInfo executionInfo) {
		// TODO Auto-generated method stub
		InstrumentationExecutor executor =  executionInfo.getDefaultExecutor();
		try {
			final RunningInfo result = executor.run();
			if (result == null) {
				return EMPTY_TRACE;
			}
			ConcurrentTrace trace = ConcurrentTrace.fromTimeStampOrder(
					result.getTraceList());
			return trace;
		} catch (StepLimitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return EMPTY_TRACE;
		}
		
		
		
		
	}

}
