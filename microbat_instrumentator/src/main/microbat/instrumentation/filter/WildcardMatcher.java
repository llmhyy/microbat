package microbat.instrumentation.filter;

import java.util.regex.Pattern;

import microbat.instrumentation.AgentConstants;

public class WildcardMatcher {

	private final Pattern pattern;

	/**
	 * Creates a new matcher with the given expression.
	 * 
	 *  java.util.* : include all types and packages under java.util package
	 *  java.util.*\ : include all types only under java.util package
	 *  java.util.Arrays : include type Arrays only
	 *  java.util.Arrays* : include type Arrays and its inner types
	 *  
	 */
	public WildcardMatcher(final String expression) {
		final String[] parts = expression.split(AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR);
		final StringBuilder regex = new StringBuilder(expression.length() * 2);
		boolean next = false;
		for (final String part : parts) {
			if (next) {
				regex.append('|');
			}
			regex.append('(').append(toRegex(part)).append(')');
			next = true;
		}
		pattern = Pattern.compile(regex.toString());
	}

	private static CharSequence toRegex(final String expression) {
		final StringBuilder regex = new StringBuilder(expression.length() * 2);
		String suffix = null;
		int endIdx = -1;
		// java.util.*
		if ((endIdx = endsWith(expression, ".*")) > 0) { 
			suffix = ".*"; // any character
		} 
		// java.util.*\
		else if ((endIdx = endsWith(expression, ".*\\")) > 0) {
			suffix = "[^\\.]*";
		} 
		// java.util.ArrayList*
		else if ((endIdx = endsWith(expression, "*")) > 0) {
			suffix = "(\\$.*)*";
			endIdx--;
		} 
		// java.util.ArrayList
		else {
			endIdx = expression.length() - 1;
		}
		
		char[] charArray = expression.toCharArray();
		for (int i = 0; i <= endIdx; i++) {
			char c = charArray[i];
			regex.append(Pattern.quote(String.valueOf(c)));
		}
		if (suffix != null) {
			regex.append(suffix);
		}
		return regex;
	}
	
	private static int endsWith(String value, String suffix) {
		if (value.endsWith(suffix)) {
			return value.length() - suffix.length();
		}
		return -1;
	}

	/**
	 * Matches the given string against the expressions of this matcher.
	 * @param s
	 *            string to test
	 * @return <code>true</code>, if the expression matches
	 */
	public boolean matches(final String s) {
		return pattern.matcher(s).matches();
	}

}
