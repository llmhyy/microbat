package microbat.examples.benchmark;

public class Benchmark3 {
	public int test(int[] a, String b){
		if(a.length > 5){
			if(a[3] == 20 && b.startsWith("aa")){
				return 0;
			}
		}
		
		return 1;
	}
}
