package microbat.baseline;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

/*
 * Child class of PriorityQueue that does not allow repeated
 * objects to be added in. This is done by using a HashMap to
 * keep track of the item in the Priority Queue so as to not 
 * let repeat item in.
 */
public class UniquePriorityQueue<E> extends PriorityQueue<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -127350210655227822L;
	private HashSet<E> inQueue = new HashSet<>();
	
	public UniquePriorityQueue() {
		super();
	}
	
	public UniquePriorityQueue(Comparator<? super E> comparator) {
		super(comparator);
	}
	
	@Override
	public boolean add(E e) {
		return this.offer(e);
	}
	
	public boolean addIgnoreNull(E e) {
		if (e == null)
			return false;
		return this.add(e);
	}
	
	@Override
	public void clear() {
		super.clear();
		inQueue.clear();
	}
	
	@Override
	public boolean offer(E e) {
		if (contains(e)) return false;
		boolean added = super.offer(e);
		if (added) inQueue.add(e);
		return added;
	}
	
	@Override
	public boolean remove(Object o) {
		if (!this.contains(o)) return false;
		boolean removed = super.remove(o);
		if (removed) inQueue.remove(o);
		return removed;
	}
	
	@Override
	public boolean contains(Object o) {
		return inQueue.contains(o);
	}
	
	@Override
	public E poll() {
		E e = super.poll();
		inQueue.remove(e);
		return e;
	}
}
