package microbattest.tools;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import microbat.instrumentation.Premain;
import sav.common.core.SavException;
import sav.common.core.utils.CollectionBuilder;
import sav.commons.TestConfiguration;
import sav.strategies.vm.VMRunner;

public class JarPackageTool {
	public static final String BASE_DIR = getBaseDir();
	public static final String MAVEN_FOLDER = BASE_DIR + "build/maven";
	public static final String LIB_DIR = BASE_DIR + "lib/";
	public static final String DEPLOY_DIR = "E:/linyun/software/eclipse-java-mars/eclipse-java-mars-clean/eclipse/dropins/junit_lib/";
//	public static final String DEPLOY_DIR = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/";
	//	public static final String DEPLOY_DIR = BASE_DIR;
	public static final String DEPLOY_JAR_PATH = DEPLOY_DIR + "instrumentator.jar";
	public static final String appLibs = MAVEN_FOLDER + "/libs";
	
	public static void main(String[] args) throws Exception {
		CollectionBuilder<String, List<String>> cmd = new CollectionBuilder<String, List<String>>(new ArrayList<String>());
		VMRunner vmRunner = new VMRunner();
		
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
			.append(BASE_DIR + "META-INF/MANIFEST.MF")
			.append("-C")
			.append(BASE_DIR + "bin")
			.append("microbat");
		vmRunner.startAndWaitUntilStop(cmd.toCollection());	
		cmd.clear();
		
		/* export instrumentor.jar */
		cmd.append(TestConfiguration.getJavaHome() + "/bin/jar")
			.append("cfm")
			.append(DEPLOY_JAR_PATH)
			.append(BASE_DIR + "META-INF/MANIFEST.MF")
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
			.append("lib/slf4j-api-1.7.12.jar");
		vmRunner.startAndWaitUntilStop(cmd.toCollection());	
		
		System.out.println("Deploy instrumentator.jar to " + DEPLOY_JAR_PATH);
		System.out.println("Done!");
	}
	
	public static String getBaseDir() {
		String path = JarPackageTool.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.replace("\\", "/");
		path = path.replace("bin/", "");
		if (path.startsWith("/")) {
			path = path.substring(1, path.length());
		}
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
	}
	
	private static String getBaseDir(String projectName) {
		return BASE_DIR.replace("microbat_instrumentator", projectName);
		
	}
}
