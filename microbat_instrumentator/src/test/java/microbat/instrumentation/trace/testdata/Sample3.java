package microbat.instrumentation.trace.testdata;

public class Sample3 {
	private boolean booleanVal = true;
	
	public Sample3() {
		boolean a = true;
		Object objVal = Boolean.valueOf(true);
	}

	public static int getObject(int i){
		return i;
	}
	
	public static void main(String[] args){
		for(int i=0; i<3; i++){
			i+= -3;
		}
		System.out.println("new");
		int[] c = new int[]{1, 3};
		int k = getObject(c[0]);
		System.out.println("done");
	}

}
