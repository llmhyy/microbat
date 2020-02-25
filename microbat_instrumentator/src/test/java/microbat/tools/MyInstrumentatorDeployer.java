package microbat.tools;

import org.junit.Test;

public class MyInstrumentatorDeployer {
//	public static final String DEPLOY_DIR = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/";
//	public static String DEPLOY_DIR = "/Users/lylytran/Projects/TOOLS/Eclipse/sdk-4.6.2/Eclipse.app/Contents/Eclipse/dropins/junit_lib/";
//	public static final String DEPLOY_DIR = "/Users/lylytran/Projects/TOOLS/Eclipse/eclipse-mars-test-cli/Eclipse.app/Contents/Eclipse/dropins/junit_lib/";
	
	//	public static final String DEPLOY_DIR = BASE_DIR;
	
	@Test
	public void lylysWindow_deployToEclipseDropins() throws Exception {
		JarPackageTool.DEPLOY_DIR = "E:/lyly/eclipse-java-mars-clean/eclipse/dropins/junit_lib/";
		JarPackageTool.DEPLOY_JAR_PATH = JarPackageTool.DEPLOY_DIR + "instrumentator.jar";
		JarPackageTool.main(new String[]{});
		
		JarPackageTool tool = new JarPackageTool();
		tool.exportToMicrobatLib();
	}
	
	@Test
	public void lylysWindow_deployToActiveLearnTest() throws Exception {
		String learntestFolder = "E:/lyly/Projects/Ziyuan/learntest-nn/Ziyuan/app/active-learntest";
		JarPackageTool.DEPLOY_DIR = learntestFolder + "/src/main/resources/";
		JarPackageTool.DEPLOY_JAR_PATH = JarPackageTool.DEPLOY_DIR + "microbat_instrumentator.jar";
		JarPackageTool.main(new String[]{});
		
		JarPackageTool.DEPLOY_DIR = learntestFolder + "/libs/";
		JarPackageTool.DEPLOY_JAR_PATH = JarPackageTool.DEPLOY_DIR + "microbat_instrumentator.jar";
		JarPackageTool.main(new String[]{});
	}
	
	@Test
	public void deployTo() throws Exception {
		JarPackageTool.DEPLOY_DIR = "D:/_1_Projects/microbat/microbat/microbat/lib";
		JarPackageTool.DEPLOY_JAR_PATH = JarPackageTool.DEPLOY_DIR + "/instrumentator.jar";
		JarPackageTool.main(new String[]{});
	}
	
	@Test
	public void deploy() throws Exception {
		JarPackageTool.DEPLOY_DIR = "D:/_1_Projects/microbat/microbat/microbat_examples/resources/";
		JarPackageTool.DEPLOY_JAR_PATH = JarPackageTool.DEPLOY_DIR + "microbat_instrumentator.jar";
		JarPackageTool.main(new String[]{});
	}
}
