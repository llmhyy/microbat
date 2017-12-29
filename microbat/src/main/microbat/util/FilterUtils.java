package microbat.util;

public class FilterUtils {
	private FilterUtils(){}
	private static final String SUFFIX = ".*";
	
	/* pattern: text.*   */
	public static String getPrefix(String filterText) {
		return filterText.replace(SUFFIX, "");
	}
	
	public static String toFilterText(String prefix) {
		return prefix + SUFFIX;
	}
	
	/**
	 * [from, to]
	 **/
	public static String toPkgFilterText(String[] pkgFrags, int from, int to) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i <= to; i++) {
			sb.append(pkgFrags[i]);
			if (i != to) {
				sb.append(".");
			}
		}
		sb.append(SUFFIX);
		return sb.toString();
	}
	
	/**
	 * s1 is subFilter of s2 when:
	 * s1 = a.b.c.* | a.b.c
	 * s2 = a.b.*
	 * */
	public static boolean isSubFilter(String s1, String s2) {
		int n1 = s1.length();
		int n2 = s2.length();
		if (n1 <= n2) {
			return false;
		}
		for (int i = 0; i < n2; i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			if (c2 == '*') {
				return true;
			}
			if (c1 != c2) {
				return false;
			}
		}
		return false;
	}
}
