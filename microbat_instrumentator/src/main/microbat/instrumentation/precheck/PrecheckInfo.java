package microbat.instrumentation.precheck;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import microbat.instrumentation.AgentUtils;
import microbat.instrumentation.output.TraceOutputReader;
import microbat.instrumentation.output.TraceOutputWriter;
import microbat.model.ClassLocation;
import sav.common.core.SavRtException;

public class PrecheckInfo {
	private static final String HEADER = "Precheck";
	private int threadNum;
	private Set<ClassLocation> visitedLocs;
	private int stepTotal;
	
	private PrecheckInfo() {
		
	}

	public PrecheckInfo(int threadNum, TraceInfo info) {
		super();
		this.threadNum = threadNum;
		this.stepTotal = info.getStepTotal();
		visitedLocs = info.getVisitedLocs();
	}
	
	@Override
	public String toString() {
		return "PrecheckInfo [threadNum=" + threadNum + ", stepTotal=" + stepTotal + "]";
	}

	public static PrecheckInfo readFromFile(String filePath) {
		FileInputStream stream = null;
		TraceOutputReader reader = null;
		try {
			stream = new FileInputStream(filePath);
			reader = new TraceOutputReader(new BufferedInputStream(stream));
			String header = reader.readString();
			if (!HEADER.equals(header)) {
				throw new SavRtException("Invalid Precheck file result!");
			}
			PrecheckInfo infor = new PrecheckInfo();
			infor.threadNum = reader.readVarInt();
			infor.stepTotal = reader.readVarInt();
			int locationsSize = reader.readVarInt();
			Set<ClassLocation> visitedLocs = new HashSet<>(locationsSize);
			for (int i = 0; i < locationsSize; i++) {
				String className = reader.readString();
				String methodSignature = reader.readString();
				int lineNumber = reader.readInt();
				ClassLocation loc = new ClassLocation(className, methodSignature, lineNumber);
				visitedLocs.add(loc);
			}
			infor.visitedLocs = visitedLocs;
			return infor;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public void saveToFile(String filePath, boolean append) {
		OutputStream bufferedStream = null;
		TraceOutputWriter outputWriter = null;
		
		try {
		File file = AgentUtils.getFileCreateIfNotExist(filePath);
		final FileOutputStream fileStream = new FileOutputStream(file, append);
		// Avoid concurrent writes from other processes:
		fileStream.getChannel().lock();
		bufferedStream = new BufferedOutputStream(fileStream);
			outputWriter = new TraceOutputWriter(bufferedStream);
			outputWriter.writeString(HEADER);
			outputWriter.writeVarInt(threadNum);
			outputWriter.writeVarInt(stepTotal);
			outputWriter.writeVarInt(visitedLocs.size());
			for (ClassLocation loc : visitedLocs) {
				outputWriter.writeString(loc.getClassCanonicalName());
				outputWriter.writeString(loc.getMethodSign());
				outputWriter.writeInt(loc.getLineNumber());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedStream != null) {
					bufferedStream.close();
				}
				if (outputWriter != null) {
					outputWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
