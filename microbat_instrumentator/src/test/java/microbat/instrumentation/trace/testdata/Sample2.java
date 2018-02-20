package microbat.instrumentation.trace.testdata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.CollectionUtils;

public class Sample2 {
	String str;
	
	public boolean testArr() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		/* WRITE LOCAL VAR */
		int intVal = 10;
		long longVal = 234l;
		double doubleVal = 23;
		int[][] a = new int[][] { { 1, 2 }, { 3, 4 } };
		String[] b = new String[] {"bele0", "bele1", "bele2", "bele3", "bele4"};
		str = "abc";
		RefVar var = new RefVar();
		intVal ++;
		intVal += -3;
		intVal --;
		longVal ++;
		System.out.println(longVal);
		char bol = 'a';
		bol += 1;
		/* READ ARRAY ELEMENT */
		String b0 = b[0];
		int a01 = a[0][1];
		
		for (int i = 0; i < 2; i++) {
			a[0][i] = i;
		}
		/* WRITE ARRAY ELEMENT */
		a[0][0] = 5;
		a[0][1] = 7;
		a[1][0] = 10;
		b[1] = b0 + "abcd";
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
		
		/* INVOKE */
		Method method = ClassUtils.loockupMethodByNameOrSign(RefVar.class, "getVarName()Ljava/lang/String;").get(0);
		String toStrg = (String) method.invoke(var);
		System.out.println(toStrg);
		return true;
	}
	
}
