package microbat.behavior;

import java.util.HashMap;

public class BehaviorData {
	public static HashMap<String, Behavior> projectBehavior = new HashMap<>();
	
	public static Behavior getOrNewBehavior(String caseName){
		Behavior behavior = projectBehavior.get(caseName);
		if(behavior == null){
			behavior = new Behavior();
			projectBehavior.put(caseName, behavior);
		}
		
		return behavior;
	}
}
