package microbat.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import sav.common.core.SavException;
import sav.common.core.utils.CollectionBuilder;
import sav.commons.TestConfiguration;
import sav.strategies.vm.VMRunner;

public class RelocateJarPackageTool extends JarPackageTool {
	
	public static void main(String[] args) throws Exception {
		CollectionBuilder<String, List<String>> cmd = new CollectionBuilder<String, List<String>>(new ArrayList<String>());
		VMRunner vmRunner = new VMRunner();
		
		/* export instrumentator_rt.jar */
		String agentJar = "instrumentator_agent.jar";
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
			.append("lib/sav.commons.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/commons-io-1.3.2.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/mysql-connector-java-5.1.44-bin.jar")
			.append("-C").append(BASE_DIR)
			.append("lib/slf4j-api-1.7.12.jar");
		vmRunner.startAndWaitUntilStop(cmd.toCollection());	
		
//		runMvn(cmd, vmRunner, instrumentatorAgentPath);
		System.out.println("Done!");
	}
	
	protected void runMvn(CollectionBuilder<String, List<String>> cmd, VMRunner vmRunner,
			String instrumentatorAgentPath) throws Exception, SavException {
		/* modify pom */
		reWritePom();
		/* copy files */
		sav.common.core.utils.FileUtils.copyFileToFolder(LIB_DIR + "sav.commons.jar", appLibs, true);
		sav.common.core.utils.FileUtils.copyFileToFolder(instrumentatorAgentPath, appLibs, true);
		cmd.clear();
		cmd.append(getMvn())
			.append("-f")
			.append(MAVEN_FOLDER + "/pom.xml")
			.append("package shade:shade");
		vmRunner.startAndWaitUntilStop(cmd.toCollection());
		sav.common.core.utils.FileUtils.copyFile(MAVEN_FOLDER + "/pom-bk.xml", MAVEN_FOLDER + "/pom.xml", true);
		new File(MAVEN_FOLDER + "/pom-bk.xml").delete();
	}
	
	private void reWritePom() throws Exception {
		sav.common.core.utils.FileUtils.copyFile(MAVEN_FOLDER + "/pom.xml", MAVEN_FOLDER + "/pom-bk.xml", true);
		File file = new File(MAVEN_FOLDER + "/pom.xml");
		String content = FileUtils.readFileToString(file);
		content = content.replace("#instrumentor.output", DEPLOY_JAR_PATH);
		content = content.replace("#microbat.app.pkgs.folder", appLibs);
		sav.common.core.utils.FileUtils.writeFile(file.getAbsolutePath(), content);
	}
	
	public String getMvn() {
		return MAVEN_FOLDER + "/apache-maven-3.5.2/bin/mvn";
	}
}
