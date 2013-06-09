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
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;
import javolution.util.service.ConsumerService;

/**
 * A view using a custom comparator for element equality or comparison.
 */
public final class ComparatorCollectionImpl<E> extends FastCollection<E> implements
        CollectionService<E> {
    
    private static final long serialVersionUID = -8346102607362932618L;
    private final CollectionService<E> target;
    private final ComparatorService<? super E> comparator;

    public ComparatorCollectionImpl(CollectionService<E> target,
            ComparatorService<? super E> comparator) {
        this.target = target;
        this.comparator = comparator;
    }

    @Override
    public boolean add(E element) {
        return target.add(element);
    }

    @Override
    public void forEach(ConsumerService<? super E> consumer) {
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
    public ComparatorService<? super E> comparator() {
        return comparator;
    }

    @Override
    public ComparatorCollectionImpl<E> service() {
        return this;
    }
}
