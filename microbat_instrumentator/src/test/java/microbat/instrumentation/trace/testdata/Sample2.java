package microbat.instrumentation.trace.testdata;

import sav.common.core.utils.CollectionUtils;

public class Sample2 {
	String str;
	
	public boolean testArr() {
		int[][] a = new int[][] { { 1, 2 }, { 3, 4 } };
		str = "abc";
		RefVar var = new RefVar();
		RefVar.staticInt = 124;
		RefVar.staticDouble = 423;
		RefVar.staticLong = 34l;
		RefVar.staticList = CollectionUtils.listOf("str1");
		RefVar.staticStr = "strABC";
		var.varName = "name";
		var.val = 3;
		var.longVal = 100l;
		var.doubleVal = 234;
		int intVal = 10;
		long longVal = 234l;
		double doubleVal = 23;
		System.out.println(str);
		System.out.println(var.varName);
		System.out.println(var.longVal);
		System.out.println(var.val);
		return true;
	}
}
