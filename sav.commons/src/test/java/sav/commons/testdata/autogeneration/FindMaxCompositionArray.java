package sav.commons.testdata.autogeneration;

public class FindMaxCompositionArray implements IFindMax{
	private IFindMax[] numbers;
	
	public FindMaxCompositionArray(IFindMax[] num){
		this.numbers = num;
	}
	
	public int Max()
	{
		int result = Integer.MIN_VALUE;
		for(int i = 0; i < numbers.length; i++){
			int tempMax = numbers[i].Max();
			if(result < tempMax){
				result = tempMax;
			}
		}
		
		return result;
	}

	public boolean check(int result){
		
		for(int i = 0; i < numbers.length; i++){
			if(result < numbers[i].Max()){
				return false;
			}
		}
		
		for(int i = 0; i < numbers.length; i++){
			if(result == numbers[i].Max()){
				return true;
			}
		}
		return false;
	}
}
