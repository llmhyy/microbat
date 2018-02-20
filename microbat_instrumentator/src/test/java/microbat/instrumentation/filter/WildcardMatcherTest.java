package microbat.instrumentation.filter;

import org.junit.Test;

public class WildcardMatcherTest {

	@Test
	public void testMatcher() {
		WildcardMatcher matcher = initMatcher("java.util.*\\");
		match(matcher, "java.util.ArrayList");
		match(matcher, "java.util.ArrayList$Itr");
		match(matcher, "java.util.ArrayListFake");
		match(matcher, "java.util.Arrays");
		match(matcher, "java.lang.Object");
		match(matcher, "java.util.concurrent.ConcurrentMap");
		match(matcher, "java.util.log.logger.Log");
		
		matcher = initMatcher("java.util.*");
		match(matcher, "java.util.ArrayList");
		match(matcher, "java.util.ArrayList$Itr");
		match(matcher, "java.util.ArrayListFake");
		match(matcher, "java.util.Arrays");
		match(matcher, "java.lang.Object");
		
		
		matcher = initMatcher("java.util.ArrayList");
		match(matcher, "java.util.ArrayList");
		match(matcher, "java.util.ArrayList$Itr");
		match(matcher, "java.util.ArrayListFake");
		match(matcher, "java.util.Arrays");
		match(matcher, "java.lang.Object");
		
		matcher = initMatcher("java.util.ArrayList*");
		match(matcher, "java.util.ArrayList");
		match(matcher, "java.util.ArrayList$Itr");
		match(matcher, "java.util.ArrayListFake");
		match(matcher, "java.util.Arrays");
		match(matcher, "java.lang.Object");
	}
	
	private WildcardMatcher initMatcher(String expression) {
		System.out.println("\n\nmatcher: " + expression);
		return new WildcardMatcher(expression);
	}

	private void match(WildcardMatcher matcher, String s) {
		System.out.println(s + ": " + matcher.matches(s));
	}
}
