package microbat.handler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import microbat.model.trace.Trace;
import microbat.recommendation.advanceinspector.SootAnalyzer;
import microbat.util.TempVariableInfo;
import microbat.views.MicroBatViews;
import soot.Unit;
import soot.tagkit.LineNumberTag;

public class StaticAnalysisHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Preparing for Debugging ...") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
//				AdvancedDetailInspector inspector = new AdvancedDetailInspector();
//				try {
//					inspector.analysis();
//				} catch (ClassHierarchyException | InvalidClassFileException e) {
//					e.printStackTrace();
//				}
				
				SootAnalyzer analyzer = new SootAnalyzer();
				Map<String, List<Unit>> seeds = analyzer.
						analyzeSeeds(TempVariableInfo.variableOption, TempVariableInfo.cu, TempVariableInfo.line);
				
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						Trace trace = MicroBatViews.getTraceView().getTrace();
//						Map<String, List<Unit>> newSeeds = removeExecutedSeeds(trace, seeds);
						
						System.currentTimeMillis();
						
					}
				});
				
				
				
				
				return Status.OK_STATUS;
			}

			private Map<String, List<Unit>> removeExecutedSeeds(Trace trace, Map<String, List<Unit>> seeds) {
				Map<String, List<Integer>> locations = trace.getExecutedLocation();
				for(String className: seeds.keySet()){
					List<Integer> exeLines = locations.get(className);
					if(exeLines != null){
						List<Unit> seedLines = seeds.get(className);
						Iterator<Unit> iter = seedLines.iterator();
						while(iter.hasNext()){
							Unit unit = iter.next();
							LineNumberTag tag = (LineNumberTag)unit.getTag("LineNumberTag");
							if(tag != null){
								Integer seedLine = tag.getLineNumber();
								if(exeLines.contains(seedLine)){
									iter.remove();
								}
							}
						}
					}
				}
				
				return seeds;
			}
		};
		
		job.schedule();
		
		return null;
	}

}
