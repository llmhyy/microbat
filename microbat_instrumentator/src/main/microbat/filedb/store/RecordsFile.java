package microbat.filedb.store;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LLT
 * file format: 
 * 
 * [long] headerStartPos 
 * [int] numberOfRecordHeader  
 * [long] dataStartPos
 * [Header]
 * 		[int] keyType
 * 		[keyType] keyValue
 * 		[long] recordPos
 * 		[long] recordEndPos
 * 
 * 		[int] keyType
 * 		[keyType] keyValue
 * 		[long] recordPos
 * 		[long] recordEndPos
 * 		...
 * 
 * [Records]
 * 		[record]
 *	 		[attribute type] attributeValue
 * 			[attribute type] attributeValue
 * 			....
 * 		
 * 		[record]
 *	 		[attribute type] attributeValue
 * 			[attribute type] attributeValue
 * 			....
 * 
 * 		....
 * 
 * methods:
 * 	- removeHeaderByKey: update keyType to 
 *  - updateHeaderByKey
 *  - expandHeader:  moveRecordToTheEnd, update dataStartPos, updateHeaderByKey
 *  - calculateHeaderSize(class<T> reportType, int initRecordsCapacity)
 *  - findHeaderPositionByHeaderOrder
 */
public class RecordsFile<T> extends BaseFile {
	private DbContext dbContext;
	private RecordsHeader header;
	private Class<T> clazz;
	private String recordName; 
	private List<String> recordAttributes = new ArrayList<String>();
	
	public RecordsFile(DbContext dbContext, Class<T> clazz) throws Exception {
		super(null, null);
		this.dbContext = dbContext;
		this.clazz = clazz;
		microbat.filedb.annotation.RecordType recordType = clazz.getAnnotation(microbat.filedb.annotation.RecordType.class);
		recordName = recordType.name();
	}
	
	public String insert(T row) {
		String key = null; // FIXME LLT: identify key
		long recordPos = header.getRecordPos(key);
		if (recordPos < 0) {
			// doInsert
			/* 
			 * 
			 */
			// dbContext.getRecordsFile(clazz);
		} 
		return key;
	}
	
	public void insertBatch(List<T> rows) {
		// FIXME XUEZHI [2]: implement as in task description.
		
	}
	
	public T loadRecord(String key) {
		long recordPos = header.getRecordPos(key);
		return loadRecord(recordPos);
	}
	
	private T loadRecord(long recordPos) {
		// FIXME XUEZHI
		/* 
		 * Based on record annotations, load object value from file as below:
		 * - if attribute is an object load 
		 */
		
		return null;
	}
}
