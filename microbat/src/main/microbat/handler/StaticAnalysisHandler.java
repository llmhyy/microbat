package microbat.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import microbat.recommendation.advanceinspector.SootAnalyzer;
import microbat.util.TempVariableInfo;
import soot.Unit;

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
				
				
				
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		
		return null;
	}

}
