package microbat.instrumentation.trace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.Test;

import microbat.instrumentation.trace.testdata.Sample;
import sav.common.core.utils.FileUtils;

/**
 * @author LLT
 *
 */
public class InstrumenterTest {
	private final String CLASS_FOLDER = "E:/lyly/Projects/microbat/master/microbat_instrumentator/bin";
	private final String INSTRUMENT_TARGET_FOLDER = "E:/lyly/Projects/inst_src";

	@Test
	public void writeFile() throws Exception {
		String className = Sample.class.getName();
		
		String classPath = className.replace(".", "/") + ".class";
		String clazzFile = new StringBuilder("/").append(classPath).toString();

		File outFile = getFile(INSTRUMENT_TARGET_FOLDER, clazzFile);
		FileOutputStream out = new FileOutputStream(outFile);
		System.out.println(outFile.getAbsolutePath());
		File inFile = new File(CLASS_FOLDER + clazzFile);
		FileInputStream in = new FileInputStream(inFile);

		byte[] data = new byte[33000];
		in.read(data);
		data = instrument(data, className);
//		System.out.println(new String(data));
		out.write(data);
		out.close();
		in.close();
	}

	private File getFile(String folder, String fileName) throws Exception {
		File file = new File(folder + fileName);
		FileUtils.getFileCreateIfNotExist(file.getPath());
		return file;
	}

	private byte[] instrument(byte[] data, String className) throws Exception {
//		TraceTransformer transformer = new TraceTransformer();
//		return transformer.instrument(className, data, new NormalInstrumenter());
//		BcelTraceTransformer transformer = new BcelTraceTransformer();
//		return transformer.instrument(className, data);
//		FieldTransformer transformer = new FieldTransformer();
//		return transformer.instrument(className, data);
		TraceInstrumenter transformer = new TraceInstrumenter();
		return transformer.instrument(className, data);
	}
}
