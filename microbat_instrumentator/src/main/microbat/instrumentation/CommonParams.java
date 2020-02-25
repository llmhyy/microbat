package microbat.instrumentation;

import java.util.ArrayList;
import java.util.List;

import microbat.instrumentation.AgentParams.LogType;
import microbat.instrumentation.filter.UserFilters;

/**
 * @author LLT
 *
 */
public class CommonParams {
	public static final String OPT_CLASS_PATH = "class_path";
	public static final String OPT_WORKING_DIR = "working_dir";
	public static final String OPT_LOG = "log";
	
	private List<String> classPaths = new ArrayList<>();
	private String workingDirectory;
	private List<LogType> logTypes;
	private UserFilters userFilters = new UserFilters();
	
	public CommonParams() {
		
	}
	
	public CommonParams(CommandLine cmd) {
		classPaths = cmd.getStringList(OPT_CLASS_PATH);
		logTypes = LogType.valuesOf(cmd.getStringList(OPT_LOG));
		workingDirectory = cmd.getString(OPT_WORKING_DIR);
	}
	
	public List<String> getClassPaths() {
		return classPaths;
	}

	public void setClassPaths(List<String> classPaths) {
		this.classPaths = classPaths;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	public List<LogType> getLogTypes() {
		return logTypes;
	}

	public UserFilters getUserFilters() {
		return userFilters;
	}
}
