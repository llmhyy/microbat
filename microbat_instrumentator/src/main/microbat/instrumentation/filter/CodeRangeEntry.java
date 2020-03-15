package microbat.instrumentation.filter;

import java.util.ArrayList;
import java.util.List;

import sav.common.core.utils.SignatureUtils;
import sav.common.core.utils.StringUtils;

/**
 * 
 * @author linyun
 *
 */
public class CodeRangeEntry {
	private String className;
	private String methodName;
	private String methodSig;
	private int startLine;
	private int endLine;

	public CodeRangeEntry() {
		// FIXME Xuezhi [1]
	}

	public CodeRangeEntry(String className, String methodName, int startLine, int endLine) {
		this.className = className;
		this.methodName = methodName;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public static List<CodeRangeEntry> parse(List<String> codeRanges) {
		List<CodeRangeEntry> entries = new ArrayList<>(codeRanges.size());
		for (String codeRange : codeRanges) {
			CodeRangeEntry entry = new CodeRangeEntry();
			// e.g. dhu.s.xz().12.9
			int sIdx = codeRange.lastIndexOf(".");
			entry.endLine = Integer.parseInt(codeRange.substring(sIdx + 1));// get '9'

			int eIdx = sIdx;
			sIdx = codeRange.substring(0, eIdx).lastIndexOf(".");// last'.' index in "dhu.s.xz().12"
			entry.startLine = Integer.parseInt(codeRange.substring(sIdx + 1, eIdx));// get '12'
			eIdx = sIdx;
			sIdx = codeRange.substring(0, eIdx).lastIndexOf(".");// last '.' index in "dhu.s.xz()"
			/**
			 * TODO SXZ complete Method name in Preference ,or there no need method name at all
			 * In fact there is no method name that need user fill in Preference So there is
			 * always null, thus I stop the line as follows related to method name Also
			 * there is probably a issue in
			 * "SignatureUtils.extractMethodName(methodWithSign)",which input 'null'and
			 * output "nullnull"
			 **/
//			String methodWithSign = codeRange.substring(sIdx + 1, eIdx);// get "xz()"
//			entry.methodName = SignatureUtils.extractMethodName(methodWithSign);
//			int endNameIdx = methodWithSign.indexOf("(");
//			if (endNameIdx > 1) {
//				entry.methodSig = methodWithSign.substring(endNameIdx);// get '()'
//			}
			entry.className = codeRange.substring(0, sIdx);// get "s"
			entries.add(entry);
		}

		return entries;
	}

	@Override
	public String toString() {
		return StringUtils.dotJoin(className, methodName + methodSig, startLine, endLine);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodSig() {
		return methodSig;
	}

	public void setMethodSig(String methodSig) {
		this.methodSig = methodSig;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

}
