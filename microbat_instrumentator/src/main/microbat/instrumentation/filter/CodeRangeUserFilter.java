/**
 * 
 */
package microbat.instrumentation.filter;

import java.util.List;

import microbat.instrumentation.filter.UserFilterChecker.IUserFilter;

/**
 * @author lyly
 *
 */
public class CodeRangeUserFilter implements IUserFilter {

	private List<CodeRangeEntry> codeRanges;
	
	public CodeRangeUserFilter(List<CodeRangeEntry> codeRanges) {
		super();
		this.setCodeRanges(codeRanges);
	}


	@Override
	public boolean isInstrumentable(String className) {
		// FIXME Xuezhi
		return false;
	}


	public List<CodeRangeEntry> getCodeRanges() {
		return codeRanges;
	}


	public void setCodeRanges(List<CodeRangeEntry> codeRanges) {
		this.codeRanges = codeRanges;
	}

}
