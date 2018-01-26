package microbat.instrumentation.trace.data;

import java.util.HashMap;
import java.util.Map;

public class RuntimeDataStore {
	private static Map<Long, RuntimeDataStore> rtStores;
	static {
		rtStores = new HashMap<>();
	}
	
	public void store(int lineNo, Object[] readValues, Object[] writeValuess) {
		if (!filter()) {
			// execute
		}
	}
	
	/**
	 * to record return value if needed 
	 * */
	public void returnValue() {
		
	}
	
	private boolean filter() {
		// TODO check if this is excluded & not being invoked by a valid class 
		return false;
	}

	public synchronized static RuntimeDataStore getStore() {
		long threadId = Thread.currentThread().getId();
		RuntimeDataStore store = rtStores.get(threadId);
		if (store == null) {
			store = new RuntimeDataStore();
			rtStores.put(threadId, store);
		}
		return store;
	}
	
}
