package microbat.instrumentation.filter;

import sav.strategies.dto.AppJavaClassPath;

public interface IFilterChecker {

	boolean checkExclusive(String classFName, String methodName);
	
	boolean checkAppClass(String classFName);

	boolean checkTransformable(String classFName, String path, boolean isBootstrap);

	void startup(AppJavaClassPath appClasspath, String includeExpression, String excludeExpression);

}
