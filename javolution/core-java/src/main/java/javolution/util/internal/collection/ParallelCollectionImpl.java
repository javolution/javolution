/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Iterator;

import javolution.context.ConcurrentContext;
import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.service.CollectionService;

/**
 * A parallel view over a collection. 
 */
public class ParallelCollectionImpl<E> extends CollectionView<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public ParallelCollectionImpl(CollectionService<E> target) {
        super(target);
    }

    @Override
    public boolean add(E e) {
        return target().add(e);
    }

    @Override
    public void clear() {
        target().clear();
    }

    @Override
    public Equality<? super E> comparator() {
        return target().comparator();
    }

    @Override
    public boolean contains(Object obj) {
        return target().contains(obj);
    }

    @Override
    public boolean isEmpty() {
        return target().isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return target().iterator();
    }

    @Override
    public void perform(final Consumer<CollectionService<E>> action,
            CollectionService<E> view) {
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            int concurrency = ctx.getConcurrency();
            CollectionService<E>[] subViews = view.split(concurrency + 1, false);
            for (int i = 1; i < subViews.length; i++) {
                final CollectionService<E> subView = subViews[i];
                ctx.execute(new Runnable() {
                    @Override
                    public void run() {
                        target().perform(action, subView);
                    }
                });
            }
            target().perform(action, subViews[0]); // This thread works too !
        } finally {
            // Any exception raised during parallel iterations will be re-raised here.                       
            ctx.exit();
        }
    }

    @Override
    public boolean remove(Object obj) {
        return target().remove(obj);
    }

    @Override
    public int size() {
        return target().size();
    }

    @Override
    public CollectionService<E>[] split(int n, boolean threadsafe) {
        return target().split(n, threadsafe); // Forwards.
    }

    @Override
    public void update(final Consumer<CollectionService<E>> action,
            CollectionService<E> view) {
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            int concurrency = ctx.getConcurrency();
            CollectionService<E>[] subViews = view.split(concurrency + 1, true);
            for (int i = 1; i < subViews.length; i++) {
                final CollectionService<E> subView = subViews[i];
                ctx.execute(new Runnable() {
                    @Override
                    public void run() {
                        target().update(action, subView);
                    }
                });
            }
            target().perform(action, subViews[0]); // This thread works too !
        } finally {
            // Any exception raised during parallel iterations will be re-raised here.                       
            ctx.exit();
        }
    }

}
