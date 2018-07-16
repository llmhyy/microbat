package microbat.instrumentation.utils;

/**
 * @author LLT
 *
 */
public class CollectionUtils {
	public static <T> boolean existIn(T val, T... valList) {
		for (T valInList : valList) {
			if (val.equals(valInList)) {
				return true;
			}
		}
		return false;
	}
}
