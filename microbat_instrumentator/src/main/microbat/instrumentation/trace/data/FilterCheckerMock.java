package microbat.instrumentation.trace.data;

import java.util.Arrays;
import java.util.List;

public class FilterCheckerMock implements IFilterChecker {
	private List<String> transformables = Arrays.asList(
			"com0",
			"microbat/instrumentation/trace/testdata",
			"java/util/Random",
//			"java/util/List",
			"java/util/ArrayList",
			"org/apache/commons/lang/ArrayUtils",
//			"java/util/Date",
			"java/util/Arrays"
//			"java/lang/String"
			);
	
	@Override
	public void startup() {
	}

	@Override
	public boolean checkTransformable(String classFName) {
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

}
