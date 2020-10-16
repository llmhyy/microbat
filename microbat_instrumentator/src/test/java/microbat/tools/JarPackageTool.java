package microbat.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import microbat.instrumentation.Premain;
import sav.common.core.SavException;
import sav.common.core.utils.CollectionBuilder;
import sav.common.core.utils.FileUtils;
import sav.commons.TestConfiguration;
import sav.strategies.vm.VMRunner;

public class JarPackageTool {
	public static final String BASE_DIR = getBaseDir();
	public static final String LIB_DIR = BASE_DIR + "lib/";

	public static String DEPLOY_DIR;
	public static String DEPLOY_JAR_PATH;
	static {
		DEPLOY_DIR = System.getenv("deploy_dir");
		if (DEPLOY_DIR == null) {
			String prjRelativeDir = System.getenv("relative_deploy_dir");
			if (prjRelativeDir != null) {
				DEPLOY_DIR = BASE_DIR.replace("microbat_instrumentator", prjRelativeDir);
			} else {
				DEPLOY_DIR = BASE_DIR.replace("microbat_instrumentator", "microbat/lib");
			}
		}
		DEPLOY_JAR_PATH = DEPLOY_DIR + "instrumentator.jar";
	}
	
	public static void main(String[] args) throws Exception {
		CollectionBuilder<String, List<String>> cmd = new CollectionBuilder<String, List<String>>(new ArrayList<String>());
		VMRunner vmRunner = new VMRunner();
		FileUtils.createFolder(DEPLOY_DIR);
		/* !! DONOT REMOVE THIS COMMENT BLOCK !!!!
		 * UNCOMMENT WHEN YOU UPDATE microbat_junit_test project
		 * */
		/* export & copy to microbat/lib */
		/* export Microbat junit runner */
//		cmd.append(TestConfiguration.getJavaHome() + "/bin/jar")
//			.append("cf")
//			.append(DEPLOY_DIR + "testrunner.jar")
//			.append("-C")
//			.append(getBaseDir("microbat_junit_test") + "bin")
//			.append("microbat");
//		vmRunner.startAndWaitUntilStop(cmd.toCollection());	
//		cmd.clear();
//		System.out.println("Deploy testrunner.jar to " + DEPLOY_JAR_PATH);
		
		/* export instrumentator_agent.jar */
		String agentJar = Premain.INSTRUMENTATION_STANTDALONE_JAR;
		String instrumentatorAgentPath = LIB_DIR + agentJar;
		cmd.append(TestConfiguration.getJavaHome() + "/bin/jar")
			.append("cfm")
			.append(instrumentatorAgentPath)
			.append(BASE_DIR + "/META-INF/MANIFEST.MF")
			.append("-C")
			.append(BASE_DIR + "bin")
			.append("microbat");
		vmRunner.startAndWaitUntilStop(cmd.toCollection());	
		cmd.clear();
		
		/* export instrumentor.jar */
		cmd.append(TestConfiguration.getJavaHome() + "/bin/jar")
			.append("cfm")
			.append(DEPLOY_JAR_PATH)
			.append(BASE_DIR + "/META-INF/MANIFEST.MF")
			.append("-C")
			.append(BASE_DIR + "bin")
			.append("microbat")
			.append("-C").append(BASE_DIR)
			.append("lib/" + agentJar)
			.append("-C").append(BASE_DIR)
			.append("lib/bcel-6.0.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/javassist.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/commons-lang-2.6.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/sav.commons.simplified.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/commons-io-1.3.2.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/mysql-connector-java-5.1.44-bin.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/sqlite-jdbc-3.32.3.2.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/slf4j-api-1.7.12.jar");
		vmRunner.startAndWaitUntilStop(cmd.toCollection());	
		
		System.out.println("Deploy instrumentator.jar to " + DEPLOY_JAR_PATH);
		System.out.println("Done!");
		System.exit(0);
	}
	
	public static String getBaseDir() {
		String path = new File(JarPackageTool.class.getProtectionDomain().getCodeSource().getLocation().getPath())
				.getAbsolutePath();
		path = path.replace("\\", "/");
		path = path.replace("bin", "");
		return path;
	}
	
	@Test
	public void exportToMicrobatLib() throws SavException {
		CollectionBuilder<String, List<String>> cmd = new CollectionBuilder<String, List<String>>(new ArrayList<String>());
		VMRunner vmRunner = new VMRunner();
		
//		/* export & copy to microbat/lib */
		String microbatLibJar = BASE_DIR.replace("microbat_instrumentator/", "microbat/lib/instrumentator.jar");
		cmd.append(TestConfiguration.getJavaHome() + "/bin/jar")
			.append("cfm")
			.append(microbatLibJar)
			.append(BASE_DIR + "META-INF/MANIFEST.MF")
			.append("-C")
			.append(BASE_DIR + "bin")
			.append("microbat/instrumentation");
		vmRunner.startAndWaitUntilStop(cmd.toCollection());	
		cmd.clear();
		System.out.println("Done");
	}
	
	private static String getBaseDir(String projectName) {
		return BASE_DIR.replace("microbat_instrumentator", projectName);
		
	}
}
