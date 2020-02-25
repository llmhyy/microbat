package microbat.examples.benchmark;

public class Benchmark {
	public int test(int a, int b){
		if(isValid(a, b)){
			if(isInCorner(a, b)){
				return 0;
			}
		}
		
		return 1;
	}

	private boolean isInCorner(int a, int b) {
		return Math.abs(a-b)>=9;
	}

	private boolean isValid(double a, double b) {
		if(Math.pow(a, 2)<=25){
			if(Math.pow(b, 2)<=25){
				return true;
			}
		}
		return false;
	}
}
