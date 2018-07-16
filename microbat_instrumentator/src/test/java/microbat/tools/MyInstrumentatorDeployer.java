package microbat.tools;

import org.junit.Test;

public class MyInstrumentatorDeployer {
//	public static final String DEPLOY_DIR = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/";
//	public static String DEPLOY_DIR = "/Users/lylytran/Projects/TOOLS/Eclipse/sdk-4.6.2/Eclipse.app/Contents/Eclipse/dropins/junit_lib/";
//	public static final String DEPLOY_DIR = "/Users/lylytran/Projects/TOOLS/Eclipse/eclipse-mars-test-cli/Eclipse.app/Contents/Eclipse/dropins/junit_lib/";
	
	//	public static final String DEPLOY_DIR = BASE_DIR;
	
	@Test
	public void deployToActiveLearnTest() throws Exception {
		String learntestFolder = "/Users/lylytran/Projects/Ziyuan-branches/learntest-eclipse/app/active-learntest";
		JarPackageTool.DEPLOY_DIR = learntestFolder + "/src/main/resources/";
		JarPackageTool.DEPLOY_JAR_PATH = JarPackageTool.DEPLOY_DIR + "microbat_instrumentator.jar";
		JarPackageTool.main(new String[]{});
		
		JarPackageTool.DEPLOY_DIR = learntestFolder + "/libs/";
		JarPackageTool.DEPLOY_JAR_PATH = JarPackageTool.DEPLOY_DIR + "microbat_instrumentator.jar";
		JarPackageTool.main(new String[]{});
	}
}
