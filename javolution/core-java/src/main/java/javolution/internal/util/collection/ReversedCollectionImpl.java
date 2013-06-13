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
import javolution.util.function.CollectionConsumer;
import javolution.util.function.FullComparator;
import javolution.util.service.CollectionService;
import javolution.util.service.TableService;

/**
 * A reversed view over a collection (unmodifiable).
 */
public final class ReversedCollectionImpl<E> extends FastCollection<E> implements
        CollectionService<E> {

    private static final long serialVersionUID = -8075747377480487300L;
    private final CollectionService<E> target;

    public ReversedCollectionImpl(CollectionService<E> target) {
        this.target = target;
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
    public void forEach(final CollectionConsumer<? super E> consumer) {
        reversed().forEach(consumer);
    }
    
    @Override
    public Iterator<E> iterator() {
        return reversed().iterator();
    }
    
    @Override
    public FullComparator<? super E> comparator() {
        return target.comparator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        return reversed().service().trySplit(n);
    }
    
    private FastTable<E> reversed() {
        final FastTable<E> reversed = new FastTable<E>();
        target.forEach(new CollectionConsumer.Sequential<E>() {

            @Override
            public void accept(E e, Controller controller) {
                reversed.addFirst(e);
            }});
        return reversed.unmodifiable();
    }

    @Override
    public ReversedCollectionImpl<E> service() {
        return this;
    }
}
