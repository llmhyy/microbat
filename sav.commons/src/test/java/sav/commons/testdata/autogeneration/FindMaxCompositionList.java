package sav.commons.testdata.autogeneration;

import java.util.List;

public class FindMaxCompositionList implements IFindMax{
	private List<IFindMax> numbers;

	public FindMaxCompositionList(List<IFindMax> num) {
		this.numbers = num;
	}

	public int Max() {
		int result = Integer.MIN_VALUE;
		for (int i = 0; i < numbers.size(); i++) {
			int tempMax = numbers.get(i).Max();
			if (result < tempMax) {
				result = tempMax;
			}
		}

		return result;
	}

	public boolean check(int result) {

		for (int i = 0; i < numbers.size(); i++) {
			if (result < numbers.get(i).Max()) {
				return false;
			}
		}

		for (int i = 0; i < numbers.size(); i++) {
			if (result == numbers.get(i).Max()) {
				return true;
			}
		}
		
		
		return false;
	}
}
