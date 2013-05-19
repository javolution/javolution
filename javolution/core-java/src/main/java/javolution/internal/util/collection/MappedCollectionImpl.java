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

import javolution.util.Comparators;
import javolution.util.function.Function;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * A mapped view over a collection.
 */
public final class MappedCollectionImpl<E,R> implements
        CollectionService<R>, Serializable {

    private final CollectionService<E> that;
    
    private final  Function<? super E, ? extends R> function;
    
    private ComparatorService<? super R> comparator = Comparators.STANDARD;

    public MappedCollectionImpl(CollectionService<E> that, 
            Function<? super E, ? extends R> function) {
        this.that = that;
        this.function = function;
    }

    @Override
    public int size() {
        return that.size();
    }

    @Override
    public void clear() {
        that.clear();
    }
    
    @Override
    public boolean add(R element) {
        throw new UnsupportedOperationException("New elements cannot be added to mapped views");
    }

    @Override
    public boolean contains(final R element) {
        return !that.doWhile(new Predicate<E>() {

            @Override
            public Boolean apply(E param) {
                R r = function.apply(param);
                if (comparator.areEqual(element, r)) return false;
                return true;
            }});
    
    }

    @Override
    public boolean remove(final R element) {
        final boolean[] modified = new boolean[1];
        return !that.doWhile(new Predicate<E>() {

            @Override
            public Boolean apply(E param) {
                R r = function.apply(param);
                if (comparator.areEqual(element, r)) {
                    modified[0] = that.remove(param);
                    return false;
                }
                return true;
            }}) && modified[0];
    }

    @Override
    public boolean doWhile(final Predicate<? super R> predicate) {
        return that.doWhile(new Predicate<E>() {

            @Override
            public Boolean apply(E param) {
                R r = function.apply(param);
                return predicate.apply(r);
                }});
    }

    @Override
    public boolean removeAll(final Predicate<? super R> predicate) {
        return that.removeAll(new Predicate<E>() {

            @Override
            public Boolean apply(E param) {
                R r = function.apply(param);
                return predicate.apply(r);
            }});
    }

    @Override
    public Iterator<R> iterator() {
        final Iterator<E> thatIterator = that.iterator();
        return new Iterator<R>() {
       
            @Override
            public boolean hasNext() {
                return thatIterator.hasNext();
            }

            @Override
            public R next() {                
                return function.apply(thatIterator.next());
            }

            @Override
            public void remove() {
                thatIterator.remove();
            }

        };
    }

    @Override
    public ComparatorService<? super R> getComparator() {
        return comparator;
    }
    
    @Override
    public void setComparator(ComparatorService<? super R> cmp) {
        this.comparator = cmp;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<R>[] trySplit(int n) {
        CollectionService<E>[] tmp = that.trySplit(n);
        if (tmp == null) return null;
        MappedCollectionImpl<E,R>[] mappeds = new MappedCollectionImpl[tmp.length]; 
       for (int i=0; i < tmp.length; i++) {
            mappeds[i] = new MappedCollectionImpl<E,R>(tmp[i], function); 
       }
        return mappeds;
    }
  
    private static final long serialVersionUID = -7828030658889842514L;
}
