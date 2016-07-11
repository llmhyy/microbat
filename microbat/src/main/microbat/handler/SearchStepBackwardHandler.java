package microbat.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class SearchStepBackwardHandler extends SearchStepHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.directionDown = false;
		
		return super.execute(event);
	}
}
