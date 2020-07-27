package microbat.filedb.store;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LLT
 * Table which holds only data part, not header
 */
public class Table<T> {
	private TableHeader header;
	private Class<T> clazz;
	private String tableName; 
	private List<String> columns = new ArrayList<String>();
	
	public Table(Class<T> clazz) {
		this.clazz = clazz;
		microbat.filedb.annotation.Table tableAnnotation = clazz.getAnnotation(microbat.filedb.annotation.Table.class);
		tableName = tableAnnotation.name();
		
	}
	
	public void insert(T row) {
		
	}
	
	public void insertBatch(List<T> rows) {
		// FIXME XUEZHI [2]: implement as in task description.
		
	}
}
