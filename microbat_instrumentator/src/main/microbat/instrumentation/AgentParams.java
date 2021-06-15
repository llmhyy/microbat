package microbat.instrumentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import microbat.instrumentation.filter.CodeRangeEntry;
import microbat.instrumentation.instr.instruction.info.EntryPoint;
import microbat.instrumentation.utils.FileUtils;
import microbat.sql.TraceRecorder;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.StringUtils;
import sav.strategies.dto.AppJavaClassPath;

/**
 * 
 * @author lyly
 *
 */
public class AgentParams extends CommonParams {
	public static final String OPT_CLASS_PATH = CommonParams.OPT_CLASS_PATH;
	public static final String OPT_WORKING_DIR = CommonParams.OPT_WORKING_DIR;
	public static final String OPT_LOG = CommonParams.OPT_LOG;
	
	public static final String OPT_PRECHECK = "precheck";
	public static final String OPT_ENTRY_POINT = "entry_point";
	public static final String OPT_LAUNCH_CLASS = "launch_class";
	public static final String OPT_JAVA_HOME = "java_home";
	public static final String OPT_DUMP_FILE = "dump_file_path";
	public static final String OPT_TCP_PORT = "tcp_port";
	public static final String OPT_INCLUDES = "includes";
	public static final String OPT_EXCLUDES = "excludes";
	public static final String OPT_VARIABLE_LAYER = "varLayer";
	public static final String OPT_STEP_LIMIT = "stepLimit";
	public static final String OPT_EXPECTED_STEP = "expectedSteps";
	public static final String OPT_INCLUDES_FILE = "includes_file";
	public static final String OPT_EXCLUDES_FILE = "excludes_file";
	public static final String OPT_OVER_LONG_METHODS = "overlong_methods";
	public static final String OPT_REQUIRE_METHOD_SPLITTING = "require_method_split";
	public static final String OPT_AVOID_TO_STRING_OF_PROXY_OBJ = "avoid_proxy_tostring";
	public static final String OPT_CODE_RANGE = "code_range";
	public static final String OPT_TRACE_RECORDER = "trace_recorder";
	public static final String OPT_RUN_ID = "run_id";
	
	private boolean precheck;
	private EntryPoint entryPoint;
	
	private String javaHome;
	private String launchClass;
	private int tcpPort = -1;
	private String dumpFile;
	private int variableLayer;
	/* format: java.lang.*;java.util.ArrayList;java.util.*\;java.util.Arrays*        */
	private String includesExpression;
	private String excludesExpression;
	private int stepLimit;
	private int expectedSteps;
	private Set<String> overlongMethods;
	private boolean requireMethodSplit;
	private boolean avoidProxyToString;
	private List<CodeRangeEntry> codeRanges;
	private String recorderName;
	private String runId;
	
	public AgentParams(CommandLine cmd) {
		super(cmd);
		precheck = cmd.getBoolean(OPT_PRECHECK, false);
		String entryPointStr = cmd.getString(OPT_ENTRY_POINT);
		if (entryPointStr != null) {
			int idx = entryPointStr.lastIndexOf(".");
			String mainClass = entryPointStr.substring(0, idx);
			String mainMethod = entryPointStr.substring(idx + 1);	
			EntryPoint entryPoint = new EntryPoint(mainClass, mainMethod);
			this.entryPoint = entryPoint;
		}
		
		setJavaHome(cmd.getString(OPT_JAVA_HOME));
		setWorkingDirectory(cmd.getString(OPT_WORKING_DIR));

		String launchClass = cmd.getString(OPT_LAUNCH_CLASS);
		if (launchClass == null && entryPoint != null) {
			launchClass = entryPoint.getClassName();
		}
		setLaunchClass(launchClass);
		
		tcpPort = cmd.getInt(OPT_TCP_PORT, -1);
		dumpFile = cmd.getString(OPT_DUMP_FILE);
		includesExpression = getFilterExpression(cmd, OPT_INCLUDES_FILE, OPT_INCLUDES);
		excludesExpression = getFilterExpression(cmd, OPT_EXCLUDES_FILE, OPT_EXCLUDES);
		variableLayer = cmd.getInt(OPT_VARIABLE_LAYER, 2);
		
		stepLimit = cmd.getInt(OPT_STEP_LIMIT, AgentConstants.UNSPECIFIED_INT_VALUE);
		expectedSteps = cmd.getInt(OPT_EXPECTED_STEP, AgentConstants.UNSPECIFIED_INT_VALUE);
		overlongMethods = cmd.getStringSet(OPT_OVER_LONG_METHODS);
		requireMethodSplit = cmd.getBoolean(OPT_REQUIRE_METHOD_SPLITTING, false);
		avoidProxyToString = cmd.getBoolean(OPT_AVOID_TO_STRING_OF_PROXY_OBJ, false);
		codeRanges = CodeRangeEntry.parse(cmd.getStringList(OPT_CODE_RANGE));
		recorderName = cmd.getString(OPT_TRACE_RECORDER);
		runId = cmd.getString(OPT_RUN_ID);
	}

	public static AgentParams initFrom(CommandLine cmd) {
		return new AgentParams(cmd);
	}

	private static String getFilterExpression(CommandLine cmd, String fileOpt, String opt) {
		String filePath = cmd.getString(fileOpt);
		String expression = null;
		Collection<?> vals = FileUtils.readLines(filePath);
		if (vals != null) {
			for (Iterator<?> it = vals.iterator(); it.hasNext();) {
				String line = (String) it.next();
				if (line.startsWith("#")) {
					it.remove();
				}
			}
		}
		
		if (!CollectionUtils.isEmpty(vals)) {
			expression = StringUtils.join(vals, AgentConstants.AGENT_PARAMS_MULTI_VALUE_SEPARATOR);
		}
		if (expression == null) {
			expression = cmd.getString(opt);
		}
		return expression;
	}
	
	public EntryPoint getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(EntryPoint entryPoint) {
		this.entryPoint = entryPoint;
	}

	public String getJavaHome() {
		return javaHome;
	}

	public void setJavaHome(String javaHome) {
		this.javaHome = javaHome;
	}

	public String getLaunchClass() {
		return launchClass;
	}

	public void setLaunchClass(String launchClass) {
		this.launchClass = launchClass;
	}
	
	public int getTcpPort() {
		return tcpPort;
	}

	public String getDumpFile() {
		return dumpFile;
	}

	public String getExcludesExpression() {
		return excludesExpression;
	}
	
	public String getIncludesExpression() {
		return includesExpression;
	}
	
	public int getVariableLayer() {
		return variableLayer;
	}
	
	public boolean isPrecheck() {
		return precheck;
	}
	
	public int getStepLimit() {
		return stepLimit;
	}

	public int getExpectedSteps() {
		return expectedSteps;
	}
	
	public Set<String> getOverlongMethods() {
		return overlongMethods;
	}
	
	public boolean isRequireMethodSplit() {
		return requireMethodSplit;
	}
	
	public boolean isAvoidProxyToString() {
		return avoidProxyToString;
	}
	public String getTraceRecorderName() {
		return recorderName;
	}
	
	public String getRunId() {
		return this.runId;
	}
	
	public AppJavaClassPath initAppClassPath() {
		return initAppClassPath(getLaunchClass(), getJavaHome(), getClassPaths(), getWorkingDirectory());
	}
	
	public static AppJavaClassPath initAppClassPath(String launchClass, String javaHome, List<String> classPaths, String workingDir) {
		AppJavaClassPath appPath = new AppJavaClassPath();
		appPath.setLaunchClass(launchClass);
		appPath.setJavaHome(javaHome);
		for(String cp: classPaths){
			appPath.addClasspath(cp);
		}
		appPath.setWorkingDirectory(workingDir);
//		appPath.setOptionalTestMethod(entryPoint.getMethodSignature());
		return appPath;
	}
	
	public static enum LogType {
		debug, info, printProgress, error;

		public static List<LogType> valuesOf(List<String> types) {
			List<LogType> result = new ArrayList<>(types.size());
			for (String type : types) {
				result.add(LogType.valueOf(type));
			}
			return result;
		}
	}

	public List<CodeRangeEntry> getCodeRanges() {
		return codeRanges;
	}
}
