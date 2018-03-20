package microbat.instrumentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import microbat.instrumentation.instr.TestRunnerTranformer;

public class Premain {
	public static final String INSTRUMENTATION_STANTDALONE_JAR = "instrumentator_agent.jar";
	private static final String SAV_JAR = "sav.commons.simplified.jar";

	public static void premain(String agentArgs, Instrumentation inst) throws Exception {
		installBootstrap(inst);
		
		Class<?>[] retransformableClasses = getRetransformableClasses(inst);
		AgentLogger.debug("start instrumentation...");
		AgentParams agentParams = AgentParams.parse(agentArgs);
		Agent agent = new Agent(agentParams);
		agent.startup();
		inst.addTransformer(agent.getTransformer(), true);
		inst.addTransformer(new TestRunnerTranformer());
		agent.setTransformableClasses(retransformableClasses);
		if (!agentParams.isPrecheck()) {
			if (retransformableClasses.length > 0) {
				inst.retransformClasses(retransformableClasses);
			}
		}
		
		AgentLogger.debug("after retransform");
	}
	
	private static List<JarFile> getJarFilesDevMode() throws IOException {
		List<String> jarPaths = Arrays.asList(
				"E:/lyly/Projects/microbat/master/microbat_instrumentator/instrumentator.jar",
				"E:/lyly/Projects/microbat/master/microbat_instrumentator/lib/bcel-6.0.jar",
				"E:/lyly/Projects/microbat/master/microbat_instrumentator/lib/javassist.jar",
				"E:/lyly/Projects/microbat/master/microbat_instrumentator/lib/commons-lang-2.6.jar",
				"E:/lyly/Projects/microbat/master/microbat_instrumentator/lib/sav.commons.jar",
				"E:/lyly/Projects/microbat/master/microbat_instrumentator/lib/commons-io-1.3.2.jar",
				"E:/lyly/Projects/microbat/master/microbat_instrumentator/lib/mysql-connector-java-5.1.44-bin.jar",
				"E:/lyly/Projects/microbat/master/microbat_instrumentator/lib/slf4j-api-1.7.12.jar"
				);
		List<JarFile> jars = new ArrayList<>(jarPaths.size());
		for (String jarPath : jarPaths) {
			JarFile jarFile = new JarFile(jarPath);
			jars.add(jarFile);
		}
		return jars;
	}

	private static void installBootstrap(Instrumentation inst) throws Exception {
		AgentLogger.debug("install jar to boostrap...");
		File tempFolder = AgentUtils.createTempFolder("microbat");
		AgentLogger.debug("Temp folder to extract jars: " + tempFolder.getAbsolutePath());
		List<JarFile> bootJarPaths = getJarFiles("instrumentator_all.jar");
		if (bootJarPaths.isEmpty()) {
			bootJarPaths = getJarFiles(INSTRUMENTATION_STANTDALONE_JAR, 
										"bcel-6.0.jar",
										"javassist.jar",
										SAV_JAR,
										"commons-io-1.3.2.jar",
										"mysql-connector-java-5.1.44-bin.jar",
										"slf4j-api-1.7.12.jar");
		}
		if (bootJarPaths.isEmpty()) {
			AgentLogger.debug("Switch to dev mode");
			bootJarPaths = getJarFilesDevMode();
		}
		for (JarFile jarfile : bootJarPaths) {
			AgentLogger.debug("append to boostrap classloader: " + jarfile.getName());
			inst.appendToBootstrapClassLoaderSearch(jarfile);
			if (jarfile.getName().contains("mysql-connector-java")) {
				inst.appendToSystemClassLoaderSearch(jarfile);
			}
		}
	}

	private static List<JarFile> getJarFiles(String... jarNames) throws Exception {
		File tempFolder = AgentUtils.createTempFolder("microbat");
		List<JarFile> jars = new ArrayList<>();
		for (String jarName : jarNames) {
			File file = new File(tempFolder.getAbsolutePath(), jarName);
			if (AgentUtils.existIn(file.getName(), INSTRUMENTATION_STANTDALONE_JAR, SAV_JAR) || !file.exists()) {
				try {
					String jarResourcePath = "lib/" + jarName;
					boolean success = extractJar(jarResourcePath, file.getAbsolutePath());
					if (!success) {
						AgentLogger.debug("Could not extract jar: " + jarResourcePath);
						if (jarName.endsWith(INSTRUMENTATION_STANTDALONE_JAR)) {
							jars.clear();
							return jars;
						}
						continue;
					}
					AgentLogger.debug("Extracted jar: " + jarResourcePath);
				} catch (Exception ex) {
					file.delete();
					continue;
				}
			}
			JarFile jarFile = new JarFile(file);
			jars.add(jarFile);
		}
		return jars;
	}
	
	public static boolean extractJar(String jarResourcePath, String filePath) throws IOException {
		final InputStream inputJarStream = Premain.class.getClassLoader().getResourceAsStream(jarResourcePath);
		if (inputJarStream == null) {
			return false;
		}
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(filePath);
			outStream.getChannel().force(true);
			AgentUtils.copy(inputJarStream, outStream);
		} finally {
			if (outStream != null) {
				outStream.close();
			}
		}
		return true;
	}

	private static Class<?>[] getRetransformableClasses(Instrumentation inst) {
		AgentLogger.debug("Collect retransformable classes....");
		List<Class<?>> candidates = new ArrayList<Class<?>>();
		Class<?>[] classes = inst.getAllLoadedClasses();
		for (Class<?> c : classes) {
			if (inst.isModifiableClass(c) && inst.isRetransformClassesSupported()) {
				candidates.add(c);
			}
		}
		AgentLogger.debug(candidates.size() + " transformable candidates");
		return candidates.toArray(new Class<?>[candidates.size()]);
	}
}
