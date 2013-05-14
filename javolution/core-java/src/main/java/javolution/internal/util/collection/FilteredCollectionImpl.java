/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.io.Serializable;
import java.util.Iterator;

import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * A filtered view over a collection.
 */
public final class FilteredCollectionImpl<E> implements
        CollectionService<E>, Serializable {

    private final CollectionService<E> that;
    
    private final Predicate<? super E> predicate;

    public FilteredCollectionImpl(CollectionService<E> that, 
            Predicate<? super E> predicate) {
        this.that = that;
        this.predicate = predicate;
    }

    @Override
    public int size() {
        final int[] count = new int[1];
        that.doWhile(new Predicate<E>() {

            @Override
            public Boolean apply(E param) {
                if (predicate.apply(param)) count[0]++;
                return true;
            }});
        return count[0];
    }

    @Override
    public void clear() {
        that.removeAll(predicate);
    }
    
    @Override
    public boolean add(E element) {
        return predicate.apply(element) && that.add(element); 
    }

    @Override
    public boolean contains(E element) {
        return predicate.apply(element) && that.contains(element);
    }

    @Override
    public boolean remove(E element) {
        return predicate.apply(element) && that.remove(element);
    }

    @Override
    public boolean doWhile(final Predicate<? super E> p) {
        return that.doWhile(new Predicate<E>() {

            @Override
            public Boolean apply(E param) {
                if (predicate.apply(param)) {
                    return p.apply(param);
                }
                return true;
            }});
    }

    @Override
    public boolean removeAll(final Predicate<? super E> p) {
        return that.removeAll(new Predicate<E>() {

            @Override
            public Boolean apply(E param) {
                return predicate.apply(param) && p.apply(param);
            }});
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> thatIterator = that.iterator();
        return new Iterator<E>() {
            E next = null; // Next element for which the predicate is verified. 
            boolean peekNext; // If the next element has been read in advance.

            @Override
            public boolean hasNext() {
                if (peekNext) return true;
                while (true) {
                    if (!thatIterator.hasNext()) return false;
                    next = thatIterator.next();
                    if (predicate.apply(next)) {
                        peekNext = true;
                        return true;
                    }
                }
            }

            @Override
            public E next() {                
                if (peekNext) { // Usually true (hasNext has been called before). 
                    peekNext = false;
                    return next;
                }
                while (true) {
                    E e = thatIterator.next();
                    if (predicate.apply(e)) return e;
                }
            }

            @Override
            public void remove() {
                thatIterator.remove();
            }

        };
    }

    @Override
    public ComparatorService<? super E> getComparator() {
        return that.getComparator();
    }
    
    @Override
    public void setComparator(ComparatorService<? super E> cmp) {
        that.setComparator(cmp);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = that.trySplit(n);
        if (tmp == null) return null;
        FilteredCollectionImpl<E>[] filtereds = new FilteredCollectionImpl[tmp.length]; 
       for (int i=0; i < tmp.length; i++) {
            filtereds[i] = new FilteredCollectionImpl<E>(tmp[i], predicate); 
       }
        return filtereds;
    }
    
    private static final long serialVersionUID = -8038802508310724258L;
   
}
