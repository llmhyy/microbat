package microbat.instrumentation.trace.testdata;

import java.util.List;

public class RefVar {
	public static String staticStr;
	public static long staticLong;
	public static double staticDouble;
	public static int staticInt;
	public static List<String> staticList;
	public String varName;
	public int val;
	public long longVal;
	public double doubleVal;
	
	public String getVarName() {
		return varName;
	}
}
