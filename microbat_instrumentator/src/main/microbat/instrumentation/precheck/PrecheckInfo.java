package microbat.instrumentation.precheck;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.StepMismatchChecker;
import microbat.instrumentation.output.TraceOutputReader;
import microbat.instrumentation.output.TraceOutputWriter;
import microbat.instrumentation.utils.FileUtils;
import microbat.model.ClassLocation;
import sav.common.core.SavRtException;

public class PrecheckInfo {
	private static final String HEADER = "Precheck";
	private int threadNum;
	private Set<ClassLocation> visitedLocs;
	private int stepTotal;
	private boolean isOverLong;
	private List<String> exceedingLimitMethods;
	private String programMsg;
	private List<String> loadedClasses;
	
	private PrecheckInfo() {
		
	}

	public PrecheckInfo(int threadNum, TraceInfo info) {
		super();
		AgentLogger.debug("Measurement ThreadNum = " + threadNum);
		this.setThreadNum(threadNum);
		this.setStepTotal(info.getStepTotal());
		setVisitedLocs(info.getVisitedLocs());
		isOverLong = info.isOverLong();
		StepMismatchChecker.logPrecheckSteps(info);
	}

	@Override
	public String toString() {
		return "PrecheckInfo [threadNum=" + threadNum + ", stepTcounted_stepTotalotal=" + stepTotal + ", isOverLong=" + isOverLong
				+ ", exceedingLimitMethods=" + exceedingLimitMethods + "]";
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
			infor.programMsg = reader.readString();
			infor.setThreadNum(reader.readVarInt());
			infor.isOverLong = reader.readBoolean();
			infor.setStepTotal(reader.readVarInt());
			int exceedingMethodsSize = reader.readVarInt();
			List<String> exceedingMethods = new ArrayList<String>(exceedingMethodsSize);
			for (int i = 0; i < exceedingMethodsSize; i++) {
				exceedingMethods.add(reader.readString());
			}
			infor.exceedingLimitMethods = exceedingMethods;
			int locationsSize = reader.readVarInt();
			Set<ClassLocation> visitedLocs = new HashSet<>(locationsSize);
			for (int i = 0; i < locationsSize; i++) {
				String className = reader.readString();
				String methodSignature = reader.readString();
				int lineNumber = reader.readInt();
				ClassLocation loc = new ClassLocation(className, methodSignature, lineNumber);
				visitedLocs.add(loc);
			}
			infor.setVisitedLocs(visitedLocs);
			int loadedClassesSize = reader.readVarInt();
			List<String> loadedClasses = new ArrayList<>(loadedClassesSize);
			for (int i = 0; i < loadedClassesSize; i++) {
				loadedClasses.add(reader.readString());
			}
			infor.loadedClasses = loadedClasses;
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
		FileOutputStream fileStream = null;
		
		try {
			File file = FileUtils.getFileCreateIfNotExist(filePath);
			fileStream = new FileOutputStream(file, append);
			try {
				// Avoid concurrent writes from other processes:
				fileStream.getChannel().lock();
			} catch (IOException e)  {
				// ignore
				AgentLogger.error(e);
			}
			bufferedStream = new BufferedOutputStream(fileStream);
			outputWriter = new TraceOutputWriter(bufferedStream);
			outputWriter.writeString(HEADER);
			outputWriter.writeString(programMsg);
			outputWriter.writeVarInt(threadNum);
			outputWriter.writeBoolean(isOverLong);
			outputWriter.writeVarInt(getStepTotal());
			outputWriter.writeVarInt(exceedingLimitMethods.size());
			for (String method : exceedingLimitMethods) {
				outputWriter.writeString(method);
			}
			outputWriter.writeVarInt(getVisitedLocs().size());
			for (ClassLocation loc : getVisitedLocs()) {
				outputWriter.writeString(loc.getClassCanonicalName());
				outputWriter.writeString(loc.getMethodSign());
				outputWriter.writeInt(loc.getLineNumber());
			}
			outputWriter.writeVarInt(loadedClasses.size());
			for (String loadedClass : loadedClasses) {
				outputWriter.writeString(loadedClass);
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
				if (fileStream != null) {
					fileStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	public Set<ClassLocation> getVisitedLocs() {
		return visitedLocs;
	}

	public void setVisitedLocs(Set<ClassLocation> visitedLocs) {
		this.visitedLocs = visitedLocs;
	}

	public int getStepTotal() {
		return stepTotal;
	}

	public void setStepTotal(int stepTotal) {
		this.stepTotal = stepTotal;
	}

	public boolean isOverLong() {
		return isOverLong;
	}

	public void setOverLong(boolean isOverLong) {
		this.isOverLong = isOverLong;
	}

	public void setExceedingLimitMethods(List<String> exceedingLimitMethods) {
		this.exceedingLimitMethods = exceedingLimitMethods;
	}
	
	public List<String> getExceedingLimitMethods() {
		return exceedingLimitMethods;
	}
	
	public String getProgramMsg() {
		return programMsg;
	}
	
	public void setProgramMsg(String programMsg) {
		this.programMsg = programMsg;
	}

	public List<String> getLoadedClasses() {
		return loadedClasses;
	}

	public void setLoadedClasses(List<String> loadedClasses) {
		this.loadedClasses = loadedClasses;
	}
	
}
