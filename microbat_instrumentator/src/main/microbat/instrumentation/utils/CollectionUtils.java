package microbat.instrumentation.utils;

import java.util.List;

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

	public static boolean isEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}
}
