package microbat.handler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import microbat.Activator;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.mutation.MutationAgent;
import microbat.preference.MicrobatPreference;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;
import sav.strategies.dto.AppJavaClassPath;

public class MutationHandler extends AbstractHandler{

	TraceView traceView = null;
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		Job job = new Job("Run Baseline") {

			@Override
			protected IStatus run(IProgressMonitor arg0) {
				
				// Access the trace view
				setup();
				
				// Check is all parameter avaiable
				if (!isReady()) {
					throw new RuntimeException("Mutation Handler is not ready");
				}

				// Access mutation setting
				boolean useTestCaseID = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.USE_TEST_CASE_ID).equals("true");
				String projectPath = "";
				if (useTestCaseID) {
					projectPath = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.PROJECT_PATH);
				} else {
					projectPath = MicroBatUtil.getProjectPath(Settings.projectName);
				}
				
//				final String projectPath = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.PROJECT_PATH);
				final String dropInDir = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.DROP_IN_FOLDER_MICROBAT);
				final String microbatConfigPath = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.CONFIG_PATH_MICROBAT);

				// Perform mutation
				MutationAgent mutationAgent = new MutationAgent(projectPath, dropInDir, microbatConfigPath);
				if (useTestCaseID) {
					final String testCaseID_str = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.TEST_CASE_ID_MICROBAT);
					final int testCaseID = Integer.parseInt(testCaseID_str);
					mutationAgent.setTestCaseID(testCaseID);
				} else {
					mutationAgent.setTestCaseInfo(Settings.launchClass, Settings.testMethod);
				}
				mutationAgent.startMutation();
				
				updateView(mutationAgent.getBuggyTrace());
				
				String rootCauseIDStr = "";
				List<TraceNode> rootCauses = mutationAgent.getRootCause();
				Collections.sort(rootCauses, new Comparator<TraceNode>() {
					@Override
					public int compare(TraceNode node1, TraceNode node2) {
						return node1.getOrder() - node2.getOrder();
					}
				});
				for (TraceNode rootCause : rootCauses) {
					rootCauseIDStr += rootCause.getOrder() + ",";
				}
				System.out.println("Root Causes Node ID: " + rootCauseIDStr);
				
				BaselineHandler.setTestCaseMethod(mutationAgent.getTestCase().simpleName);
				
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
	
	private boolean isReady() {
		return this.traceView != null;
	}
	
	private void updateView(Trace trace) {
		Display.getDefault().asyncExec(new Runnable(){
			
			@Override
			public void run() {

				traceView.setMainTrace(trace);
				traceView.updateData();
			}
			
		});
	}
}
