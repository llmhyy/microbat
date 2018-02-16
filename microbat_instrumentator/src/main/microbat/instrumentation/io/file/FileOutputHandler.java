package microbat.instrumentation.io.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import microbat.instrumentation.io.TraceOutputWriter;
import microbat.model.trace.Trace;

public class FileOutputHandler {
	private File file;

	public FileOutputHandler(final String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		final File folder = file.getParentFile();
		if (folder != null) {
			folder.mkdirs();
		}
		this.file = file;
	}

	public void save(String message, Trace trace, final boolean append) throws IOException {
		final FileOutputStream fileStream = new FileOutputStream(file, append);
		// Avoid concurrent writes from other processes:
		fileStream.getChannel().lock();
		final OutputStream bufferedStream = new BufferedOutputStream(fileStream);
		TraceOutputWriter outputWriter = null;
		try {
			outputWriter = new TraceOutputWriter(bufferedStream);
			outputWriter.writeString(message);
			outputWriter.writeTrace(trace);
		} finally {
			bufferedStream.close();
			if (outputWriter != null) {
				outputWriter.close();
			}
		}
	}

}
