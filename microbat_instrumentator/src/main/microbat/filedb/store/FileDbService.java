package microbat.filedb.store;

import java.util.List;

/**
 * @author LLT
 *
 */
public class FileDbService {

	public <T> void insertBatch(List<T> listObj, Class<T> clazz) {
		Table<T> table = new Table<>(clazz);
		table.insertBatch(listObj);
	}
	
}
