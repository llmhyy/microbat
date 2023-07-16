package microbat.debugpilot.propagation.spp;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ModelActionTest {

	@Test
	public void testParseSuspicion() {
		ModelPrediction action = ModelPrediction.parseString("0");
		assertEquals(action, ModelPrediction.SUSPICION);
	}
	
	@Test
	public void testParseNotSuspicion() {
		ModelPrediction action = ModelPrediction.parseString("1");
		assertEquals(action, ModelPrediction.NOT_SUSPICION);
	}
	
	@Test
	public void testParseUncertain() {
		ModelPrediction action = ModelPrediction.parseString("2");
		assertEquals(action, ModelPrediction.UNCERTAIN);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testParseIllegalArgumentException1() {
		@SuppressWarnings("unused")
		ModelPrediction action = ModelPrediction.parseString("3");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testParseIllegalArgumentException2() {
		@SuppressWarnings("unused")
		ModelPrediction action = ModelPrediction.parseString("some input");
	}
	
	@Test
	public void testParseStringList() {
		List<ModelPrediction> actionList = ModelPrediction.parseStringList("0,0,1,2");
		List<ModelPrediction> expectedList = new ArrayList<>();
		expectedList.add(ModelPrediction.SUSPICION);
		expectedList.add(ModelPrediction.SUSPICION);
		expectedList.add(ModelPrediction.NOT_SUSPICION);
		expectedList.add(ModelPrediction.UNCERTAIN);
		assertEquals(actionList, expectedList);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testParseStringList1() {
		@SuppressWarnings("unused")
		List<ModelPrediction> action = ModelPrediction.parseStringList("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testParseStringList2() {
		@SuppressWarnings("unused")
		List<ModelPrediction> action = ModelPrediction.parseStringList(",");
	}
	
}
