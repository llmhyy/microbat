package microbat.instrumentation.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestCodeRangeUserFilter {

	@Test
	public void testIsHitMethod() {
		// case 0 M(2,4) C(1,3)
		int msl = 2, mel = 4, csl = 1, cel = 3;		
		assertTrue(Math.max(msl, csl) <= Math.min(mel, cel));
		// case 1 M(2,4) C(3,5)
		int msl1 = 2, mel1 = 4, csl1 = 3, cel1 = 5;
		assertTrue(Math.max(msl1, csl1) <= Math.min(mel1, cel1));
		
		// case 2 M(2,4) C(3,4)
		int msl2 = 2, mel2 = 4, csl2 = 3, cel2 = 4;
		assertTrue(Math.max(msl2, csl2) <= Math.min(mel2, cel2));
		// case 3 M(2,4) C(2,4)
		int msl3 = 2, mel3 = 4, csl3 = 2, cel3 = 4;
		assertTrue(Math.max(msl3, csl3) <= Math.min(mel3, cel3));
		// case 4 M(1,4) C(2,3)
		int msl4 = 1, mel4 = 4, csl4 = 2, cel4 = 3;
		assertTrue(Math.max(msl4, csl4) <= Math.min(mel4, cel4));
		// case5 M(2,3) C(1,4)
		int msl5 = 2, mel5 = 3, csl5 = 1, cel5 = 4;
		assertTrue(Math.max(msl5, csl5) <= Math.min(mel5, cel5));
		// case6 M(2,3) C(3,4)
		int msl6 = 2, mel6 = 3, csl6 = 3, cel6 = 4;
		assertTrue(Math.max(msl6, csl6) <= Math.min(mel6, cel6));

		// case7 M(2,3) C(1,2)
		int msl7 = 2, mel7 = 3, csl7 = 1, cel7 = 2;
		assertTrue(Math.max(msl7, csl7) <= Math.min(mel7, cel7));
		// case8 M(2,3) C(0,1)
		int msl8 = 2, mel8 = 3, csl8 = 0, cel8 = 1;
		assertFalse(Math.max(msl8, csl8) <= Math.min(mel8, cel8));
	
	}
}
