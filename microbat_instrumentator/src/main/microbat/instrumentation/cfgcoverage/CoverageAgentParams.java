package microbat.instrumentation.cfgcoverage;

import java.util.List;

import microbat.instrumentation.AgentParams;
import microbat.instrumentation.CommandLine;
import microbat.model.ClassLocation;
import sav.strategies.dto.AppJavaClassPath;

public class CoverageAgentParams {
	public static final String OPT_IS_COUNT_COVERAGE = "countCoverage";
	/* format of targetmethod (CoverageAgentUtil.getMethodId()): className.methodName.startLine */
	public static final String OPT_TARGET_METHOD = "targetMethod";
	public static final String OPT_CLASS_PATH = "class_path";
	public static final String OPT_CDG_LAYER = "cdg_layer";
	public static final String OPT_DUMP_FILE = "dumpe_file_path";
	
	private ClassLocation targetMethodLoc;
	private List<String> classPaths;
	private int cdgLayer;
	private String dumpFile;

	public static CoverageAgentParams initFrom(CommandLine cmd) {
		CoverageAgentParams params = new CoverageAgentParams();
		params.targetMethodLoc = InstrumentationUtils.fromTargetMethodId(cmd.getString(OPT_TARGET_METHOD));
		params.classPaths = cmd.getStringList(OPT_CLASS_PATH);
		params.cdgLayer = cmd.getInt(OPT_CDG_LAYER, 1);
		params.dumpFile = cmd.getString(OPT_DUMP_FILE);
		return params;
	}
	
	public ClassLocation getTargetMethod() {
		return targetMethodLoc;
	}
	
	public String getTargetMethodParam() {
		return InstrumentationUtils.toTargetMethodId(targetMethodLoc);
	}
	
	public AppJavaClassPath initAppClasspath() {
		return AgentParams.initAppClassPath(null, null, classPaths, null);
	}
	
	public int getCdgLayer() {
		return cdgLayer;
	}
	
	public String getDumpFile() {
		return dumpFile;
	}

	public ClassLocation getTargetMethodLoc() {
		return targetMethodLoc;
	}

	public void setTargetMethodLoc(ClassLocation targetMethodLoc) {
		this.targetMethodLoc = targetMethodLoc;
	}

	public List<String> getClassPaths() {
		return classPaths;
	}

	public void setClassPaths(List<String> classPaths) {
		this.classPaths = classPaths;
	}

	public void setCdgLayer(int cdgLayer) {
		this.cdgLayer = cdgLayer;
	}

	public void setDumpFile(String dumpFile) {
		this.dumpFile = dumpFile;
	}
	
}
