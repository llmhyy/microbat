package microbat.instrumentation.output;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import microbat.model.trace.Trace;
import sav.common.core.SavRtException;

/**
 * Wrapper class over Traces collected in an execution
 * @author dingyuchen
 *
 */
public class RunningInfo {
	private static final String HEADER = "TracingResult";
	private List<Trace> traceList;
	private String programMsg;
	private int expectedSteps;
	private int collectedSteps;
	
	public RunningInfo(String programMsg, List<Trace> traceList, int expectedSteps, int collectedSteps) {
		this.programMsg = programMsg;
		this.traceList = traceList;
		this.expectedSteps = expectedSteps;
		this.collectedSteps = collectedSteps;
	}
	
	public static RunningInfo readFromFile(String execTraceFile) { 
		return readFromFile(new File(execTraceFile));
	}
	
	public static RunningInfo readFromFile(File execTraceFile) { 
		TraceOutputReader reader = null;
		InputStream stream = null;
		try {
			stream = new FileInputStream(execTraceFile);
			reader = new TraceOutputReader(new BufferedInputStream(stream), execTraceFile.getParent());
			String header = reader.readString();
			String programMsg;
			int expectedSteps = 0;
			int collectedSteps = 0;
			if (HEADER.equals(header)) {
				programMsg = reader.readString();
				expectedSteps = reader.readInt();
				collectedSteps = reader.readInt();
			} else {
				programMsg = header; // for compatible reason with old version. TO BE REMOVED.
			}
			List<Trace> traceList = reader.readTrace();
			return new RunningInfo(programMsg, traceList, expectedSteps, collectedSteps);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SavRtException(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Trace getMainTrace() {
		for(Trace trace: traceList) {
			if(trace.isMain()) {
				return trace;
			}
		}
		
		return null;
	}
	
	public void saveToFile(String dumpFile, boolean append) throws IOException {
		File file = new File(dumpFile);
		String traceExecFolder = file.getParent();
		final FileOutputStream fileStream = new FileOutputStream(dumpFile, append);
		// Avoid concurrent writes from other processes:
		fileStream.getChannel().lock();
		final OutputStream bufferedStream = new BufferedOutputStream(fileStream);
		TraceOutputWriter outputWriter = null;
		try {
			outputWriter = new TraceOutputWriter(bufferedStream, traceExecFolder,
					file.getName().substring(0, file.getName().lastIndexOf(".")));
			outputWriter.writeString(HEADER);
			outputWriter.writeString(programMsg);
			outputWriter.writeInt(expectedSteps);
			outputWriter.writeInt(collectedSteps);
			outputWriter.writeTrace(traceList);
		} finally {
			bufferedStream.close();
			if (outputWriter != null) {
				outputWriter.close();
			}
			if (fileStream != null) {
				fileStream.close();
			}
		}
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

	public int getCollectedSteps() {
		return collectedSteps;
	}

	public void setCollectedSteps(int actualSteps) {
		this.collectedSteps = actualSteps;
	}

	public boolean isExpectedStepsMet() {
		return (expectedSteps < 0) || (expectedSteps == collectedSteps);
	}

	@Override
	public String toString() {
		return "RunningInfo [programMsg=" + programMsg + ", expectedSteps=" + expectedSteps + ", collectedSteps="
				+ collectedSteps + "]";
	}

	public List<Trace> getTraceList() {
		return traceList;
	}

	public void setTraceList(List<Trace> traceList) {
		this.traceList = traceList;
	}
}
