package microbat.filedb.store;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LLT
 * Table which holds only data part, not header
 */
public class RecordsHolder<T> {
	private RecordsHeader header;
	private Class<T> clazz;
	private String recordName; 
	private List<String> recordAttributes = new ArrayList<String>();
	
	public RecordsHolder(Class<T> clazz) {
		this.clazz = clazz;
		microbat.filedb.annotation.RecordType recordType = clazz.getAnnotation(microbat.filedb.annotation.RecordType.class);
		recordName = recordType.name();
		
	}
	
	public void insert(T row) {
		
	}
	
	public void insertBatch(List<T> rows) {
		// FIXME XUEZHI [2]: implement as in task description.
		
	}
}
