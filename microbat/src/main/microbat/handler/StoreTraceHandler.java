package microbat.handler;

import java.sql.SQLException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import microbat.model.trace.Trace;
import microbat.sql.TraceRecorder;
import microbat.util.MessageDialogs;
import microbat.views.MicroBatViews;
import sav.common.core.utils.StringUtils;

public class StoreTraceHandler extends AbstractHandler {
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Trace trace = MicroBatViews.getTraceView().getTrace();
		try {
			TraceRecorder recorder = new TraceRecorder();
			recorder.storeTrace(trace);
		} catch (SQLException e) {
			MessageDialogs.showErrorInUI(StringUtils.spaceJoin("Cannot store trace: ", e.getMessage()));
		} 
		System.out.println("test");
		return null;
	}

	
}
