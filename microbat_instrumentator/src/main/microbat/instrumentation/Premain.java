package microbat.instrumentation;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import microbat.instrumentation.trace.TraceTransformer;

public class Premain {
	private static final String AGENT_JAR_FOLDER = "E:/lyly/Projects/microbat/master/microbat_instrumentator/src/test/resources/";
	private static final String AGENT_JAR = AGENT_JAR_FOLDER + "microbat_rt.jar";
	private static final String AGENT_JAR_TEST = AGENT_JAR_FOLDER +  "microbat_instrumentator.jar";
	
	public static void premain(String agentArgs, Instrumentation inst) throws Exception {
		Class<?>[] retransformableClasses = getRetransformableClasses(inst);
		installBootstrap(Arrays.asList(AGENT_JAR), inst);
		
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
				JarFile jarfile = new JarFile(new File(bootJarPath));
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
