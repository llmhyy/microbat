package microbat.instrumentation.trace.data;

import java.util.Arrays;
import java.util.List;

public class FilterCheckerMock implements IFilterChecker {
	private List<String> includes = Arrays.asList(
			"microbat/instrumentation/trace/testdata",
			"java/util/Random",
//			"java/util/List",
//			"java/util/ArrayList",
			"org/apache/commons/lang/ArrayUtils",
//			"java/util/Date",
			"java/util/Arrays"
//			"java/lang/String"
			);
	
	@Override
	public void startup() {
	}

	@Override
	public boolean checkTransformable(String className) {
		for (String include : includes) {
			if (className.startsWith(include)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean checkExclusive(String className, String methodName) {
		return !className.startsWith("microbat/instrumentation/trace/testdata");
	}

}
