package microbat.instrumentation;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import com.mysql.jdbc.Messages;

import microbat.instrumentation.trace.TraceTransformer;

public class Premain {
	private static final String BASE_DIR = "E:/lyly/Projects/microbat/master/microbat_instrumentator/";
	private static final String AGENT_JAR = BASE_DIR + "src/test/resources/microbat_rt.jar";
	
	public static void premain(String agentArgs, Instrumentation inst) throws Exception {
//		installBootstrap(Arrays.asList(AGENT_JAR), inst);
		installBootstrap(Arrays.asList(
				BASE_DIR + "lib/mysql-connector-java-5.1.44-bin.jar",
				BASE_DIR + "src/test/resources/microbat_instrumentator.jar",
				BASE_DIR + "lib/bcel-6.0.jar",
				BASE_DIR + "lib/commons-cli-1.2.jar",
				BASE_DIR + "lib/commons-io-1.3.2.jar",
				BASE_DIR + "lib/commons-lang-2.6.jar",
				BASE_DIR + "lib/sav.commons.jar",
				BASE_DIR + "lib/slf4j-api-1.7.12.jar",
				BASE_DIR + "lib/slf4j-log4j12-1.7.12.jar"
				), inst);
		Class<?>[] retransformableClasses = getRetransformableClasses(inst);
		
		System.out.println("start instrumentation...");
		final Agent agent = new Agent(agentArgs);
		agent.startup();
		inst.addTransformer(new TraceTransformer(agent.getAgentParams().getEntryPoint()), true);
		if (retransformableClasses.length > 0) {
        	inst.retransformClasses(retransformableClasses);
        }
        System.out.println("after retransform");
    }

	private static void installBootstrap(List<String> bootJarPaths, Instrumentation inst) throws IOException {
		System.out.println("install jar to boostrap...");
		for (String bootJarPath : bootJarPaths) {
			try {
				System.out.println(bootJarPath);
				JarFile jarfile = new JarFile(new File(bootJarPath), false);
				inst.appendToBootstrapClassLoaderSearch(jarfile);
			} catch (IOException ioe) {
				System.err.println("unable to open boot jar file : " + bootJarPath);
				throw ioe;
			}
		}		
	}
	
	private static Class<?>[] getRetransformableClasses(Instrumentation inst) {
		System.out.println("Collect retransformable classes....");
		List<Class<?>> candidates = new ArrayList<Class<?>>();
		Class<?>[] classes = inst.getAllLoadedClasses();
		for (Class<?> c : classes) {
			if (inst.isModifiableClass(c) && inst.isRetransformClassesSupported()) {
				if (!Excludes.isExcluded(c.getName())) {
					candidates.add(c);
					System.out.println(c.getName());
				}
			}
		}
		candidates.add(ArrayList.class);
		return candidates.toArray(new Class<?>[candidates.size()]);
	}
}
