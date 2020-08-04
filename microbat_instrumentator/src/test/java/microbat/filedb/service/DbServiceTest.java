package microbat.filedb.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import microbat.filedb.store.FileDb;
import microbat.model.BreakPoint;
import microbat.model.ControlScope;
import microbat.model.SourceScope;
import sav.commons.testdata.loopinvariant.Loop;

/**
 * @author LLT
 *
 */
public class DbServiceTest {

	@Test
	public void testStoreBreakpoint() {
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
}
