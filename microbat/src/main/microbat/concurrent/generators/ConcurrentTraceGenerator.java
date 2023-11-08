package microbat.concurrent.generators;

import java.util.List;
import java.util.concurrent.Executor;

import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.model.trace.Trace;
import microbat.preference.AnalysisScopePreference;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;

public abstract class ConcurrentTraceGenerator {


	abstract Trace generateSequentialTrace(ExecutionInfo info);
}
