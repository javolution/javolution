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
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;
import javolution.util.service.ConsumerService;

/**
 * A filtered view over a collection.
 */
public final class FilteredCollectionImpl<E> extends FastCollection<E>
        implements CollectionService<E> {

    /**
     * 
     */
    private static final long serialVersionUID = -5475396934090095686L;
    private final CollectionService<E> target;
    private final Predicate<? super E> filter;

    public FilteredCollectionImpl(CollectionService<E> target,
            Predicate<? super E> filter) {
        this.target = target;
        this.filter = filter;
    }

    @Override
    public boolean add(E element) {
        return filter.test(element) && target.add(element);
    }

    @Override
    public void forEach(final ConsumerService<? super E> consumer) {
        if (consumer instanceof ConsumerService.Sequential) {
            target.forEach(new ConsumerService.Sequential<E>() {
                @Override
                public void accept(E e, Controller controller) {
                    if (filter.test(e)) {
                        consumer.accept(e, controller);
                    }
                }
            });
        } else {
            target.forEach(new ConsumerService<E>() {
                @Override
                public void accept(E e, Controller controller) {
                    if (filter.test(e)) {
                        consumer.accept(e, controller);
                    }
                }
            });
        }
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> targetIterator = target.iterator();
        return new Iterator<E>() {
            E next = null; // Next element for which the predicate is verified. 
            boolean peekNext; // If the next element has been read in advance.

            @Override
            public boolean hasNext() {
                if (peekNext)
                    return true;
                while (true) {
                    if (!targetIterator.hasNext())
                        return false;
                    next = targetIterator.next();
                    if (filter.test(next)) {
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
                    if (filter.test(next))
                        return next;
                }
            }

            @Override
            public void remove() {
                targetIterator.remove();
            }

        };
    }

    @Override
    public ComparatorService<? super E> comparator() {
        return target.comparator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = target.trySplit(n);
        if (tmp == null)
            return null;
        FilteredCollectionImpl<E>[] filtereds = new FilteredCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            filtereds[i] = new FilteredCollectionImpl<E>(tmp[i], filter);
        }
        return filtereds;
    }

    @Override
    public FilteredCollectionImpl<E> service() {
        return this;
    }

}
