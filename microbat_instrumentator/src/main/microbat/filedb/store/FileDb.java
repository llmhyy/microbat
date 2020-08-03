package microbat.filedb.store;

import java.util.List;

/**
 * @author LLT
 *
 */
public class FileDb {
	private DbContext dbContext;
	
	public FileDb() {
		dbContext = new DbContext();
	}

	public <T> void insertBatch(List<T> listObj, Class<T> clazz) {
		RecordsFile<T> table = dbContext.getRecordsFile(clazz);
		table.insertBatch(listObj);
	}
	
	public <T> void insert(T record, Class<T> clazz) {
		RecordsFile<T> table = dbContext.getRecordsFile(clazz);
		table.insert(record);
	}

}
