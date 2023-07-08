package microbat.vectorization.vector;

import static org.junit.Assert.*;

import org.junit.Test;

import microbat.vectorization.vector.InputParameterVector;
import microbat.vectorization.vector.Vector;

public class InputParameterVectorTest {

	@Test
	public void testPrimitiveTypeInputs() {
		final String typeDescriptor = "IBSJFDZCLjava/lang/String;";
		final float[] element = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		final Vector expected = new InputParameterVector(element);
		InputParameterVector vector = new InputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testArrayPrimitive() {
		final String typeDescriptor = "[I[B[S[J[F[D[Z[C[Ljava/lang/Object;";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
								 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f};
		final Vector expected = new InputParameterVector(element);
		InputParameterVector vector = new InputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testHighDimArrayInputs() {
		final String typeDescriptor = "[[I[II";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		final Vector expected = new InputParameterVector(element);
		InputParameterVector vector = new InputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testSelfDefinedObjInputs() {
		final String typeDescriptor = "LmyClass;[LmyClass;";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
		final Vector expected = new InputParameterVector(element);
		InputParameterVector vector = new InputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testDoubleObjInputs() {
		final String typeDescriptor = "[[Ljava/lang/Object;";
		final float[] element = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
								 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
		final Vector expected = new InputParameterVector(element);
		InputParameterVector vector = new InputParameterVector(typeDescriptor);
		assertEquals(expected, vector);
	}

}
