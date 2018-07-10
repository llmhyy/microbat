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
	
	private ClassLocation targetMethodLoc;
	private List<String> classPaths;
	private int cdgLayer;

	public static CoverageAgentParams initFrom(CommandLine cmd) {
		CoverageAgentParams params = new CoverageAgentParams();
		params.targetMethodLoc = InstrumentationUtils.getMethodId(cmd.getString(OPT_TARGET_METHOD));
		params.classPaths = cmd.getStringList(OPT_CLASS_PATH);
		params.cdgLayer = cmd.getInt(OPT_CDG_LAYER, 1);
		return params;
	}
	
	public ClassLocation getTargetMethod() {
		return targetMethodLoc;
	}
	
	public AppJavaClassPath initAppClasspath() {
		return AgentParams.initAppClassPath(null, null, classPaths, null);
	}
	
	public int getCdgLayer() {
		return cdgLayer;
	}
}
