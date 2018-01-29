package sav.common.core.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * a weak simple iterator chain for simple purpose.
 * */
public class Iterators<T> implements Iterator<T> {
    protected final List<Iterator<T>> iteratorChain;
    protected int currentIteratorIndex = 0;
    protected Iterator<T> currentIterator = null;

    public Iterators(Iterator<T>... iterators) {
        super();
        iteratorChain = Arrays.asList(iterators);
    }
    
    public boolean hasNext() {
        updateCurrentIterator();
        return currentIterator.hasNext();
    }

    public T next() {
        updateCurrentIterator();
        return currentIterator.next();
    }

    public void remove() {
       throw new UnsupportedOperationException();
    }

    public int size() {
        return iteratorChain.size();
    }

    protected void updateCurrentIterator() {
        if (currentIterator == null) {
            currentIterator = (Iterator<T>) iteratorChain.get(0);
        }

        while (currentIterator.hasNext() == false && currentIteratorIndex < iteratorChain.size() - 1) {
            currentIteratorIndex++;
            currentIterator = (Iterator<T>) iteratorChain.get(currentIteratorIndex);
        }
    }

   

}
