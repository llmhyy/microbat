package microbat.filedb.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import microbat.dbtest.SampleClass;
import microbat.filedb.RecordsFileException;
import microbat.filedb.store.FileDb;
import microbat.filedb.store.reflection.RTypeFactory;
import microbat.model.BreakPoint;
import microbat.model.ControlScope;
import microbat.model.SourceScope;

/**
 * @author LLT
 *
 */
public class DbServiceTest {

	@Test
	public void testStoreBreakpoint() throws RecordsFileException {
		// FIXME XUEZHI [1]: fill more data for bkp, you can add more cases to test.
		BreakPoint bkp = new BreakPoint("micorbat.dbtest.SampleClass", "sampleMethod", 20);
		ControlScope controlScope =new ControlScope(null,false,true);
		SourceScope loopScope =new SourceScope("sampleMethod",20,21);
		bkp.setControlScope(controlScope);
		bkp.setLoopScope(loopScope);
		FileDb dbService = new FileDb();
		List<BreakPoint> bkps = new ArrayList<>();
		bkps.add(bkp);
		dbService.insertBatch(bkps, BreakPoint.class);
	}
	
	@Test
	public void testCreateType() throws RecordsFileException {
		RTypeFactory factory = new RTypeFactory();
		factory.create(SampleClass.class);
	}
}
