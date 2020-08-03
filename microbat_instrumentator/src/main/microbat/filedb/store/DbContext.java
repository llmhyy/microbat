package microbat.filedb.store;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LLT
 *
 */
public class DbContext {
	private Map<Class<?>, RecordsFile<?>> recordsFileMap = new HashMap<Class<?>, RecordsFile<?>>();

	@SuppressWarnings("unchecked")
	public <T>RecordsFile<T> getRecordsFile(Class<T> clazz) {
		RecordsFile<T> table;
		try {
			table = (RecordsFile<T>) recordsFileMap.get(clazz);
			if (table == null) {
				table = (RecordsFile<T>) new RecordsFile<>(this, clazz);
				recordsFileMap.put(clazz, table);
			}
			return table;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
