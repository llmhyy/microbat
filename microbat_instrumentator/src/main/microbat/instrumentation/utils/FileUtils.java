package microbat.instrumentation.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.AgentLogger;

public class FileUtils {
	public static List<String> readLines(String filePath) {
		if (filePath == null) {
			return null;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		List<String> lines = new ArrayList<>();
		List<Closeable> closables = new ArrayList<>();
		try {
			FileInputStream stream = new FileInputStream(file);
			closables.add(stream);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			closables.add(reader);
			String line = reader.readLine();
			while (line != null) {
				lines.add(line);
				line = reader.readLine();
			}
			return lines;
		} catch (Exception e) {
			AgentLogger.info("Read file error: " + e.getMessage());
			AgentLogger.error(e);
			return null;
		} finally {
			for (Closeable closeble : closables) {
				try {
					closeble.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
	
	public static List<String> readLines(Reader input) throws IOException {
		BufferedReader reader = new BufferedReader(input);
		List<String> list = new ArrayList<>();
		String line = reader.readLine();
		while (line != null) {
			list.add(line);
			line = reader.readLine();
		}
		return list;
	}
	
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
				throw new RuntimeException(e);
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
	
	public static void closeStreams(Closeable... closables) {
		for (Closeable closable : closables) {
			if (closable != null) {
				try {
					closable.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
}
