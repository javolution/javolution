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
public class MappedCollectionImpl<E,R> implements
        CollectionService<R>, Serializable {

    protected final CollectionService<E> that;
    
    protected final  Function<? super E, ? extends R> function;
 
    public MappedCollectionImpl(CollectionService<E> that, 
            Function<? super E, ? extends R> function) {
        this.that = that;
        this.function = function;
    }
    
    @Override
    public boolean add(R element) {
        throw new UnsupportedOperationException("New elements cannot be added to mapped views");
    }

    @Override
    public boolean doWhile(final Predicate<? super R> predicate) {
        return that.doWhile(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                R r = function.apply(param);
                return predicate.test(r);
                }});
    }

    @Override
    public boolean removeIf(final Predicate<? super R> predicate) {
        return that.removeIf(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                R r = function.apply(param);
                return predicate.test(r);
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
    public ComparatorService<? super R> comparator() {
        return Comparators.STANDARD;
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
  
    private static final long serialVersionUID = 5283622142410922044L;
}
