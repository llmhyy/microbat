package microbat.instrumentation.trace.data;

import sav.strategies.dto.AppJavaClassPath;

public interface IFilterChecker {

	void startup(AppJavaClassPath appClasspath);

	boolean checkExclusive(String classFName, String methodName);
	
	boolean checkAppClass(String classFName);

	boolean checkTransformable(String classFName, String path, boolean isBootstrap);

}
