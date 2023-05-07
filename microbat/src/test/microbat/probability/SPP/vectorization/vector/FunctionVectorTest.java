package microbat.probability.SPP.vectorization.vector;

import static org.junit.Assert.*;

import org.junit.Test;
import microbat.model.trace.TraceNode;

public class FunctionVectorTest {

	@Test
	public void testSpecialMethod() {
		final TraceNode node = new TraceNode();
		node.addInvokingMethod("exp6.FunctionVectorTest#<init>()V");
		node.setBytecode("new[187](3) 1:dup[89](1):invokespecial[183](3) 16:astore_1[76](1):");
		
		final float[] element = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
				                 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
				                 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
		
		Vector vector = new Vector(element);
		
		FunctionVector[] vectors = FunctionVector.constructFuncVectors(node, 1);
		assertEquals(vector, vectors[0]);
	}
	
	@Test
	public void testVirualMethod() {
		final TraceNode node = new TraceNode();
		node.addInvokingMethod("exp6.FunctionVectorTest#function1(I)I");
		node.setBytecode("aload_1[43](1):iconst_0[3](1):invokevirtual[182](3) 17:pop[87](1):");
		
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
				                 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
				                 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		
		Vector vector = new Vector(element);
		
		FunctionVector[] vectors = FunctionVector.constructFuncVectors(node, 1);
		assertEquals(vector, vectors[0]);
	}
	
	@Test
	public void testLibObjMethod() {
		final TraceNode node = new TraceNode();
		node.addInvokingMethod("java.lang.Math#addExact(II)I");
		node.setBytecode("iconst_0[3](1):iconst_0[3](1):invokestatic[184](3) 25:pop[87](1):");
		
		final float[] element = {0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
				                 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
				                 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		
		Vector vector = new Vector(element);
		
		FunctionVector[] vectors = FunctionVector.constructFuncVectors(node, 1);
		assertEquals(vector, vectors[0]);
	}

}
