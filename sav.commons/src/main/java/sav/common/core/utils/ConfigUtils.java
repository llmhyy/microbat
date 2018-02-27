package sav.common.core.utils;


public class ConfigUtils {

	/**
	 * Try to get the property identified with 'name' parameter by first check
	 * in System Properties then Environment Properties.
	 * 
	 * @param name
	 *            Name of the property
	 * @return Value of the property, or <code>null</code> if nothing found
	 */
	public static String getProperty(final String name) {
		String value = System.getProperty(name);
		if (StringUtils.isEmpty(value)) {
			value = System.getenv(name);
		}
		return value;
	}
}
