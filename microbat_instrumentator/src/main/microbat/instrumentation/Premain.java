package microbat.instrumentation;

import java.lang.instrument.Instrumentation;

import microbat.instrumentation.trace.TraceTransformer;

public class Premain {

	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("start instrumentation...");
		final Agent agent = new Agent();
		agent.startup();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					agent.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
        inst.addTransformer(new TraceTransformer(), true);
    }
	
}
