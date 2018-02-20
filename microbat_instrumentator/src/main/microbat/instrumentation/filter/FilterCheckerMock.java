package microbat.instrumentation.filter;

import java.util.Arrays;
import java.util.List;

import sav.strategies.dto.AppJavaClassPath;

public class FilterCheckerMock implements IFilterChecker {
	private List<String> transformables = Arrays.asList(
			"com0",
			"microbat/instrumentation/trace/testdata"
//			"java/util/Random",
//			"java/util/List",
//			"java/util/ArrayList"
//			"org/apache/commons/lang/ArrayUtils",
//			"java/util/Date",
//			"java/util/Arrays"
//			"java/lang/String"
			);
	
	@Override
	public void startup(AppJavaClassPath appClasspath, String includeExpression, String excludeExpression) {
	}

	@Override
	public boolean checkTransformable(String classFName, String path, boolean isBootstrap) {
		for (String include : transformables) {
			if (classFName.startsWith(include)) {
				return true;
			}
		}
		return false;
	}

	private List<String> includes = Arrays.asList("java.util.ArrayList", 
			"com0", "microbat.instrumentation.trace.testdata");
	@Override
	public boolean checkExclusive(String className, String methodName) {
		for (String include : includes) {
			if (className.startsWith(include)) {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean checkAppClass(String classFName) {
		return false;
	}

}
