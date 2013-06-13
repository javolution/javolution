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

import javolution.context.ConcurrentContext;
import javolution.util.FastCollection;
import javolution.util.function.CollectionConsumer;
import javolution.util.function.FullComparator;
import javolution.util.service.CollectionService;

/**
 * A parallel view over a collection.
 */
public final class ParallelCollectionImpl<E> extends FastCollection<E>
        implements CollectionService<E> {

    private static final long serialVersionUID = -3997574892344595177L;
    private final CollectionService<E> target;

    public ParallelCollectionImpl(CollectionService<E> target) {
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
        if (consumer instanceof CollectionConsumer.Sequential) { 
            target.forEach(consumer); // Sequential.
            return;
        }
        CollectionService<E>[] split = target
                .trySplit(ConcurrentContext.CONCURRENCY.get());
        if (split == null) {
            target.forEach(consumer);
            return;
        }
        // Parallelization.
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            for (int i = 0; i < split.length; i++) {
                final CollectionService<E> subcollection = split[i];
                ctx.execute(new Runnable() {
                    @Override
                    public void run() {
                        subcollection.forEach(consumer);
                    }
                });
            }
        } finally {
            ctx.exit();
        }
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        return target.trySplit(n);
    }

    @Override
    public Iterator<E> iterator() {
        return target.iterator();
    }

    @Override
    public FullComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public ParallelCollectionImpl<E> service() {
        return this;
    }
}
