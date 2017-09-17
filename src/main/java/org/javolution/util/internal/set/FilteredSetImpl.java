/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import java.util.Iterator;

import org.javolution.util.FastIterator;
import org.javolution.annotations.Nullable;
import org.javolution.util.AbstractSet;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.collection.FilteredCollectionImpl;

/**
 * A filtered view over a set.
 */
public final class FilteredSetImpl<E> extends AbstractSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final Predicate<? super E> filter;
    private final AbstractSet<E> inner;

    public FilteredSetImpl(AbstractSet<E> inner, Predicate<? super E> filter) {
        this.inner = inner;
        this.filter = filter;
    }

    @Override
    public boolean add(E element, boolean allowDuplicate) {
        return filter.test(element) ? inner.add(element, allowDuplicate) : false;
    }

    @Override
    public void clear() {
        inner.removeIf(filter);
    }

    @Override
    public AbstractSet<E> clone() {
        return new FilteredSetImpl<E>(inner.clone(), filter);
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public FastIterator<E> iterator() {
        return new FilteredCollectionImpl.IteratorImpl<E>(inner.iterator(), filter);
    }
    
    @Override
    public FastIterator<E> descendingIterator() {
        return new FilteredCollectionImpl.IteratorImpl<E>(inner.descendingIterator(), filter);
    }

    @Override
    public FastIterator<E> iterator(@Nullable E from) {
        return new FilteredCollectionImpl.IteratorImpl<E>(inner.iterator(from), filter);
    }
    
    @Override
    public FastIterator<E> descendingIterator(@Nullable E from) {
        return new FilteredCollectionImpl.IteratorImpl<E>(inner.descendingIterator(from), filter);
    }

    @Override
    public Order<? super E> order() {
        return inner.order();
    }

    @Override
    public boolean removeIf(final Predicate<? super E> toRemove) {
        return inner.removeIf(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                return filter.test(param) && toRemove.test(param);
            }
        });
    }

    @Override
    public int size() {
        int count = 0;
        for (Iterator<E> itr = iterator(); itr.hasNext(); itr.next())
            count++;
        return count;
    }

    @Override
    public AbstractSet<E>[] trySplit(int n) {
        AbstractSet<E>[] subViews = inner.trySplit(n);
        for (int i = 0; i < subViews.length; i++)
            subViews[i] = new FilteredSetImpl<E>(subViews[i], filter);
        return subViews;
    }

    @Override
    public E getAny(E element) {
        return filter.test(element) ? inner.getAny(element) : null;
    }

    @Override
    public E removeAny(E element) {
        return filter.test(element) ? inner.removeAny(element) : null;        
    }


}
