package microbat.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.probability.BP.BeliefPropagation;
import microbat.util.JavaUtil;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;
import microbat.probability.SPP.vectorization.vector.FunctionMismatchException;
import microbat.probability.SPP.vectorization.vector.FunctionVector;
import microbat.probability.SPP.vectorization.vector.NodeVector;
import microbat.probability.SPP.vectorization.vector.VariableVector;

import java.util.ArrayList;

public class TestHandler extends AbstractHandler {
	
	TraceView traceView = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		JavaUtil.sourceFile2CUMap.clear();
		Job job = new Job("Testing Tregression") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				setup();
				
		        Trace trace = traceView.getTrace();
		        
		        TraceNode node1 = trace.getTraceNode(7);
		        VarValue var1 = node1.getReadVariables().get(0);
		        
		        TraceNode node2 = trace.getTraceNode(10);
		        VarValue var2 = node2.getReadVariables().get(0);
		        
		        System.out.println(var1);
		        System.out.println(var2);
		        
		        System.out.println(var1 == var2);
		        
		        var1.setProbability(10.0);
		        
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

}
