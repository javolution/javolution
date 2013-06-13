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
import javolution.util.FastSet;
import javolution.util.function.CollectionConsumer;
import javolution.util.function.FullComparator;
import javolution.util.service.CollectionService;
import javolution.util.service.SetService;

/**
 * A view which does not iterate twice over the same elements and which 
 * maintains element unicity.
 */
public final class DistinctCollectionImpl<E> extends FastCollection<E>
        implements CollectionService<E> {

    private static final long serialVersionUID = 5695227218089832829L;
    private final CollectionService<E> target;

    public DistinctCollectionImpl(CollectionService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        return checkContains(element) ? false : target.add(element);
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
            target.forEach(new CollectionConsumer.Sequential<E>() {
                FastSet<E> iterated = new FastSet<E>(target.comparator());

                @Override
                public void accept(E e, Controller controller) {
                    if (!iterated.add(e))
                        return; // Already iterated over.
                    consumer.accept(e, controller);
                }
            });
        } else { // Potentially concurrent (use shared collection).
            target.forEach(new CollectionConsumer<E>() {
                FastSet<E> iterated = new FastSet<E>(target.comparator()).shared();

                @Override
                public void accept(E e, Controller controller) {
                    if (!iterated.add(e))
                        return; // Already iterated over.
                    consumer.accept(e, controller);
                }
            });
        }
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> targetIterator = target.iterator();
        final FastSet<E> iterated = new FastSet<E>(target.comparator());
        return new Iterator<E>() {
            E next = null; // Next element not already iterated over. 
            boolean peekNext; // If the next element has been read in advance.

            @Override
            public boolean hasNext() {
                if (peekNext)
                    return true;
                while (true) {
                    if (!targetIterator.hasNext())
                        return false;
                    next = targetIterator.next();
                    if (!iterated.contains(next)) {
                        iterated.add(next);
                        peekNext = true;
                        return true;
                    }
                }
            }

            @Override
            public E next() {
                if (peekNext) { // Usually true (hasNext has been called before). 
                    peekNext = false;
                    return next;
                }
                while (true) {
                    next = targetIterator.next();
                    if (!iterated.contains(next)) {
                        iterated.add(next);
                        return next;
                    }
                }
            }

            @Override
            public void remove() {
                targetIterator.remove();
            }

        };
    }

    @Override
    public FullComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        return target.trySplit(n);
    }

    // Check if this collection contains the specified element.
    private boolean checkContains(final E element) {
        if (target instanceof SetService)
            return ((SetService<E>) target).contains(element);
        final boolean[] found = new boolean[1];
        target.forEach(new CollectionConsumer<E>() {
            FullComparator<? super E> cmp = target.comparator();

            @Override
            public void accept(E e, Controller controller) {
                if (cmp.areEqual((E) element, e)) {
                    found[0] = true;
                    controller.terminate();
                }
            }
        });
        return found[0];
    }

    @Override
    public DistinctCollectionImpl<E> service() {
        return this;
    }
}
