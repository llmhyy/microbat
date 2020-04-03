package microbat.instrumentation.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestCodeRangeEntry {
	List<CodeRangeEntry> entries =new ArrayList<>(2);
	List<String> cmdlines= new ArrayList<>(2);
	
	@Before
	public void setup() {
		CodeRangeEntry codeRangeEntry= new CodeRangeEntry("Sample1",11,22);
		CodeRangeEntry codeRangeEntry2= new CodeRangeEntry("Sample2",18,22);
		entries.add(codeRangeEntry);
		entries.add(codeRangeEntry2);
		cmdlines.add(codeRangeEntry.toString());
		cmdlines.add(codeRangeEntry2.toString());
		
	}
	@Test
	public void testParse() {		
		List<CodeRangeEntry> entries2=CodeRangeEntry.parse(cmdlines);
		StringBuffer s2= new StringBuffer();
		for (CodeRangeEntry parseEntry : entries2) {
			s2.append(parseEntry.toString());
		}
		StringBuffer s1= new StringBuffer();
		for (CodeRangeEntry entry : entries) {
			s1.append(entry.toString());
		}
		assertEquals(s1.toString(), s2.toString());
	}

}
