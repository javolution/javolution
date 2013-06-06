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

import javolution.util.FastTable;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * A reversed view over a collection.
 */
public class ReversedCollectionImpl<E> implements
        CollectionService<E>, Serializable {

    protected final CollectionService<E> that;

    public ReversedCollectionImpl(CollectionService<E> that) {
        this.that = that;
    }

    @Override
    public boolean add(E element) {
        return that.add(element);
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        return getReversedTable().doWhile(predicate);
    }
    
    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        return that.removeIf(predicate);
    }

    @Override
    public Iterator<E> iterator() {
        return getReversedTable().unmodifiable().iterator();
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
        ReversedCollectionImpl<E>[] sorteds = new ReversedCollectionImpl[tmp.length]; 
       for (int i=0; i < tmp.length; i++) {
           sorteds[i] = new ReversedCollectionImpl<E>(tmp[i]); 
       }
        return sorteds;
    }
    
    private FastTable<E> getReversedTable() {
        final FastTable<E> reversed = new FastTable<E>();
        that.doWhile(new Predicate<E>() {

            @Override
            public boolean test(E e) {
                reversed.addFirst(e);
                return true;
            }});
        return reversed;
   }

    private static final long serialVersionUID = 4309258622150539799L;
}
