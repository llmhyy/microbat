package microbat.examples.benchmark;

public class Benchmark7 {
	public int test(int[] a, TestInterface s){
		if(a[3] == 20){
			if(s.getAttribute() > 6){
				return 3;
			}
			
			return 0;
		}
		
		return 1;
	}
}
