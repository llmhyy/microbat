package microbat.agent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import microbat.instrumentation.output.TraceOutputReader;
import microbat.model.trace.Trace;
import sav.common.core.SavRtException;

public class ExecTraceFileReader {
	private String msg;
	private Trace trace;
	
	public Trace read(String execTraceFile) {
		return read(new File(execTraceFile));
	}
	
	public Trace read(File execTraceFile) {
		TraceOutputReader reader = null;
		InputStream stream = null;
		try {
			stream = new FileInputStream(execTraceFile);
			reader = new TraceOutputReader(new BufferedInputStream(stream));
			this.msg = reader.readString();
			trace = reader.readTrace();
			return trace;
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
	
	public String getMsg() {
		return msg;
	}
}
