package microbat.agent;

import microbat.Activator;
import microbat.model.trace.Trace;
import microbat.util.IResourceUtils;
import sav.common.core.SavRtException;
import sav.common.core.utils.ClassUtils;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.vm.VMConfiguration;

public class TraceConstructor {
	
	public Trace getTrace(AppJavaClassPath appClassPath) throws Exception {
		String instrumentationJarPath = IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "lib")
				+ "/instrumentation.jar";

		TraceAgentRunner agentRunner = new TraceAgentRunner(instrumentationJarPath);
		VMConfiguration config = new VMConfiguration(appClassPath);
		config.setLaunchClass(appClassPath.getLaunchClass());
		config.setNoVerify(true);
		agentRunner.addAgentParam("entry_point",
				ClassUtils.toClassMethodStr(appClassPath.getOptionalTestClass(), appClassPath.getOptionalTestMethod()));
		agentRunner.startVm(config);
		Trace trace = agentRunner.getTrace();
		if (trace == null) {
			throw new SavRtException("Cannot build trace");
		}
		return trace;
	}
}
