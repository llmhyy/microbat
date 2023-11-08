package microbat.baseline.factorgraph;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import microbat.debugpilot.propagation.BP.VarIDConverter;

public class VarIDConverterTest {
	
	private VarIDConverter converter;
	
	@Before
	public void setUp() throws Exception {
		this.converter = new VarIDConverter();
	}

	@Test
	public void testVarToGraph() {
		final String id = "vir_exp6.simpletesting.ProgramTest#testMissingVars(I)I:4";
		final String expected = "vir_exp6.simpletesting.ProgramTest#testMissingVars%I^I:4";
		assertEquals(expected, this.converter.varID2GraphID(id));
	}
	
	@Test
	public void testVarToGraph_2() {
		final String id = "exp6/simpletesting/ProgramTest{38,39}input-0";
		final String expected = "exp6/simpletesting/ProgramTest{38@39}input-0";
		assertEquals(expected, this.converter.varID2GraphID(id));
	}
	
	@Test
	public void testGraphToVar() {
		final String expected = "vir_exp6.simpletesting.ProgramTest#testMissingVars(I)I:4";
		final String id = "vir_exp6.simpletesting.ProgramTest#testMissingVars%I^I:4";
		assertEquals(expected, this.converter.graphID2VarID(id));
	}
	
	@Test
	public void testGraphToVar_2() {
		final String expected = "exp6/simpletesting/ProgramTest{38,39}input-0";
		final String id = "exp6/simpletesting/ProgramTest{38@39}input-0";
		assertEquals(expected, this.converter.graphID2VarID(id));
	}

}
