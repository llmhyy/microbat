package microbat.trace;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import microbat.codeanalysis.runtime.ExecutionStatementCollector;
import microbat.model.BreakPoint;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

public class TraceTest {

	class OriginalSettings{
		String projectName;
		String launchClass;
		boolean isRecordSnapshort;
		int stepLimit;
		int referenceFieldLayerInString;
		
	}
	
	private OriginalSettings originalSettings = new OriginalSettings();
	
	@Before
	public void setup(){
		originalSettings.projectName = Settings.projectName;
		originalSettings.launchClass = Settings.launchClass;
		originalSettings.isRecordSnapshort = Settings.isRecordSnapshot;
		originalSettings.stepLimit = Settings.stepLimit;
		originalSettings.referenceFieldLayerInString = Settings.getVariableLayer();
	}
	
	@After
	public void tearDown(){
		Settings.projectName = originalSettings.projectName;
		Settings.launchClass = originalSettings.launchClass;
		Settings.isRecordSnapshot = originalSettings.isRecordSnapshort;
		Settings.stepLimit = originalSettings.stepLimit;
		Settings.setVariableLayer(originalSettings.referenceFieldLayerInString);
	}
	
	@Test
	public void testRetrieveRunningStatement() {
		Settings.projectName = "Bugs";
		Settings.stepLimit = 10000;
		Settings.launchClass = "com.simplecalculator.SimpleCalculator";
		
		AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
		ExecutionStatementCollector collector = new ExecutionStatementCollector();
		List<BreakPoint> executingStatements = collector.collectBreakPoints(appClassPath, false);
		
		System.out.println(executingStatements);
		
		assertEquals(55, executingStatements.size());
	}
	
	@Test
	public void testReadWrttienVariable(){
		
	}
	
	@Test
	public void testCFGConstructor(){
		
	}
	
	
	@Test
	public void testTraceConstruction() {
		
	}

}
