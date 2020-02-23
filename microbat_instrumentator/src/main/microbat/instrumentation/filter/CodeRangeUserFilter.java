/**
 * 
 */
package microbat.instrumentation.filter;

import microbat.instrumentation.filter.UserFilterChecker.IUserFilter;

/**
 * @author lyly
 *
 */
public class CodeRangeUserFilter implements IUserFilter {

	@Override
	public boolean isInstrumentable(String className) {
		// FIXME Xuezhi LinYun
		return false;
	}

}
