package microbat.instrumentation.trace;

import java.util.HashMap;
import java.util.Map;

public class RuntimeDataStore {
	private static Map<Long, RuntimeDataStore> rtStores;
	static {
		rtStores = new HashMap<>();
	}
	
	public void store(int lineNo, Object[] readValues, Object[] writeValuess) {
		// TODO Auto-generated method stub
		
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
