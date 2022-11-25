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

import microbat.baseline.probpropagation.BeliefPropagation;
import microbat.bytecode.ByteCode;
import microbat.bytecode.ByteCodeList;
import microbat.bytecode.OpcodeType;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.util.JavaUtil;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;


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
				for(TraceNode node : trace.getExecutionList()) {
					System.out.println("-------------------------------");
					System.out.println("Trace Node: " + node.getOrder());
					
					if (isForEachLoop(node)) {
						System.out.println("This node is for each loop");
					}
					ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
					System.out.println(byteCodeList.toOpCodeString());
					for(ByteCode byteCode : byteCodeList) {
						System.out.println(byteCode);
					}
				}
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
		return null;
	}
	
	private boolean isForEachLoop(TraceNode node) {
		ByteCodeList byteCodeList = new ByteCodeList(node.getBytecode());
		return this.isCollectionForEachLoop(byteCodeList) || this.isArrayListForEachLoop(byteCodeList);
	}
	
	private boolean isCollectionForEachLoop(ByteCodeList byteCodeList) {
		if (byteCodeList.size() != 10) {
			return false;
		}
		int[] opCodeList = {-1,185,-1,167,-1,185,-1,-1,185,154};
		for (int i=0; i<10; i++) {
			ByteCode byteCode = byteCodeList.getByteCode(i);
			int targetOpcode = opCodeList[i];
			if (targetOpcode == -1) {
				continue;
			}
			if (byteCode.getOpcode() != targetOpcode) {
				return false;
			}
		}
		
		ByteCode byteCode_0 = byteCodeList.getByteCode(0);
		if(byteCode_0.getOpcodeType() != OpcodeType.LOAD_VARIABLE) {
			return false;
		}
		
		ByteCode byteCode_2 = byteCodeList.getByteCode(2);
		if (byteCode_2.getOpcodeType() != OpcodeType.STORE_VARIABLE) {
			return false;
		}
		
		ByteCode byteCode_4 = byteCodeList.getByteCode(4);
		if (byteCode_4.getOpcodeType() != OpcodeType.LOAD_VARIABLE) {
			return false;
		}
		
		ByteCode byteCode_6 = byteCodeList.getByteCode(6);
		if (byteCode_6.getOpcodeType() != OpcodeType.STORE_VARIABLE) {
			return false;
		}
		
		ByteCode byteCode_7 = byteCodeList.getByteCode(7);
		if (byteCode_7.getOpcodeType() != OpcodeType.LOAD_VARIABLE) {
			return false;
		}
		
		return true;
	}
	
	private boolean isArrayListForEachLoop(ByteCodeList byteCodeList) {
		if (byteCodeList.size() != 16) {
			return false;
		}
		
		int[] opCodeList = {-1,89,58,190,54,3,-1,167,25,-1,-1,-1,132,-1,21,161};
		for (int i=0; i<16; i++) {
			ByteCode byteCode = byteCodeList.getByteCode(i);
			int targetOpcode = opCodeList[i];
			if (targetOpcode == -1) {
				continue;
			}
			if (byteCode.getOpcode() != targetOpcode) {
				return false;
			}
		}
		
		ByteCode byteCode_1 = byteCodeList.getByteCode(0);
		if(byteCode_1.getOpcodeType() != OpcodeType.LOAD_VARIABLE) {
			return false;
		}
		
		ByteCode byteCode_6 = byteCodeList.getByteCode(6);
		if (byteCode_6.getOpcodeType() != OpcodeType.STORE_VARIABLE) {
			return false;
		}
		
		ByteCode byteCode_9 = byteCodeList.getByteCode(9);
		if (byteCode_9.getOpcodeType() != OpcodeType.LOAD_VARIABLE) {
			return false;
		}
		
		ByteCode byteCode_10 = byteCodeList.getByteCode(10);
		if (byteCode_10.getOpcodeType() != OpcodeType.LOAD_FROM_ARRAY) {
			return false;
		}
		
		ByteCode byteCode_11 = byteCodeList.getByteCode(11);
		if (byteCode_11.getOpcodeType() != OpcodeType.STORE_VARIABLE) {
			return false;
		}
		
		ByteCode byteCode_13 = byteCodeList.getByteCode(13);
		if (byteCode_13.getOpcodeType() != OpcodeType.LOAD_VARIABLE) {
			return false;
		}
		
		return true;
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
