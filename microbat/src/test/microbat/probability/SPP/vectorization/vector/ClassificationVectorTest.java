package microbat.probability.SPP.vectorization.vector;

import static org.junit.Assert.*;

import org.junit.Test;
import microbat.model.trace.TraceNode;

public class ClassificationVectorTest {

	@Test
	public void testVector_1() {
		TraceNode node = new TraceNode();
		// int a = 1+1;
		node.setBytecode("iconst_2[5](1):istore_1[60](1):");
		
		ClassificationVector vector = new ClassificationVector(node);
		final float[] element = {
			0.0f, 0.0f, 0.0f, 0.0f, 
			0.0f, 0.0f, 1.0f, 1.0f, 
		};
		ClassificationVector expected = new ClassificationVector(element); 
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVector_2() {
		TraceNode node = new TraceNode();
		// int b = 1;
		node.setBytecode("iconst_1[4](1):istore_2[61](1)");
		
		ClassificationVector vector = new ClassificationVector(node);
		final float[] element = {
			0.0f, 0.0f, 0.0f, 0.0f, 
			0.0f, 0.0f, 1.0f, 1.0f, 
		};
		ClassificationVector expected = new ClassificationVector(element); 
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVector_3() {
		TraceNode node = new TraceNode();
		// int c = a+b;
		node.setBytecode("iload_1[27](1):iload_2[28](1):iadd[96](1):istore_3[62](1):");
		
		ClassificationVector vector = new ClassificationVector(node);
		final float[] element = {
			1.0f, 0.0f, 0.0f, 0.0f, 
			0.0f, 0.0f, 1.0f, 1.0f, 
		};
		ClassificationVector expected = new ClassificationVector(element); 
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVector_4() {
		TraceNode node = new TraceNode();
		// int d = function(c);
		node.setBytecode("iload_3[29](1):invokestatic[184](3) 16:istore[54](2) 4:");
		
		ClassificationVector vector = new ClassificationVector(node);
		final float[] element = {
			0.0f, 1.0f, 0.0f, 0.0f, 
			0.0f, 0.0f, 1.0f, 1.0f, 
		};
		ClassificationVector expected = new ClassificationVector(element); 
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVector_5() {
		TraceNode node = new TraceNode();
		// return c;
		node.setBytecode("iload_0[26](1):ireturn[172](1):");
		
		ClassificationVector vector = new ClassificationVector(node);
		final float[] element = {
			0.0f, 0.0f, 0.0f, 0.0f, 
			1.0f, 0.0f, 1.0f, 0.0f, 
		};
		ClassificationVector expected = new ClassificationVector(element); 
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVector_6() {
		TraceNode node = new TraceNode();
		// if (c == 1) {
		node.setBytecode("iload_3[29](1):iconst_1[4](1):if_icmpne[160](3) -> 22:");

		ClassificationVector vector = new ClassificationVector(node);
		final float[] element = {
			0.0f, 0.0f, 1.0f, 0.0f, 
			0.0f, 0.0f, 1.0f, 0.0f, 
		};
		ClassificationVector expected = new ClassificationVector(element); 
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVector_7() {
		TraceNode node = new TraceNode();
		// d = d << 1;
		node.setBytecode("iload[21](2) 4:iconst_1[4](1):ishl[120](1):istore[54](2) 4:");

		ClassificationVector vector = new ClassificationVector(node);
		final float[] element = {
			0.0f, 0.0f, 0.0f, 0.0f, 
			0.0f, 1.0f, 1.0f, 1.0f, 
		};
		ClassificationVector expected = new ClassificationVector(element); 
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVector_8() {
		TraceNode node = new TraceNode();
		// new[187](3) 3:dup[89](1):invokespecial[183](3) 8:astore[58](2) 5:
		node.setBytecode("new[187](3) 3:dup[89](1):invokespecial[183](3) 8:astore[58](2) 5:");

		ClassificationVector vector = new ClassificationVector(node);
		final float[] element = {
			0.0f, 1.0f, 0.0f, 1.0f, 
			0.0f, 0.0f, 0.0f, 1.0f, 
		};
		ClassificationVector expected = new ClassificationVector(element); 
		assertEquals(vector, expected);
	}
}
