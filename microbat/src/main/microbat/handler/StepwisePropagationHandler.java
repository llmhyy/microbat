package microbat.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import microbat.model.value.VarValue;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class StepwisePropagationHandler extends AbstractHandler {

	TraceView traceView = null;
	
	private static List<VarValue> inputs = new ArrayList<>();
	private static List<VarValue> outputs = new ArrayList<>();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				setup();
				
				System.out.println();
				System.out.println("---------------------------------------------");
				System.out.println("\t Stepwise Probability Propagation");
				System.out.println();
				
				// Check is the trace ready
				if (traceView.getTrace() == null) {
					System.out.println("Please setup the trace before propagation");
					return Status.OK_STATUS;
				}
				
				// Check is the IO ready
				if (!StepwisePropagationHandler.isIOReady()) {
					System.out.println("Please provide the inputs and the outputs");
					return Status.OK_STATUS;
				}
				
				
				System.out.println("Propagation Start");
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
		return null;
	}
	
	private void setup() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				traceView = MicroBatViews.getTraceView();
			}
		});
	}
	
	public static boolean isIOReady() {
		return !StepwisePropagationHandler.inputs.isEmpty() && !StepwisePropagationHandler.outputs.isEmpty();
	}
	
	public static void clearData() {
		StepwisePropagationHandler.inputs = null;
		StepwisePropagationHandler.outputs = null;
	}
	
	public static void addInputs(List<VarValue> inputs) {
		StepwisePropagationHandler.inputs.addAll(inputs);
		for (VarValue input : StepwisePropagationHandler.inputs) {
			System.out.println("StepwisePropagationHandler: Selected Inputs: " + input.getVarID());
		}
	}
	
	public static void printIO() {
		for (VarValue input : StepwisePropagationHandler.inputs) {
			System.out.println("StepwisePropagationHandler: Selected Inputs: " + input.getVarID());
		}
		for (VarValue output : StepwisePropagationHandler.outputs) {
			System.out.println("StepwisePropagationHandler: Selected Outputs: " + output.getVarID());
		}
	}
	
	public static void addOutpus(List<VarValue> outputs) {
		StepwisePropagationHandler.outputs.addAll(outputs);
		for (VarValue output : StepwisePropagationHandler.outputs) {
			System.out.println("StepwisePropagationHandler: Selected Outputs: " + output.getVarID());
		}
	}

}
