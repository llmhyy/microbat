package microbat.filedb.store;

import java.util.List;

/**
 * @author LLT
 *
 */
public class FileDb {

	public <T> void insertBatch(List<T> listObj, Class<T> clazz) {
		RecordsHolder<T> table = new RecordsHolder<>(clazz);
		table.insertBatch(listObj);
	}
	
}
