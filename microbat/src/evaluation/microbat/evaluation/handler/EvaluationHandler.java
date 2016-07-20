package microbat.evaluation.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;

import microbat.evaluation.junit.TestCaseAnalyzer;

public class EvaluationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
//		ExcelHandler handler = new ExcelHandler();
//		handler.start2();
		
		Job job = new Job("Do evaluation") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				TestCaseAnalyzer parser = new TestCaseAnalyzer();
				try {
					parser.runEvaluation();
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		
		return null;
	}

	
//	private void archievedSimulation(){
//		SimulatedMicroBat simulator = new SimulatedMicroBat();
//		try {
//			simulator.startSimulation();
//		} catch (GenerateRootCauseException e) {
//			e.printStackTrace();
//		}
//	}
}
