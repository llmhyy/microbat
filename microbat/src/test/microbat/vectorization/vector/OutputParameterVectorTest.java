package microbat.vectorization.vector;

import static org.junit.Assert.*;

import org.junit.Test;

import microbat.vectorization.vector.InputParameterVector;
import microbat.vectorization.vector.OutputParameterVector;
import microbat.vectorization.vector.Vector;

public class OutputParameterVectorTest {

	@Test
	public void testVoidOutput() {
		final String typeDescriptor = "V";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
		final Vector expected = new InputParameterVector(element);
		OutputParameterVector vector = new OutputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testPrimitiveOutput() {
		final String typeDescriptor = "I";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		final Vector expected = new InputParameterVector(element);
		OutputParameterVector vector = new OutputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testArrayPrimitiveOutput() {
		final String typeDescriptor = "[I";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		final Vector expected = new InputParameterVector(element);
		OutputParameterVector vector = new OutputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testLibObjOutput() {
		final String typeDescriptor = "Ljava/lang/Object;";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		final Vector expected = new InputParameterVector(element);
		OutputParameterVector vector = new OutputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testSelfDefinedObjOutput() {
		final String typeDescriptor = "LmyClass;";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		final Vector expected = new InputParameterVector(element);
		OutputParameterVector vector = new OutputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testArrayObjOutput() {
		final String typeDescriptor = "[[[Ljava/lang/Object;";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f};
		final Vector expected = new InputParameterVector(element);
		OutputParameterVector vector = new OutputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}

}
