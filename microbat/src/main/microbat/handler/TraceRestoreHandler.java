package microbat.handler;

import java.sql.SQLException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.model.trace.Trace;
import microbat.sql.MysqlTraceRetriever;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;
import sav.strategies.dto.AppJavaClassPath;

public class TraceRestoreHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
		if (Settings.isRunTest) {
			appClassPath.setOptionalTestClass(Settings.launchClass);
			appClassPath.setOptionalTestMethod(Settings.testMethod);
			appClassPath.setLaunchClass(TestCaseAnalyzer.TEST_RUNNER);
		}

		Job job = new Job("") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MysqlTraceRetriever retriever = new MysqlTraceRetriever();
				try {
					int traceID = retriever.getLatestTrace(Settings.projectName);
					final Trace trace = retriever.retrieveTrace(traceID);
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							TraceView traceView = MicroBatViews.getTraceView();
							traceView.setMainTrace(trace);
							traceView.updateData();
						}

					});
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
		return null;
	}
	
	
}
