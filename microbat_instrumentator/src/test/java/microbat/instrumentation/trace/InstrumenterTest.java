package microbat.instrumentation.trace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.Test;

import microbat.instrumentation.AgentParams;
import microbat.instrumentation.CommandLine;
import microbat.instrumentation.instr.TraceInstrumenter;
import microbat.instrumentation.instr.instruction.info.EntryPoint;
import microbat.instrumentation.trace.testdata.Sample3;
import sav.common.core.utils.FileUtils;

/**
 * @author LLT
 *
 */
public class InstrumenterTest {
	public static final String CLASS_FOLDER = "E:/lyly/Projects/microbat/master/microbat_instrumentator/bin";
	private static final String INSTRUMENT_TARGET_FOLDER = "E:/lyly/Projects/inst_src";

	@Test
	public void writeFile() throws Exception {
		String className = Sample3.class.getName();
		String classFolder = CLASS_FOLDER;
//		String className = "org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests";
//		String classFolder = "E:/linyun/bug_repo/Chart/1/bug/build-tests";
		String classPath = className.replace(".", "/") + ".class";
		String clazzFile = new StringBuilder("/").append(classPath).toString();

		///
		File outFile = getFile(INSTRUMENT_TARGET_FOLDER, clazzFile);
		FileOutputStream out = new FileOutputStream(outFile);
		System.out.println(outFile.getAbsolutePath());
		
		File inFile = new File(classFolder + clazzFile);
		FileInputStream in = new FileInputStream(inFile);

		byte[] data = new byte[100000];
		in.read(data);
		data = instrument(data, className);
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
		AgentParams params = new AgentParams(new CommandLine());
		params.setEntryPoint(new EntryPoint("", ""));
		TraceInstrumenter transformer = new TraceInstrumenter(params);
		return transformer.instrument(className, data);
	}
}
