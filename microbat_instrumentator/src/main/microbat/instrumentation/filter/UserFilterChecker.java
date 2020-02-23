/**
 * 
 */
package microbat.instrumentation.filter;

/**
 * @author lyly
 *
 */
public class UserFilterChecker {
	public static IUserFilter userFilter = new EmptyUserFilter();	

	public static boolean isInstrumentable(String classFName) {
		return userFilter.isInstrumentable(classFName.replace("/", "."));
	}
	
	public static interface IUserFilter {

		boolean isInstrumentable(String className);
		
	}
	
	private static class EmptyUserFilter implements IUserFilter {

		@Override
		public boolean isInstrumentable(String className) {
			return true;
		}
		 
	}

}
