package microbat.test.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import microbat.trace.TraceTest;

public class RunTestHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Result result = JUnitCore.runClasses(TraceTest.class);
		for (Failure failure : result.getFailures()){
            System.out.println(failure.toString());
        }
		
		return null;
	}


}
