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
public class FilteredCollectionImpl<E> implements
        CollectionService<E>, Serializable {

    protected final CollectionService<E> that;
    
    protected final Predicate<? super E> filter;

    public FilteredCollectionImpl(CollectionService<E> that, 
            Predicate<? super E> filter) {
        this.that = that;
        this.filter = filter;
    }

  
    @Override
    public boolean add(E element) {
        return filter.test(element) && that.add(element); 
    }

    @Override
    public boolean doWhile(final Predicate<? super E> p) {
        return that.doWhile(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                if (filter.test(param)) {
                    return p.test(param);
                }
                return true;
            }});
    }

    @Override
    public boolean removeIf(final Predicate<? super E> p) {
        return that.removeIf(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                return filter.test(param) && p.test(param);
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
                    if (filter.test(next)) {
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
                    next = thatIterator.next();
                    if (filter.test(next)) return next;
                }
            }

            @Override
            public void remove() {
                thatIterator.remove();
            }

        };
    }

    @Override
    public ComparatorService<? super E> comparator() {
        return that.comparator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = that.trySplit(n);
        if (tmp == null) return null;
        FilteredCollectionImpl<E>[] filtereds = new FilteredCollectionImpl[tmp.length]; 
       for (int i=0; i < tmp.length; i++) {
            filtereds[i] = new FilteredCollectionImpl<E>(tmp[i], filter); 
       }
        return filtereds;
    }
    
    private static final long serialVersionUID = -8038802508310724258L;
   
}
