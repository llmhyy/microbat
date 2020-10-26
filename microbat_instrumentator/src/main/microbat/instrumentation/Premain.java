package microbat.instrumentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import microbat.instrumentation.instr.TestRunnerTranformer;
import microbat.instrumentation.utils.CollectionUtils;
import microbat.instrumentation.utils.FileUtils;

/**
 * 
 * @author LLT
 * An agent jar 
 * Para
 * 
 */
public class Premain {
	public static final String INSTRUMENTATION_STANTDALONE_JAR = "instrumentator_agent_v02.jar";
	private static final String SAV_JAR = "sav.commons.simplified.jar";
	private static boolean testMode = true;

	public static void premain(String agentArgs, Instrumentation inst) throws Exception {
		long vmStartupTime = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
		long agentPreStartup = System.currentTimeMillis();
		installBootstrap(inst);
		CommandLine cmd = CommandLine.parse(agentArgs);
		Class<?>[] retransformableClasses = getRetransformableClasses(inst);
		
		debug("start instrumentation...");
		agentPreStartup = System.currentTimeMillis() - agentPreStartup;
		System.out.println("Vm start up time: " + vmStartupTime);
		System.out.println("Agent start up time: " + agentPreStartup);
		Agent agent = new Agent(cmd, inst);
		agent.startup(vmStartupTime, agentPreStartup);
		inst.addTransformer(agent.getTransformer(), true);
		inst.addTransformer(new TestRunnerTranformer());
		agent.retransformClasses(retransformableClasses);
		
		debug("after retransform");
	}

	/**
	 * see the tutorial for bootstrap classloader at: https://www.baeldung.com/java-classloaders
	 * @param inst
	 * @throws Exception
	 */
	private static void installBootstrap(Instrumentation inst) throws Exception {
		debug("install jar to boostrap...");
		File tempFolder = FileUtils.createTempFolder("microbat");
		debug("Temp folder to extract jars: " + tempFolder.getAbsolutePath());
		List<JarFile> bootJarPaths = getJarFiles("instrumentator_all.jar");
		if (bootJarPaths.isEmpty()) {
			bootJarPaths = getJarFiles(INSTRUMENTATION_STANTDALONE_JAR, 
										"bcel-6.0.jar",
										"javassist.jar",
										SAV_JAR,
										"commons-io-1.3.2.jar",
				/* 
				 * LLT: mysql-connector-java-5.1.44-bin.jar & slf4j-api-1.7.12.jar
				 * used to be added to bootstrap classloader to avoid ClassNotFoundException when the trace is
				 * being stored into sql database using TraceRecorder, but later, we don't use that approach to
				 * record trace anymore, so these two lines are commented to avoid any unnecessary cause.
				 *  */
										"sqlite-jdbc-3.32.3.2.jar"
//										"slf4j-api-1.7.12.jar"
										);
		}
		for (JarFile jarfile : bootJarPaths) {
			debug("append to boostrap classloader: " + jarfile.getName());
			inst.appendToBootstrapClassLoaderSearch(jarfile);
			if (jarfile.getName().contains("sqlite-jdbc")) {
				inst.appendToSystemClassLoaderSearch(jarfile);
			}
		}
	}

	private static List<JarFile> getJarFiles(String... jarNames) throws Exception {
		File tempFolder = FileUtils.createTempFolder("microbat");
		List<JarFile> jars = new ArrayList<>();
		
		boolean isUptodate = checkInstrumentatorVersion(tempFolder.getAbsolutePath());
		for (String jarName : jarNames) {
			File file = new File(tempFolder.getAbsolutePath(), jarName);
			if ((!isUptodate && CollectionUtils.existIn(file.getName(), INSTRUMENTATION_STANTDALONE_JAR, SAV_JAR)) || !file.exists()) {
				try {
					String jarResourcePath = "lib/" + jarName;
					boolean success = extractJar(jarResourcePath, file.getAbsolutePath());
					if (!success) {
						debug("Could not extract jar: " + jarResourcePath);
						if (jarName.endsWith(INSTRUMENTATION_STANTDALONE_JAR)) {
							jars.clear();
							return jars;
						}
						continue;
					}
					debug("Extracted jar: " + jarResourcePath);
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
	
	private static boolean checkInstrumentatorVersion(String tempFolder) {
		if (testMode) {
			return false;
		}
		File file = new File(tempFolder, INSTRUMENTATION_STANTDALONE_JAR);
		return file.exists();
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
			FileUtils.copy(inputJarStream, outStream);
		} finally {
			if (outStream != null) {
				outStream.close();
			}
		}
		return true;
	}

	private static Class<?>[] getRetransformableClasses(Instrumentation inst) {
		debug("Collect retransformable classes....");
		List<Class<?>> candidates = new ArrayList<Class<?>>();
		Class<?>[] classes = inst.getAllLoadedClasses();
		for (Class<?> c : classes) {
			if (inst.isModifiableClass(c) && inst.isRetransformClassesSupported()
					&& !ClassLoader.class.equals(c)) {
				candidates.add(c);
			}
		}
		candidates.remove(Thread.class);
		debug(candidates.size() + " transformable candidates");
		return candidates.toArray(new Class<?>[candidates.size()]);
	}

	private static void debug(String msg) {
		if (testMode) {
			System.out.println(msg);
		}
	}
}
