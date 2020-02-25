/**
 * 
 */
package microbat.examples.benchmark.test;

import org.junit.Test;

import microbat.examples.benchmark.Benchmark;

/**
 * @author lyly
 *
 */
public class BenchmarkTest {

	@Test
	public void test1() {
		Benchmark bm = new Benchmark();
		bm.test(1, 2);
	}
}
