package microbat.concurrent.generators;

import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.model.trace.Trace;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;

public class TimeStampTraceGenerator extends ConcurrentTraceGenerator {
	protected String generateTraceDir(AppJavaClassPath appPath) {
		String traceFolder;
		if (appPath.getOptionalTestClass() != null) {
			traceFolder = FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), 
					Settings.projectName,
					appPath.getOptionalTestClass(), 
					appPath.getOptionalTestMethod());
		} else {
			traceFolder = FileUtils.getFilePath(MicroBatUtil.getTraceFolder(), 
					Settings.projectName, 
					appPath.getLaunchClass()); 
		}
		FileUtils.createFolder(traceFolder);
		return traceFolder;
	}
	@Override
	Trace generateSequentialTrace(AppJavaClassPath classPath) {
		// TODO Auto-generated method stub
//		InstrumentationExecutor(classPath, generateTraceDir(classPath))
		return null;
		
		
	}

}
