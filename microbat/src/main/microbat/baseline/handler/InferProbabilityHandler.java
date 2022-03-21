package microbat.baseline.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import microbat.Activator;
import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.StepLimitException;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;
import microbat.preference.AnalysisScopePreference;
import microbat.util.JavaUtil;
import microbat.util.MicroBatUtil;
import sav.strategies.dto.AppJavaClassPath;

public class InferProbabilityHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
		
		Job job = new Job("Probabilistic Inference") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// TODO Auto-generated method stub
				String projectPath = Activator.getDefault().getPreferenceStore().getString("");
				System.out.println("Working on the project at " + projectPath);
				
				List<String> includedClassNames = AnalysisScopePreference.getIncludedLibList();
				List<String> excludedClassNames = AnalysisScopePreference.getExcludedLibList();
				InstrumentationExecutor executor = new InstrumentationExecutor(appClassPath,
						projectPath, includedClassNames, excludedClassNames);
				
				try {
					final RunningInfo result = executor.run();
					Trace trace = result.getMainTrace();
					trace.setAppJavaClassPath(appClassPath);
				} catch (StepLimitException e) {
					System.out.println("Step limit exceeded");
					e.printStackTrace();
				}
				return null;
			}
			
		};
		return null;
	}

}
