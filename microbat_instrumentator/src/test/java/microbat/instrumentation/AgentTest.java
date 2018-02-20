package microbat.instrumentation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import microbat.instrumentation.trace.testdata.Sample;
import sav.common.core.SavException;
import sav.common.core.utils.CollectionBuilder;
import sav.commons.TestConfiguration;
import sav.strategies.vm.AgentVmRunner;
import sav.strategies.vm.VMConfiguration;
import sav.strategies.vm.VmRunnerUtils;

public class AgentTest {
	protected static final String BASE_DIR = "E:/lyly/Projects/microbat/master/microbat_instrumentator";
//	private static final String JAR_PATH = BASE_DIR + "/src/test/resources/microbat_rt.jar";
	protected static final String JAR_PATH = BASE_DIR + "/src/resources/instrumentator.jar";
	
	@Test
	public void runSample() throws Exception {
		startVm();
	}

	private void startVm() throws Exception {
		AgentVmRunner vmRunner = new AgentVmRunner(JAR_PATH, AgentConstants.AGENT_OPTION_SEPARATOR, AgentConstants.AGENT_PARAMS_SEPARATOR);
		VMConfiguration config = new VMConfiguration();
		config.setJavaHome(TestConfiguration.getJavaHome());
		config.addClasspath(BASE_DIR + "/bin");
//		config.addClasspaths(getLibJars(BASE_DIR + "/lib"));
		Class<?> clazz = Sample.class;
		config.setLaunchClass(clazz.getName());
		config.setDebug(true);
		config.setPort(9595);
		config.setNoVerify(true);
		vmRunner.addAgentParam("entry_point", clazz.getName() + ".main([Ljava/lang/String;)V");
//		vmRunner.startVm(config);
		System.out.println(vmRunner.getCommandLinesString(config));
	}
	
	protected List<String> getLibJars(String... libFolders) throws Exception {
		List<String> jars = new ArrayList<String>();
		for (String libFolder : libFolders) {
			Collection<?> files = FileUtils.listFiles(new File(libFolder),
					new String[] { "jar" }, true);
			for (Object obj : files) {
				File file = (File) obj;
				jars.add(file.getAbsolutePath());
			}
		}
		return jars;
	}
	
}
