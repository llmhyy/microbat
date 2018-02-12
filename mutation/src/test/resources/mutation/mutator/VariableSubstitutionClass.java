
package mutation.mutator;

public class VariableSubstitutionClass {

	public int a;
	public double b;
	private int c;
	public String d;
	
	public void method1(){
		double x0 = 1;
		int x1 = 0;
		x1 = x1 + a;
		int x2 = 1;
		x2 = x1 + x2;
	}
	
	public void method2(int para1){
		int y1 = 0;
		y1 = a + c;
		double y2 = 1;
		
		for(int i = 0; i < 10; i++){
			double y3 = y2 + y1;
		}
	}
	
	public class InnerClass{
		public void method3(){
			int z1 = 0;
			double z2 = 1.2;
			double z3 = z1 + c;
		}
	}
	
	public void method4(){
		int p1 = 0;{int p2 = p1;
		}
	}
}
