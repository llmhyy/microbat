package microbat.instrumentation.runtime;

/**
 * 
 * @author LLT
 * 
 * This class is supposed to keep at very basic, NOT use or trigger ANY other liberay function even in jdk,
 * only Array is allowed.
 * [TO AVOID RECURSIVE LOOP IN GET_TRACER!!]
 */
public class LockedThreads {
	long[] lockedThreadIds = new long[10];
	
	public LockedThreads() {
		for (int i = 0; i < lockedThreadIds.length; i++) {
			lockedThreadIds[i] = -1l;
		}
	}

	public boolean contains(long threadId) {
		for (int i = 0; i < lockedThreadIds.length; i++) {
			if (lockedThreadIds[i] == threadId) {
				return true;
			}
		}
		return false;
	}

	public void add(long threadId) {
		int i = 0;
		for (; i < lockedThreadIds.length; i++) {
			if (lockedThreadIds[i] < 0) {
				lockedThreadIds[i] = threadId;
				return;
			}
		}
		increaseSize();
		lockedThreadIds[i] = threadId;
	}
	
	 private void increaseSize() {
        int newCapacity = lockedThreadIds.length + 2;
        long[] temp = new long[newCapacity];
        int i = 0;
        for (; i < lockedThreadIds.length; i++) {
        	temp[i] = lockedThreadIds[i];
        }
        for (; i < newCapacity; i++) {
        	temp[i] = -1l;
        }
        lockedThreadIds = temp;
	 }

	public void remove(long threadId) {
		for (int i = 0; i < lockedThreadIds.length; i++) {
			if (lockedThreadIds[i] == threadId) {
				lockedThreadIds[i] = -1l;
				return;
			}
		}
	}
	
	
}
