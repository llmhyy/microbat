package microbat.vectorization.vector;

import static org.junit.Assert.*;

import org.junit.Test;

import microbat.vectorization.vector.Vector;

import java.util.Set;
import java.util.HashSet;;

public class VectorTest {

	@Test
	public void testEmptyVector() {
		final Vector vector = new Vector();
		assertEquals(0, vector.getSize());
	}
	
	@Test
	public void testSet() {
		final float[] array = {1.0f, 0.0f, 0.0f};
		final Vector vector = new Vector(array);
		vector.set(1);
		
		final float[] array2 = {1.0f, 1.0f, 0.0f};
		final Vector expected = new Vector(array2);
		assertEquals(expected, vector);
	}
	
	@Test
	public void testString() {
		final float[] array = {1.0f, 1.0f};
		final Vector vector = new Vector(array);
		final String expected = "1.0,1.0";
		assertEquals(expected, vector.toString());
	}
	
	@Test
	public void testEquals() {
		final float[] array1 = {1.0f, 1.0f};
		final float[] array2 = {1.0f, 2.0f};
		final float[] array3 = {1.0f};
		
		final Vector vector1 = new Vector(array1);
		final Vector vector2 = new Vector(array2);
		final Vector vector3 = new Vector(array3);
		final Vector vector4 = new Vector(array1);
		
		assertFalse(vector1.equals(vector2));
		assertFalse(vector1.equals(vector3));
		assertTrue(vector1.equals(vector4));
	}
	
	@Test
	public void testHashCode() {
		final float[] array1 = {1.0f, 1.0f};
		final float[] array2 = {1.0f, 2.0f};
		final float[] array3 = {1.0f};
		
		final Vector vector1 = new Vector(array1);
		final Vector vector2 = new Vector(array2);
		final Vector vector3 = new Vector(array3);
		final Vector vector4 = new Vector(array1);
		
		Set<Vector> set = new HashSet<>();
		set.add(vector1);
		set.add(vector2);
		set.add(vector3);
		set.add(vector4);
		
		assertEquals(3, set.size());
		assertTrue(set.contains(vector1));
		assertTrue(set.contains(vector2));
		assertTrue(set.contains(vector3));
	}
	
	@Test
	public void testCosineSimilarity() {
		final float[] array1 = {0.2f, 0.7f, 0.5f};
		final Vector vector1 = new Vector(array1);
		
		final float[] array2 = {0.4f, 0.1f, 0.2f};
		final Vector vector2 = new Vector(array2);
		
		final float cosSim = Vector.calCosSim(vector1, vector2);
		final float expected = 0.617708f;
		assertEquals(expected, cosSim, 0.001);
	}
}
