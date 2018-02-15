package microbat.instrumentation.trace.testdata;

public class Sample4 {

	private int a;
	private int b;
	
	public void test(Object o){
		String v = null;
		if(o!=null){
			v = String.valueOf(null);
		}
	}

	public static int getObject(int i){
		if(i==0){
			i++;
			return 111;
		}
		
		return 100;
	}
	
	
	public void foo(int[] a, int b) throws Exception {
		while(b>0){
			if(b>1){
				a[b] = 3;
				throw new Exception();
			}
			else{
				a[1] = 2;
			}
			b--;
		}
	}

	public static void main(String[] args){
		Sample4 test = new Sample4();
		test.a = 0;
		test.b = test.a*2 - 1;
		
		int[] c = new int[]{1, 2, 3};
		
		for(int i=0; i<3; i++){
			try {
				test.foo(c, i);
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		
		int k = getObject(c[0]);
		test.a = k+1;
		System.out.println("done");
	}

}
