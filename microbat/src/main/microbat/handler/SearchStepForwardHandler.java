package microbat.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class SearchStepForwardHandler extends SearchStepHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.directionDown = true;
		
		return super.execute(event);
	}
}
