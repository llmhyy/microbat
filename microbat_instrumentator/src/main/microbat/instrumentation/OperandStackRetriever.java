package microbat.instrumentation;

import java.lang.instrument.Instrumentation;

public class OperandStackRetriever {
	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("start instrumentation...");
        inst.addTransformer(new OperandRetrievingTransfomer(), true);
    }
}
