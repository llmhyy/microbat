package microbat.codeanalysis.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to locate all the executor classes in this project
 * @author "linyun"
 *
 */
public abstract class Executor {
	
	protected int steps = 0;
	
	public static final int TIME_OUT = 10000;
	
	protected String[] stepWatchExcludes = { "java.*", "java.lang.*", "javax.*", "sun.*", "com.sun.*", 
			"org.junit.*", "junit.*", "junit.framework.*", "org.hamcrest.*", "org.hamcrest.core.*", "org.hamcrest.internal.*",
			"jdk.*", "jdk.internal.*", "org.GNOME.Accessibility.*"};
	
	public void addExcludeList(List<String> excludeList) {
		List<String> existingList = new ArrayList<>();
		for (int i = 0; i < stepWatchExcludes.length; i++) {
			existingList.add(stepWatchExcludes[i]);
		}
		
		for (Iterator<String> iterator = excludeList.iterator(); iterator.hasNext();) {
			String excludeString = (String) iterator.next();
			if (!existingList.contains(excludeString)) {
				existingList.add(excludeString);
			}
		}
		
		stepWatchExcludes = existingList.toArray(new String[0]);
	}
}
