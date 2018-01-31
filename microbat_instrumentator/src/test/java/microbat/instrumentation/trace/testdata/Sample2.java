package microbat.instrumentation.trace.testdata;

import sav.common.core.utils.CollectionUtils;

public class Sample2 {
	String str;
	
	public boolean testArr() {
		/* WRITE LOCAL VAR */
		int intVal = 10;
		long longVal = 234l;
		double doubleVal = 23;
		int[][] a = new int[][] { { 1, 2 }, { 3, 4 } };
		str = "abc";
		RefVar var = new RefVar();
		intVal ++;
		intVal += 3;
		intVal --;
		longVal ++;
//		/* WRITE FIELD */
		var.varName = "name";
		var.val = 3;
		var.longVal = 100l;
		var.doubleVal = 234;
		/* WRITE STATIC */
		RefVar.staticInt = 124;
		RefVar.staticDouble = 423;
		RefVar.staticLong = 34l;
		RefVar.staticList = CollectionUtils.listOf("str1");
		RefVar.staticStr = "strABC";
		/* READ LOCAL VAR */
		System.out.println(a);
		var.toString();
		System.out.println(intVal);
		System.out.println(longVal);
		System.out.println(doubleVal);
		System.out.println(str);
//		/* READ FIELD */
//		System.out.println(str);
		System.out.println(var.varName);
		System.out.println(var.longVal);
		System.out.println(var.val);
//		/* READ STATIC */
		System.out.println(RefVar.staticInt);
		System.out.println(RefVar.staticDouble);
		System.out.println(RefVar.staticList);
		
		return true;
	}
	
}
