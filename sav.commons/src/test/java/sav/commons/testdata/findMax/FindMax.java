package sav.commons.testdata.findMax;


public class FindMax {

	public static void Test()
	{
		int c = 2;
		int[] arr = new int[]{1,c, 4, 3};
		findMax(arr, c);
	}
	
	public static int findMax(int[] arr, int c) {
		int max = arr[0];
		for (int i = 1; i < arr.length - 1; i++) {
			if (max < arr[i]) {
				max = arr[i];
			}
			max = c;
		}
//		assert max > 110;
		return max;
	}
}
