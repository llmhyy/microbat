package microbat.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import microbat.recommendation.AdvancedDetailInspector;

public class StaticAnalysisHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AdvancedDetailInspector inspector = new AdvancedDetailInspector();
		inspector.analysis();
		
		
		return null;
	}

}
