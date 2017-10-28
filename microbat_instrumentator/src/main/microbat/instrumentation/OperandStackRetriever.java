package microbat.instrumentation;

import java.lang.instrument.Instrumentation;

public class OperandStackRetriever {
	public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new OperandRetrievingTransfomer());
    }
}
