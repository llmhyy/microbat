package microbat.instrumentation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sav.common.core.SavRtException;

public class AgentUtils {
	
	public static File getFileCreateIfNotExist(String path) {
		File file = new File(path);
		if (!file.exists()) {
			File folder = file.getParentFile();
			if (!folder.exists()) {
				folder.mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new SavRtException(e);
			}
		}
		return file;
	}

	public static File createTempFolder(String folderName) throws Exception {
		File folder = getFileInTempFolder(folderName);
		if (folder.exists()) {
			if (folder.isDirectory()) {
				return folder;
			}
			throw new Exception(String.format("Cannot create temp folder: %s", folderName));
		}
		folder.mkdirs();
		return folder;
	}
	
	public static File getFileInTempFolder(String fileName) {
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(tmpdir, fileName);
		return file;
	}
	
	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024 * 4];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}
}
