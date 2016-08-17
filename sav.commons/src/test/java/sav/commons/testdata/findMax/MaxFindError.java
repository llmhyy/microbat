package sav.commons.testdata.findMax;


public class MaxFindError {

	public static int findArrayMax(int[] tlist) {
		int max = tlist[0];
		for (int i = 1; i < tlist.length - 1; i++) {
			if (max < tlist[i]) {
				max = tlist[i];
			}
		}
		return max;
	}
	
	public static boolean checkMax(int[] tList, int max ){
		for(int i : tList){
			assert max >= i : "Not right maximum number";
			return true;
		}
		return false;
	}
}
