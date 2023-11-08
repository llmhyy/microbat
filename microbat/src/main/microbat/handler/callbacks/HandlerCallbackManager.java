package microbat.handler.callbacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HandlerCallbackManager {
	
	private static HandlerCallbackManager INSTANCE = null;
	
	private List<HandlerCallback> debugpilotTerminateCallbacks = Collections.synchronizedList(new ArrayList<>());
	
	private HandlerCallbackManager() {
		
	}
	
	public static synchronized HandlerCallbackManager getInstance() {
		if (HandlerCallbackManager.INSTANCE == null) {
			HandlerCallbackManager.INSTANCE = new HandlerCallbackManager();
		}
		return HandlerCallbackManager.INSTANCE;
	}
	
	public void registerDebugPilotTermianteCallback(final HandlerCallback callback) {
		this.debugpilotTerminateCallbacks.add(callback);
	}
	
	public void removeDebugPilotTerminateCallback(final HandlerCallback callback) {
		this.debugpilotTerminateCallbacks.remove(callback);
	}
	
	public void runDebugPilotTerminateCallbacks() {
		synchronized (this.debugpilotTerminateCallbacks) {
			for (HandlerCallback callback : this.debugpilotTerminateCallbacks) {
				callback.callBack();
			}
		}
	}
	
}
