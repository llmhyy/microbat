package microbat.filedb.store;

import java.util.List;

import microbat.filedb.RecordsFileException;
import microbat.filedb.store.DataFile.AccessMode;
import microbat.filedb.store.reflection.RAttribute;
import microbat.filedb.store.reflection.RColAttribute;
import microbat.filedb.store.reflection.RType;

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
	private RType<T> rType;

	public RecordsFile(DbContext dbContext, Class<T> clazz) throws Exception {
		super("/test.db", AccessMode.READ_WRITE);
		this.dbContext = dbContext;
		rType = dbContext.getrTypeFactory().create(clazz);
		RecordsHeader header = new RecordsHeader(getDataFile());
	}

	public Object insert(T row) throws RecordsFileException {
		// write header
		// write key
		Object key = null;
		writeRecordData(row, rType);
		return key;
	}

	private void writeRecordData(Object row, RType<?> type) throws RecordsFileException {
		for (RAttribute attr : type.getAttributes()) {
			try {
				System.out.print(attr.getName() + " = ");
				Object attrValue = attr.getGetter().invoke(row);
				if (attr.isPrimiveType()) {
					writePrimitive(attr.getClassName(), attrValue);
				} else {
					if (attr.isEmbedded()) {
						writeRecordData(attrValue, attr.getRType());
					} else if (!attr.isCollectionAttr()) {
						Object keyValue = dbContext.getRecordsFile(attr.getRType().getOwner()).insert(attrValue);
						RAttribute<?> attrKey = attr.getRType().getKey();
						writePrimitive(attrKey.getClassName(), keyValue);
					} else {
						RColAttribute col = (RColAttribute) attr;
						if (col.isListAttr()) {
							//TODO: store list
						} else {
							throw new RecordsFileException(String.format("attribute Collection type {%s} not supported yet!", col.getClassName()));
						}
					}
				}
			} catch (Exception e) {
				throw new RecordsFileException(String.format("Cannot retrieve value for field %s", attr.getName()), e);
			}
		}
	}
	

	public void insertBatch(List<T> rows) throws RecordsFileException {
		for (T row : rows) {
			insert(row);
		}
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