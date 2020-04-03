package microbat.instrumentation.filter;

import java.util.ArrayList;
import java.util.List;

import sav.common.core.utils.StringUtils;

/**
 * 
 * @author linyun
 * 
 */
public class CodeRangeEntry {
	private String className;
	private int startLine;
	private int endLine;
	
	public CodeRangeEntry(String className, int startLine, int endLine) {
		this.className = className;
		this.startLine = startLine;
		this.endLine = endLine;
	}
	
	private CodeRangeEntry() {
		
	}
	
	public static List<CodeRangeEntry> parse(List<String> codeRanges) {
		List<CodeRangeEntry> entries = new ArrayList<>(codeRanges.size());
		for (String codeRange : codeRanges) {
			CodeRangeEntry entry = new CodeRangeEntry();
			//e.g. s.12.9
			int sIdx = codeRange.lastIndexOf(".");
			entry.endLine = Integer.parseInt(codeRange.substring(sIdx+1));// get '9'
			
			int eIdx = sIdx;
			sIdx = codeRange.substring(0, eIdx).lastIndexOf(".");//last'.' index in "s.12"
			entry.startLine = Integer.parseInt(codeRange.substring(sIdx+1, eIdx));//get '12'
			entry.className = codeRange.substring(0, sIdx);//get "s"
			entries.add(entry);
		}

		return entries;
	}
	
	@Override
	public String toString() {
		return StringUtils.dotJoin(className, startLine, endLine);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
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
