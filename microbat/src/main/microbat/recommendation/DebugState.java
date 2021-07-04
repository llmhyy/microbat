package microbat.recommendation;

public class DebugState {
	public static final int SCLICING = 0;
	public static final int SKIP = 1;
	public static final int BINARY_SEARCH = 2;
	public static final int DETAIL_INSPECT = 3;
	public static final int UNCLEAR = 4;
	public static final int PARTIAL_CLEAR = 5;
	
	public static String printState(int debugState){
		switch(debugState){
		case 0:
			return "simple inference";
		case 1:
			return "skip";
		case 2:
			return "binary search";
		case 3:
			return "detail inspect";
		case 4:
			return "unclear";
		case 5:
			return "partial clear";
		}
		
		return null;
	}
}
