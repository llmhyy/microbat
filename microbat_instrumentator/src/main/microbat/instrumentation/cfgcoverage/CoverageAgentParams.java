package microbat.instrumentation.cfgcoverage;

import java.util.List;

import microbat.instrumentation.AgentParams;
import microbat.instrumentation.CommandLine;
import microbat.instrumentation.CommonParams;
import microbat.model.ClassLocation;
import sav.strategies.dto.AppJavaClassPath;

public class CoverageAgentParams extends CommonParams {
	public static final String OPT_CLASS_PATH = CommonParams.OPT_CLASS_PATH;
	public static final String OPT_LOG_TYPE = CommonParams.OPT_LOG;
	public static final String OPT_WORKING_DIR = CommonParams.OPT_WORKING_DIR;
	public static final String OPT_IS_COUNT_COVERAGE = "countCoverage";
	public static final String OPT_COVERAGE_COLLECTION_TYPE = "coverageType";
	/* format of targetmethod (CoverageAgentUtil.getMethodId()): className.methodName.startLine */
	public static final String OPT_TARGET_METHOD = "targetMethod";
	public static final String OPT_CDG_LAYER = "cdg_layer";
	public static final String OPT_DUMP_FILE = "dumpe_file";
	public static final String OPT_INCLUSIVE_METHOD_IDS = "inclusive_method_ids";
	public static final String OPT_VARIABLE_LAYER = "varLayer";
	public static final String OPT_COLLECT_CONDITION_VARIATION = "collect_cond_variation";
	
	private ClassLocation targetMethodLoc;
	private List<String> classPaths;
	private int cdgLayer;
	private String dumpFile;
	private List<String> inclusiveMethodIds;
	private int varLayer;
	private boolean collectConditionVariation;
	private CoverageCollectionType coverageType;
	
	public CoverageAgentParams() {
		
	}
	
	public CoverageAgentParams(CommandLine cmd) {
		super(cmd);
		targetMethodLoc = InstrumentationUtils.getClassLocation(cmd.getString(OPT_TARGET_METHOD));
		classPaths = cmd.getStringList(OPT_CLASS_PATH);
		cdgLayer = cmd.getInt(OPT_CDG_LAYER, 1);
		dumpFile = cmd.getString(OPT_DUMP_FILE);
		inclusiveMethodIds = cmd.getStringList(OPT_INCLUSIVE_METHOD_IDS);
		varLayer = cmd.getInt(OPT_VARIABLE_LAYER, 2);
		collectConditionVariation = cmd.getBoolean(OPT_COLLECT_CONDITION_VARIATION, false);
		coverageType = CoverageCollectionType.valueOf(cmd.getString(OPT_COVERAGE_COLLECTION_TYPE),
				CoverageCollectionType.BRANCH_COVERAGE);
	}
	
	public ClassLocation getTargetMethod() {
		return targetMethodLoc;
	}
	
	public String getTargetMethodParam() {
		return InstrumentationUtils.toTargetMethodId(targetMethodLoc);
	}
	
	public AppJavaClassPath initAppClasspath() {
		return AgentParams.initAppClassPath(null, null, classPaths, getWorkingDirectory());
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
	
	public List<String> getInclusiveMethodIds() {
		return inclusiveMethodIds;
	}
	
	public void setInclusiveMethodIds(List<String> inclusiveMethodIds) {
		this.inclusiveMethodIds = inclusiveMethodIds;
	}

	public int getVarLayer() {
		return varLayer;
	}

	public void setVarLayer(int varLayer) {
		this.varLayer = varLayer;
	}
	
	public boolean collectConditionVariation() {
		return collectConditionVariation;
	}
	
	public void setCollectConditionVariation(boolean collectConditionVariation) {
		this.collectConditionVariation = collectConditionVariation;
	}
	
	public CoverageCollectionType getCoverageType() {
		return coverageType;
	}

	public void setCoverageType(CoverageCollectionType coverageType) {
		this.coverageType = coverageType;
	}

	/**
	 * There are two modes to run code coverage.
	 * BRANCH_COVERAGE: 
	 * UNCIRCLE_CFG_COVERAGE: 
	 * */
	public static enum CoverageCollectionType {
		BRANCH_COVERAGE,
		UNCIRCLE_CFG_COVERAGE;

		public static CoverageCollectionType valueOf(String strValue, CoverageCollectionType defaultValue) {
			if (strValue == null || strValue.isEmpty()) {
				return defaultValue;
			}
			return valueOf(strValue);
		}
	}
}
