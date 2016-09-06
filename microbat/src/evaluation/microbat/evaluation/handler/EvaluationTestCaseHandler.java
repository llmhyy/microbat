package microbat.evaluation.handler;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;

import microbat.evaluation.junit.TestCaseEvaluator;

public class EvaluationTestCaseHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Do evaluation") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				TestCaseEvaluator parser = new TestCaseEvaluator();
				try {
					parser.runEvaluation(TestCaseEvaluator.TEST_CASE);
				} catch (JavaModelException | IOException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		
		return null;
	}

}
