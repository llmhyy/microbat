package microbat.mutation.trace;

import java.io.File;
import java.util.Arrays;

import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.FileUtils;
import tregression.empiricalstudy.TestCase;

public class MuBugInfo {
	private static final String FIX_EXEC_FILE_NAME = "fix.exec";
//	private static final String MU_EXEC_FILE_NAME = "bug.exec";
	
	private String bugExec;
	private String fixExec;
	private TestCase tc;
	private File muFile;
	private String className;
	private File orgFile;
	
	public static MuBugInfo parse(String bugExec) {
		String[] bugExecFrags = bugExec.replace("\\", "/").split("/");
		/* fix.exec path */
		String tcFolder = FileUtils.getFilePath(Arrays.copyOf(bugExecFrags, bugExecFrags.length - 2));
		String fixExec = FileUtils.getFilePath(tcFolder, FIX_EXEC_FILE_NAME);
		
		/* testcase */
		TestCase tc = new TestCase(bugExecFrags[bugExecFrags.length - 3]);
		
		/* class Simple Name */
		String classSimpleName = bugExecFrags[bugExecFrags.length - 2].split("_")[1];
		
		File muFile = FileUtils.getFileEndWith(
				FileUtils.getFilePath(CollectionUtils.toArrayList(bugExecFrags, bugExecFrags.length - 1)),
				classSimpleName + ".java");
		if (muFile == null) {
			throw new SavRtException("Cannot find mutated java file corresponding to execFile: " + bugExec);
		}
		/* class Name */
		String className = muFile.getName();
		int eIdx = className.lastIndexOf(".");
		if (eIdx > 0) {
			className = className.substring(0, eIdx);
		}
		/* original java file */
		File orgFile = new File(FileUtils.getFilePath(tcFolder, className + ".java"));
		MuBugInfo info = new MuBugInfo();
		info.bugExec = bugExec;
		info.fixExec = fixExec;
		info.tc = tc;
		info.muFile = muFile;
		info.className = className;
		info.orgFile = orgFile;
		return info;
	}

	public String getFixExec() {
		return fixExec;
	}

	public TestCase getTc() {
		return tc;
	}

	public File getMuFile() {
		return muFile;
	}

	public String getClassName() {
		return className;
	}

	public File getOrgFile() {
		return orgFile;
	}
	
	public String getBugExec() {
		return bugExec;
	}
	
}
