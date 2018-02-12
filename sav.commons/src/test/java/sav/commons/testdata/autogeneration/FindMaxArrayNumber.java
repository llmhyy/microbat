package sav.commons.testdata.autogeneration;

public class FindMaxArrayNumber<T extends Number> implements IFindMax {
	private T[] numbers;
	
	public FindMaxArrayNumber(T[] num) {
		this.numbers = num;
	}
	
	public int Max() {
		return -1;
	}

	public boolean check(int result){
		return true;
	}
}
