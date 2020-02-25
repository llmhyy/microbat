package microbat.examples.benchmark;

public class Benchmark2 {
	public int test(int[] a, double b){
		if(a.length > 5){
			if(a[3] == 20 && b == 20){
				return 0;
			}
		}
		
		return 1;
	}
}
