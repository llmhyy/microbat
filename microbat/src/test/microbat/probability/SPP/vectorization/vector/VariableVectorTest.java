package microbat.probability.SPP.vectorization.vector;

import static org.junit.Assert.*;

import org.junit.Test;
import microbat.model.variable.*;
import microbat.model.value.*;

public class VariableVectorTest {

	@Test
	public void testVariable_1() {
		Variable var = new LocalVar(null, "int", null, 1);
		VarValue varValue = new PrimitiveValue(null, true, var);
		varValue.computationalCost = 0.5;
		VariableVector vector = new VariableVector(varValue);
		
		final float[] expected = {
			1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 
			0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 
		};
		
	}

}
