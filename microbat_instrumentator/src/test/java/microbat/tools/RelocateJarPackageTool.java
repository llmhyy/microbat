package microbat.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import microbat.instrumentation.Premain;
import sav.common.core.SavException;
import sav.common.core.utils.CollectionBuilder;
import sav.common.core.utils.StringUtils;
import sav.commons.TestConfiguration;
import sav.strategies.vm.VMRunner;

public class RelocateJarPackageTool extends JarPackageTool {
	public static final String MAVEN_FOLDER = BASE_DIR + "build/maven";
	public static final String appLibs = MAVEN_FOLDER + "/libs";
	
	public static void main(String[] args) throws Exception {
		CollectionBuilder<String, List<String>> cmd = new CollectionBuilder<String, List<String>>(new ArrayList<String>());
		VMRunner vmRunner = new VMRunner();
		
		/* export instrumentator_rt.jar */
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
		
		runMvn(cmd, vmRunner, instrumentatorAgentPath);
		System.out.println("Done!");
	}
	
	protected static void runMvn(CollectionBuilder<String, List<String>> cmd, VMRunner vmRunner,
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
		//
		String batFile = MAVEN_FOLDER + "/instrn_shade.bat";
		sav.common.core.utils.FileUtils.writeFile(batFile, StringUtils.join(cmd.toCollection(), " "));
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "instrn_shade.bat");
		File dir = new File(MAVEN_FOLDER);
		pb.directory(dir);
		Process p = pb.start();
		
		sav.common.core.utils.FileUtils.copyFile(MAVEN_FOLDER + "/pom-bk.xml", MAVEN_FOLDER + "/pom.xml", true);
		new File(MAVEN_FOLDER + "/pom-bk.xml").delete();
	}
	
	private static void reWritePom() throws Exception {
		sav.common.core.utils.FileUtils.copyFile(MAVEN_FOLDER + "/pom.xml", MAVEN_FOLDER + "/pom-bk.xml", true);
		File file = new File(MAVEN_FOLDER + "/pom.xml");
		String content = FileUtils.readFileToString(file);
		content = content.replace("#instrumentor.output", DEPLOY_JAR_PATH);
		content = content.replace("#microbat.app.pkgs.folder", appLibs);
		sav.common.core.utils.FileUtils.writeFile(file.getAbsolutePath(), content);
	}
	
	public static String getMvn() {
		return MAVEN_FOLDER + "/apache-maven-3.5.2/bin/mvn";
	}
}
