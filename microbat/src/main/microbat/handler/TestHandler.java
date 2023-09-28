package microbat.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import microbat.Activator;
import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.debugpilot.propagation.BP.BeliefPropagation;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.preference.DebugPilotPreference;
import microbat.util.JavaUtil;
import microbat.util.TraceUtil;
import microbat.vectorization.TraceVectorizer;
import microbat.vectorization.vector.*;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;
import microbat.model.BreakPoint;

import java.util.ArrayList;
import java.util.Comparator;
public class TestHandler extends AbstractHandler {
	
	private final List<OpcodeType> unmodifiedType = new ArrayList<>();
	TraceView traceView = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		JavaUtil.sourceFile2CUMap.clear();
		Job job = new Job("Testing Tregression") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				setup();
				
				execute();
				
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		return null;
	}
	
	private void execute() {
		Trace trace = this.traceView.getTrace();
		TraceNode node1 = trace.getTraceNode(537);
		TraceNode node2 = trace.getTraceNode(546);
		
		System.out.println("Node: " + node1.getOrder());
		System.out.println(node1.getBreakPoint().getFullJavaFilePath());
		System.out.println(node1.getBreakPoint().getLineNumber());
		
		System.out.println("Node: " + node2.getOrder());
		System.out.println(node2.getBreakPoint().getFullJavaFilePath());
		System.out.println(node2.getBreakPoint().getLineNumber());
		
		System.out.println(node1.getBreakPoint().equals(node2.getBreakPoint()));
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
