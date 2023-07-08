package microbat.vectorization.vector;

import static org.junit.Assert.*;

import org.junit.Test;
import microbat.model.variable.*;
import microbat.vectorization.vector.VariableVector;
import microbat.model.value.*;

public class VariableVectorTest {

	@Test
	public void testVariable_1() {
		Variable var = new LocalVar(null, "int", null, 1);
		VarValue varValue = new PrimitiveValue(null, true, var);
		varValue.computationalCost = 0.5;
		VariableVector vector = new VariableVector(varValue);
		
		final float[] element = {
			1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 
			0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f, 
		};
		VariableVector expected = new VariableVector(element);
		assertEquals(vector, expected);
	}
	

	@Test
	public void testVariable_2() {
		Variable var = new FieldVar(false, null, "byte[]", null);
		VarValue varValue = new PrimitiveValue(null, true, var);
		varValue.computationalCost = 0.5;
		VariableVector vector = new VariableVector(varValue);
		
		final float[] element = {
			0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 
			1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 
		};
		VariableVector expected = new VariableVector(element);
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVariable_3() {
		Variable var = new FieldVar(true, null, "short", null);
		VarValue varValue = new PrimitiveValue(null, true, var);
		varValue.computationalCost = 0.5;
		VariableVector vector = new VariableVector(varValue);
		
		final float[] element = {
			0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f, 
		};
		VariableVector expected = new VariableVector(element);
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVariable_4() {
		Variable var = new LocalVar(null, "java.lang.Object", null, 1);
		VarValue varValue = new PrimitiveValue(null, true, var);
		varValue.computationalCost = 0.5;
		VariableVector vector = new VariableVector(varValue);
		
		final float[] element = {
			0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 
			0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.5f, 
		};
		VariableVector expected = new VariableVector(element);
		assertEquals(vector, expected);
	}
	
	@Test
	public void testVariable_5() {
		Variable var = new LocalVar(null, "java.lang.myObject[]", null, 1);
		VarValue varValue = new PrimitiveValue(null, true, var);
		varValue.computationalCost = 0.5;
		VariableVector vector = new VariableVector(varValue);
		
		final float[] element = {
			0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 
			1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f, 
		};
		VariableVector expected = new VariableVector(element);
		assertEquals(vector, expected);
	}
}
