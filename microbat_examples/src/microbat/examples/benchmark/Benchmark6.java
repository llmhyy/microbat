package microbat.examples.benchmark;

public class Benchmark6 {
	int[] a;
	double b;
	
	public int test(){
		if(a.length > 5){
			if(a[3] == 20 && b == 20){
				return 0;
			}
		}
		
		return 1;
	}

	public int[] getA() {
		return a;
	}

	public void setA(int[] a) {
		this.a = a;
	}

	public double getB() {
		return b;
	}

	public void setB(double b) {
		this.b = b;
	}
	
	
}
