/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Collection;

import javolution.context.ConcurrentContext;
import javolution.util.function.Consumer;
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
    public void clear() {
        target().clear();
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
    public void perform(final Consumer<Collection<E>> action,
            CollectionService<E> view) {
        final ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            int concurrency = ctx.getConcurrency();
            CollectionService<E>[] subViews = view.subViews(concurrency + 1);
            if (subViews.length == 1) { // No concurrency.
                target().perform(action, subViews[0]);
                return;
            }
            for (final CollectionService<E> subView : subViews) {
                ctx.execute(new Runnable() {
                    @Override
                    public void run() {
                        target().perform(action, subView); // Non-recursive.
                    }
                });
            }
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
    public void update(final Consumer<Collection<E>> action,
            CollectionService<E> view) {
        final ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            int concurrency = ctx.getConcurrency();
            CollectionService<E>[] subViews = view.subViews(concurrency + 1);
            if (subViews.length == 1) { // No concurrency.
                target().update(action, subViews[0]);
                return;
            }
            for (final CollectionService<E> subView : subViews) {
                ctx.execute(new Runnable() {
                    @Override
                    public void run() {
                        target().update(action, subView); // Non-recursive.
                    }
                });
            }
        } finally {
            // Any exception raised during parallel iterations will be re-raised here.                       
            ctx.exit();
        }
    }

}
