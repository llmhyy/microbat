package microbat.instrumentation.runtime;

/**
 * 
 * @author Lin Yun and LLT
 * 
 * This class is supposed to keep at very basic, NOT use or trigger ANY other liberay function even in jdk,
 * only Array is allowed.
 * [TO AVOID RECURSIVE LOOP IN GET_TRACER!!]
 */
public class LockedThreads {
	public static final long TRACKING = 0;
	public static final long UNTRACKING = 1;
	
	/**
	 * The first dimension is for thread id (long type), and the second dimension is for its state (i.e., tracking or untracking)
	 * 
	 * If a thread is in the state of TRACKING, all the relevant steps will be recorded (by invoking {@code ExecutionTracer}).
	 * Otherwise, no step will be recorded (by invoking {@code EmptyTracer})
	 */
	public volatile long[][] lockedThreadIds = new long[10][2];
	
	public LockedThreads() {
		for (int i = 0; i < lockedThreadIds.length; i++) {
			lockedThreadIds[i][0] = -1l;
			lockedThreadIds[i][1] = TRACKING;
		}
	}

	public boolean isUntracking(long threadId) {
		for (int i = 0; i < lockedThreadIds.length; i++) {
			if (lockedThreadIds[i][0] == threadId) {
				return lockedThreadIds[i][1] == UNTRACKING;
			}
		}
		
		return false;
	}

	public void track(long threadId) {
		int i = 0;
		int firstSlotIndex = -1;
		for (; i < lockedThreadIds.length; i++) {
			if (lockedThreadIds[i][0] == threadId) {
				lockedThreadIds[i][1] = TRACKING;
				return;
			}
			
			if(firstSlotIndex == -1 && lockedThreadIds[i][0] == -1) {
				firstSlotIndex = i;
			}
		}
		
		/**
		 * all the slots are not -1
		 */
		if(firstSlotIndex == -1) {
			increaseSize();
		}
		/**
		 * fill in an empty slot
		 */
		else {
			i = firstSlotIndex;			
		}
		lockedThreadIds[i][0] = threadId;
		lockedThreadIds[i][1] = TRACKING;
	}
	
	 private void increaseSize() {
        int newCapacity = lockedThreadIds.length + 2;
        long[][] temp = new long[newCapacity][2];
        int i = 0;
        for (; i < lockedThreadIds.length; i++) {
        	temp[i] = lockedThreadIds[i];
        }
        for (; i < newCapacity; i++) {
        	temp[i][0] = -1l;
        	temp[i][1] = TRACKING;
        }
        lockedThreadIds = temp;
	 }

//	public void remove(long threadId) {
//		for (int i = 0; i < lockedThreadIds.length; i++) {
//			if (lockedThreadIds[i] == threadId) {
//				lockedThreadIds[i] = -1l;
//			}
//		}
//	}
	
	public void untrack(long threadId) {
		int i = 0;
		int firstSlotIndex = -1;
		for (; i < lockedThreadIds.length; i++) {
			if (lockedThreadIds[i][0] == threadId) {
				lockedThreadIds[i][1] = UNTRACKING;
				return;
			}
			
			if(firstSlotIndex == -1 && lockedThreadIds[i][0] == -1) {
				firstSlotIndex = i;
			}
		}
		
		/**
		 * all the slots are not -1
		 */
		if(firstSlotIndex == -1) {
			increaseSize();
		}
		/**
		 * fill in an empty slot
		 */
		else {
			i = firstSlotIndex;			
		}
		lockedThreadIds[i][0] = threadId;
		lockedThreadIds[i][1] = UNTRACKING;
	}
	
}
