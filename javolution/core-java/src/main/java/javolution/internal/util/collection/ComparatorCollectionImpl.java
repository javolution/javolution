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

import javolution.util.function.CollectionConsumer;
import javolution.util.function.FullComparator;
import javolution.util.service.CollectionService;

/**
 * A view using a custom comparator for element equality or comparison.
 */
public class ComparatorCollectionImpl<E> implements CollectionService<E> {
    
    private final CollectionService<E> target;
    private final FullComparator<? super E> comparator;

    public ComparatorCollectionImpl(CollectionService<E> target,
            FullComparator<? super E> comparator) {
        this.target = target;
        this.comparator = comparator;
    }

    @Override
    public boolean add(E element) {
        return target.add(element);
    }

    @Override
    public void atomicRead(Runnable action) {
        target.atomicRead(action);
    }

    @Override
    public void atomicWrite(Runnable action) {
        target.atomicWrite(action);        
    }
    
    @Override
    public void forEach(CollectionConsumer<? super E> consumer) {
        target.forEach(consumer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        if (tmp == null)
            return null;
        CollectionService<E>[] result = new CollectionService[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = new ComparatorCollectionImpl<E>(tmp[i], comparator);
        }
        return result;
    }

    @Override
    public Iterator<E> iterator() {
        return target.iterator();
    }

    @Override
    public FullComparator<? super E> comparator() {
        return comparator;
    }

}
