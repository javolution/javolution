/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import javolution.context.ConcurrentContext;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A parallel view over a collection.
 */
public class ParallelCollectionImpl<E> extends SharedCollectionImpl<E> {

    public ParallelCollectionImpl(CollectionService<E> that) {
        super(that);
    }

    @Override
    public boolean doWhile(final Predicate<? super E> predicate) {
        read.lock();
        try {
            CollectionService<E>[] split = that
                    .trySplit(ConcurrentContext.CONCURRENCY.get());
            if (split == null)
                return that.doWhile(predicate);
            final boolean[] completed = new boolean[] { true };
            ConcurrentContext ctx = ConcurrentContext.enter();
            try {
                for (int i = 0; i < split.length; i++) {
                    final CollectionService<E> subCollection = split[i];
                    ctx.execute(new Runnable() {

                        @Override
                        public void run() {
                            if (!subCollection.doWhile(predicate)) {
                                completed[0] = false;
                            }

                        }
                    });
                }
            } finally {
                ctx.exit();
            }
            return completed[0];
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean removeIf(final Predicate<? super E> predicate) {
        write.lock();
        try {
            CollectionService<E>[] split = that
                    .trySplit(ConcurrentContext.CONCURRENCY.get());
            if (split == null)
                return that.removeIf(predicate);
            final boolean[] anyRemoved = new boolean[] { false };
            ConcurrentContext ctx = ConcurrentContext.enter();
            try {
                for (int i = 0; i < split.length; i++) {
                    final CollectionService<E> subCollection = split[i];
                    ctx.execute(new Runnable() {

                        @Override
                        public void run() {
                            if (subCollection.removeIf(predicate)) {
                                anyRemoved[0] = true;
                            }

                        }
                    });
                }
            } finally {
                ctx.exit();
            }
            return anyRemoved[0];
        } finally {
            write.unlock();
        }
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        return null; // No more split.
    }

    private static final long serialVersionUID = 6387429974784771L;
}
