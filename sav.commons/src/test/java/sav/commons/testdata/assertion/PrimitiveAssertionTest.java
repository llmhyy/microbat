package sav.commons.testdata.assertion;
public class PrimitiveAssertionTest {
	public double foo(int a, int b) {
		int r = a;
		r = r + b;
		int c = 2;
		r = r + c;
		return Math.sqrt(r); 
	}
}
