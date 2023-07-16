package microbat.debugpilot.propagation.spp;

import java.util.ArrayList;
import java.util.List;

import microbat.log.Log;

public enum ModelPrediction {
	SUSPICION(0),
	NOT_SUSPICION(1),
	UNCERTAIN(2);
	
	protected int id;
	
	private ModelPrediction(final int id) {
		this.id = id;
	}
	
	public int getID() {
		return this.id;
	}
	
	public String getStringID() {
		return String.valueOf(this.getID());
	}
	
	public static ModelPrediction parseString(final String string) {
		ModelPrediction action = null;
		for (ModelPrediction a : ModelPrediction.values()) {
			if (string.equals(a.getStringID())) {
				action = a;
				break;
			}
		}
		
		if (action == null) {
			throw new IllegalArgumentException(Log.genMsg("ModelAction", "Cannot parse string " + string + " to action"));
		}
		
		return action;
	}
	
	public static List<ModelPrediction> parseStringList(final String stringList) {
		return ModelPrediction.parseStringList(stringList, ",");
	}
	
	public static List<ModelPrediction> parseStringList(final String string, final String delimiter) {
		List<ModelPrediction> actionList = new ArrayList<>();
		for (String token : string.split(delimiter)) {
			actionList.add(ModelPrediction.parseString(token));
		}
		
		if (actionList.isEmpty()) {
			throw new IllegalArgumentException(Log.genMsg("ModelAction", "Cannot parse string " + string + " into action list"));
		}
		
		return actionList;
	}
}
