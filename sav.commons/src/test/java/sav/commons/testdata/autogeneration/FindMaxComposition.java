package sav.commons.testdata.autogeneration;

public class FindMaxComposition implements IFindMax{

	private IFindMax A;
	private IFindMax B;
	public FindMaxComposition(IFindMax A, IFindMax B){
		this.A = A;
		this.B = B;
	}
	
	public int Max()
	{
		return Math.max(A.Max(), B.Max());
	}

	public boolean check(int result){
		
		return true;
	}
}
