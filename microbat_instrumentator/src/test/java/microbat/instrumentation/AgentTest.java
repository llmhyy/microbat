package microbat.instrumentation;

import org.junit.Test;

import sav.strategies.vm.AgentVmRunner;
import sav.strategies.vm.VMConfiguration;

public class AgentTest {
	private static final String JAR_PATH = "E:/lyly/Projects/microbat/master/microbat_instrumentator/src/test/resources/microbat_instrumentor.jar";
	
	@Test
	public void runSample() throws Exception {
		startVm();
	}

	private void startVm() throws Exception {
		AgentVmRunner vmRunner = new AgentVmRunner(JAR_PATH);
		VMConfiguration config = new VMConfiguration();
		config.addClasspath("");
		vmRunner.startVm(config);
	}
	
	
}
