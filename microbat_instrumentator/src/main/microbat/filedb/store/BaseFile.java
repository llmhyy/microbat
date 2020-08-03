package microbat.filedb.store;

import java.io.File;
import java.io.IOException;

import microbat.filedb.store.DataFile.AccessMode;

/**
 * @author LLT
 */
public abstract class BaseFile {
	/* storage file for a recordType */
	private DataFile file;
	
	protected long curPointer;
	
	public BaseFile(String recordPath, AccessMode mode) throws Exception {
		File f = new File(recordPath);
		if (f.exists()) {
			file = new DataFile(f, mode);
		} else {
			if (mode == AccessMode.READ_ONLY) {
				throw new IOException("filedb does not exist!");
			}
			file = new DataFile(f, mode);
		}
	}
	
	private void readMetadata() throws IOException {
		file.seek(0l);
	}

	protected void writeLong(long pointer, long value) throws IOException {
		file.seek(pointer);
		file.writeLong(value);
		curPointer = file.getFilePointer();
	}
	
	protected void writePrimitive(Class<?> type, Object value) throws IOException {
		/*
		 * FIXME XUEZHI: check type, then using corresponding method in DataFile file to write value
		 *  
		 */
		curPointer = file.getFilePointer();
	}
	
	public static class Metadata {
		int numberOfRecords;
		long dataAddr;
	}
}
