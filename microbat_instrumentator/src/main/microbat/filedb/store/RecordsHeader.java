package microbat.filedb.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LLT
 *
 */
public class RecordsHeader {
	private DataFile file;
	private Map<Object, Long> recordPosMap = new HashMap<>();
	private List<Long> idxPointers;
	private long curPointer;
	
	public RecordsHeader(DataFile file) {
		this.file = file;
	}
	
	public long getRecordPos(Object key) {
		// TODO Auto-generated method stub
		return -1;
	}
	
	
}
