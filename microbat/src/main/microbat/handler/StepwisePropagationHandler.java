package microbat.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import microbat.baseline.probpropagation.StepwisePropagator;
import microbat.model.value.VarValue;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;

public class StepwisePropagationHandler extends AbstractHandler implements RequireIO {

	TraceView traceView = null;
	
	private List<VarValue> inputs = new ArrayList<>();
	private List<VarValue> outputs = new ArrayList<>();
	
	private static boolean registerFlag = false;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				if (!registerFlag) {
					registerHandler();
					return Status.OK_STATUS;
				}
				
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
				if (!isIOReady()) {
					System.out.println("Please provide the inputs and the outputs");
					return Status.OK_STATUS;
				}
				
				StepwisePropagator propagator = new StepwisePropagator(traceView.getTrace(), inputs, outputs);

				System.out.println("Propagation Start");
				propagator.propagate();
				
				System.out.println("Propagation End");
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
	
	public boolean isIOReady() {
		return !this.inputs.isEmpty() && !this.outputs.isEmpty();
	}

	@Override
	public void registerHandler() {
		DebugFeedbackView.registerHandler(this);
		StepwisePropagationHandler.registerFlag = true;
		
		System.out.println();
		System.out.println("StepwisePropagationHandler is now registered to buttons");
		System.out.println("Please select inputs and outputs");
	}

	@Override
	public void addInputs(Collection<VarValue> inputs) {
		this.inputs.addAll(inputs);
		for (VarValue input : this.inputs) {
			System.out.println("StepwisePropagationHandler: Selected Inputs: " + input.getVarID());
		}
	}

	@Override
	public void addOutputs(Collection<VarValue> outputs) {
		this.outputs.addAll(outputs);
		for (VarValue output : this.outputs) {
			System.out.println("StepwisePropagationHandler: Selected Outputs: " + output.getVarID());
		}
	}

	@Override
	public void printIO() {
		for (VarValue input : this.inputs) {
			System.out.println("StepwisePropagationHandler: Selected Inputs: " + input.getVarID());
		}
		for (VarValue output : this.outputs) {
			System.out.println("StepwisePropagationHandler: Selected Outputs: " + output.getVarID());
		}
	}

	@Override
	public void clearData() {
		this.inputs = null;
		this.outputs = null;
	}

}
