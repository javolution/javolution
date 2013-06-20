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
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * An unmodifiable sorted view over a collection.
 */
public final class SortedCollectionImpl<E> extends FastCollection<E> implements
        CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final CollectionService<E> target;

    public SortedCollectionImpl(CollectionService<E> that) {
        this.target = that;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Sorted views are unmodifiable");
    }
    
    @Override
    public void atomic(Runnable action) {
        target.atomic(action);
    }
   
    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public void forEach(Consumer<? super E> consumer, IterationController controller) {
        FastTable<E> sorted = new FastTable<E>(target.comparator());        
        sorted.addAll(this);
        sorted.sort(); // Quick-sort.
        FastCollection.serviceOf(sorted).forEach(consumer, controller);
    }
    
    @Override
    public Iterator<E> iterator() {
        FastTable<E> sorted = new FastTable<E>(target.comparator());        
        sorted.addAll(this);
        sorted.sort(); // Quick-sort.
        return sorted.unmodifiable().iterator();
     }
    
    @Override
    public boolean removeIf(Predicate<? super E> filter, IterationController controller) {
        throw new UnsupportedOperationException("Sorted views are unmodifiable");
    }

    @Override
    protected SortedCollectionImpl<E> service() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SortedCollectionImpl<E>[] trySplit(int n) {
        return new SortedCollectionImpl[] { this }; // Does not make sense to split.
    }
}
