package microbat.instrumentation.cfgcoverage.runtime.value;

/**
 * 
 * @author lyly
 * This class helps to synchronize with ExecValue in Ziyuan project.
 */
public class ExecutionValueHelper {

	public static String getArrayElementID(String varID, int idx) {
			return String.format("%s[%s]", varID, idx);
	}

	public static String getFieldId(String parentId, String fieldName) {
		return String.format("%s.%s", parentId, fieldName);
	}
	
}
