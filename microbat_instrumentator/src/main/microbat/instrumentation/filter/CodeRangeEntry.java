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
		// FIXME Xuezhi
	}

	public static List<CodeRangeEntry> parse(List<String> codeRanges) {
		List<CodeRangeEntry> entries = new ArrayList<>(codeRanges.size());
		for (String codeRange : codeRanges) {
			CodeRangeEntry entry = new CodeRangeEntry();
			int sIdx = codeRange.lastIndexOf(".");
			entry.endLine = Integer.parseInt(codeRange.substring(sIdx));
			int eIdx = sIdx;
			sIdx = codeRange.substring(0, eIdx).lastIndexOf(".");
			entry.startLine = Integer.parseInt(codeRange.substring(sIdx, eIdx));
			eIdx = sIdx;
			sIdx = codeRange.substring(0, eIdx).lastIndexOf(".");
			String methodWithSign = codeRange.substring(sIdx, eIdx);
			entry.methodName = SignatureUtils.extractMethodName(methodWithSign);
			int endNameIdx = methodWithSign.indexOf("(");
			if (endNameIdx > 1) {
				entry.methodSig = methodWithSign.substring(endNameIdx);
			}
			entry.className = codeRange.substring(0, sIdx);
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
