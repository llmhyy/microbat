package microbat.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import microbat.behavior.Behavior;
import microbat.behavior.BehaviorData;
import microbat.behavior.BehaviorReporter;
import microbat.util.Settings;

public class SearchStepForwardHandler extends SearchStepHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.directionDown = true;
		
		Behavior behavior = BehaviorData.getOrNewBehavior(Settings.launchClass);
		behavior.increaseSearchForward();
		new BehaviorReporter(Settings.launchClass).export(BehaviorData.projectBehavior);
		
		return super.execute(event);
	}
}
