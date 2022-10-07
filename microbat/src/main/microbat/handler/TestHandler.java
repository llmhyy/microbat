package microbat.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;


import microbat.baseline.encoders.ProbabilityEncoder;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;


public class TestHandler extends AbstractHandler {
	
	TraceView traceView = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		MutationFramework mutationFramework = new MutationFramework();
//		TraceDiff traceDiff = new TraceDiff();
//		
//		mutationFramework.setProjectPath("C:/Users/arkwa/git/java-mutation-framework/sample/math_70");
//		mutationFramework.setDropInsDir("C:/Users/arkwa/git/java-mutation-framework/lib");
//
//		MutationResult result = mutationFramework.startMutationFramework();
		
//		List<MutationResult> mutationResults = mutationFramework.startMutationFramework();
//		for (MutationResult mutationResult : mutationResults) {
//			Project mutatedProject = mutationResult.getMutatedProject();
//			Project originalProject = mutationResult.getOriginalProject();
//			
//			System.out.println("mutatedProject: " + mutatedProject.getSrcPath());
//			System.out.println("originalProject: " + originalProject.getSrcPath());
//			
//			PairList pairList = TraceDiff.getTraceAlignment("src/main/java", "src/main/test",
//                    mutatedProject.getRoot().getAbsolutePath(), originalProject.getRoot().getAbsolutePath(),
//                    mutationResult.getMutatedTrace(), mutationResult.getOriginalTrace());
//			
//			List<TraceNodePair> pairL = pairList.getPairList();
//			for (TraceNodePair pair : pairL) {
//				System.out.println("-------");
//            	TraceNode buggyNode = pair.getBeforeNode();
//            	System.out.println("buggyNode line number: " + buggyNode.getLineNumber());
//            	System.out.println(pair.getBeforeNode().getCodeStatement());
//            	System.out.println("correctNode line number: " + pair.getAfterNode().getLineNumber());
//            	System.out.println(pair.getAfterNode().getCodeStatement());
//            	System.out.println("file:" + pair.getBeforeNode().getBreakPoint().getFullJavaFilePath());
//            	System.out.println("file:" + pair.getAfterNode().getBreakPoint().getFullJavaFilePath());
//			}
//		}
		
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
