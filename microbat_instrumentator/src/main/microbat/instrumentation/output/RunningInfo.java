package microbat.instrumentation.output;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import microbat.model.trace.Trace;
import sav.common.core.SavRtException;

public class RunningInfo {
	private static final String HEADER = "TracingResult";
	private Trace trace;
	private String programMsg;
	private int expectedSteps;
	private int actualSteps;
	
	public static RunningInfo readFromFile(String execTraceFile) { 
		return readFromFile(new File(execTraceFile));
	}
	
	public static RunningInfo readFromFile(File execTraceFile) { 
		TraceOutputReader reader = null;
		InputStream stream = null;
		try {
			stream = new FileInputStream(execTraceFile);
			reader = new TraceOutputReader(new BufferedInputStream(stream));
			RunningInfo info = new RunningInfo();
			String header = reader.readString();
			if (HEADER.equals(header)) {
				info.programMsg = reader.readString();
				info.expectedSteps = reader.readInt();
				info.actualSteps = reader.readInt();
			} else {
				info.programMsg = header; // for compatible reason with old version. TO BE REMOVED.
			}
			info.trace = reader.readTrace();
			return info;
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void saveToFile(String dumpFile, boolean append) throws IOException {
		final FileOutputStream fileStream = new FileOutputStream(dumpFile, append);
		// Avoid concurrent writes from other processes:
		fileStream.getChannel().lock();
		final OutputStream bufferedStream = new BufferedOutputStream(fileStream);
		TraceOutputWriter outputWriter = null;
		try {
			outputWriter = new TraceOutputWriter(bufferedStream);
			outputWriter.writeString(HEADER);
			outputWriter.writeString(programMsg);
			outputWriter.writeInt(expectedSteps);
			outputWriter.writeInt(actualSteps);
			outputWriter.writeTrace(trace);
		} finally {
			bufferedStream.close();
			if (outputWriter != null) {
				outputWriter.close();
			}
		}
	}
	
	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public String getProgramMsg() {
		return programMsg;
	}

	public void setProgramMsg(String programMsg) {
		this.programMsg = programMsg;
	}

	public int getExpectedSteps() {
		return expectedSteps;
	}

	public void setExpectedSteps(int expectedSteps) {
		this.expectedSteps = expectedSteps;
	}

	public int getActualSteps() {
		return actualSteps;
	}

	public void setActualSteps(int actualSteps) {
		this.actualSteps = actualSteps;
	}

	public boolean isExpectedStepsMet() {
		return (expectedSteps < 0) || (expectedSteps == actualSteps);
	}
}
