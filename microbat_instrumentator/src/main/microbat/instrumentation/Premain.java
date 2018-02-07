package microbat.instrumentation;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.jar.JarFile;

import microbat.instrumentation.trace.TraceTransformer;

public class Premain {

	public static void premain(String agentArgs, Instrumentation inst) throws Exception {
//		logLoadedClasses(inst);
		installBootstrap(Arrays.asList("E:/lyly/Projects/microbat/master/microbat_instrumentator/src/test/resources/microbat.instrumentator.jar"), inst);
//		installBootstrap(Arrays.asList("E:/lyly/Projects/microbat/master/microbat_instrumentator/src/test/resources/testcall.jar"), inst);
		
		System.out.println("start instrumentation...");
		final Agent agent = new Agent();
		agent.startup();
        inst.addTransformer(new TraceTransformer(), true);
        /* TODO list all retransform classes. */
        inst.retransformClasses(Random.class);
        System.out.println("after retransform");
    }

	private static void installBootstrap(List<String> bootJarPaths, Instrumentation inst) throws IOException {
		System.out.println("install jar to boostrap...");
		for (String bootJarPath : bootJarPaths) {
			try {
				JarFile jarfile = new JarFile(new File(bootJarPath));
				inst.appendToBootstrapClassLoaderSearch(jarfile);
			} catch (IOException ioe) {
				System.err.println("unable to open boot jar file : " + bootJarPath);
				throw ioe;
			}
		}		
	}
	
	private static void logLoadedClasses(Instrumentation inst) {
		System.out.println("List all loaded classes....");
		Class[] classes = inst.getAllLoadedClasses();
		List<Class> candidates = new ArrayList<Class>();
		for (Class c : classes) {
			if (inst.isModifiableClass(c) && inst.isRetransformClassesSupported()) {
				System.out.println(c.getName());
			}
		}
	}
}
