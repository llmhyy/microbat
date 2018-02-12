package sav.commons.testdata.assertion;

import java.util.Stack;

public class StackAssertionTest {
	public void foo(Stack<Integer> stk) {
		// 
		stk.push(1);
		stk.pop();
		stk.pop();
	}
}
