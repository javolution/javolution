/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.util.Iterator;

import javolution.util.FastCollection;
import javolution.util.FastTable;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * A sorted view over a collection.
 */
public final class SortedCollectionImpl<E> extends FastCollection<E> implements
        CollectionService<E> {

    private static final long serialVersionUID = 2125302736706554633L;
    private final CollectionService<E> target;

    public SortedCollectionImpl(CollectionService<E> that) {
        this.target = that;
    }

    @Override
    public boolean add(E element) {
        return target.add(element);
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        return getSortedTable().doWhile(predicate);
    }
    
    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        return target.removeIf(predicate);
    }

    @Override
    public Iterator<E> iterator() {
        return getSortedTable().unmodifiable().iterator();
     }

    @Override
    public ComparatorService<? super E> comparator() {
        return target.comparator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        if (tmp == null) return null;
        SortedCollectionImpl<E>[] sorteds = new SortedCollectionImpl[tmp.length]; 
        for (int i=0; i < tmp.length; i++) {
           sorteds[i] = new SortedCollectionImpl<E>(tmp[i]); 
        }
        return sorteds;
    }
    
    private FastTable<E> getSortedTable() {
        final FastTable<E> sorted = new FastTable<E>().usingComparator(target.comparator());
        target.doWhile(new Predicate<E>() {

            @Override
            public boolean test(E e) {
                sorted.addLast(e);
                return true;
            }});
        sorted.sort();
        return sorted;
   }

    @Override
    public SortedCollectionImpl<E> service() {
        return this;
    }
}
