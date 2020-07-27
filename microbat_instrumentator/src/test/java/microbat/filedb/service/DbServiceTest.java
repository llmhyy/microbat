package microbat.filedb.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import microbat.filedb.store.FileDbService;
import microbat.model.BreakPoint;

/**
 * @author LLT
 *
 */
public class DbServiceTest {

	@Test
	public void testStoreBreakpoint() {
		// FIXME XUEZHI [1]: fill more data for bkp, you can add more cases to test.
		BreakPoint bkp = new BreakPoint("micorbat.dbtest.SampleClass", "sampleMethod", 20);
		FileDbService dbService = new FileDbService();
		List<BreakPoint> bkps = new ArrayList<>();
		bkps.add(bkp);
		dbService.insertBatch(bkps, BreakPoint.class);
	}
}
