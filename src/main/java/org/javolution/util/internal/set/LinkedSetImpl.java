/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import org.javolution.util.FastIterator;
import org.javolution.annotations.Nullable;
import org.javolution.util.AbstractSet;
import org.javolution.util.AbstractTable;
import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A linked view over a set.
 */
public final class LinkedSetImpl<E> extends AbstractSet<E> {
    
    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractSet<E> inner;
    private final AbstractTable<E> insertionTable;

    public LinkedSetImpl(AbstractSet<E> inner) {
        this.inner = inner;
        this.insertionTable = new FastTable<E>().equality(Equality.identity());
    }

    public LinkedSetImpl(AbstractSet<E> inner, AbstractTable<E> insertionTable) {
        this.inner = inner;
        this.insertionTable = insertionTable;
    }

    @Override
    public boolean add(E element, boolean allowDuplicate) {
        return inner.add(element, allowDuplicate) ? insertionTable.add(element) : false;
    }

    @Override
    public void clear() {
        inner.clear();
        insertionTable.clear();
    }

    @Override
    public LinkedSetImpl<E> clone() {
        return new LinkedSetImpl<E>(inner.clone(), insertionTable.clone());
    }

    @Override
    public FastIterator<E> iterator() {
        return insertionTable.iterator();
    }

    @Override
    public FastIterator<E> descendingIterator() {
        return insertionTable.descendingIterator();
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public Order<? super E> order() {
        return inner.order();
    }

    @Override
    public FastIterator<E> iterator(@Nullable final E low) {
        if (low == null) return insertionTable.iterator();
        return insertionTable.filter(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                Order<? super E> order = order();
                int cmp = order.compare(low, param);
                return (cmp == 0) ? order.areEqual(low, param) : cmp < 0;
            }}).iterator();
    }

    @Override
    public FastIterator<E> descendingIterator(@Nullable final E high) {
            if (high == null) return insertionTable.descendingIterator();
            return insertionTable.filter(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                Order<? super E> order = order();
                int cmp = order.compare(high, param);
                return (cmp == 0) ? order.areEqual(high, param) : cmp > 0;
            }}).descendingIterator();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        if (!inner.removeIf(filter)) return false;
        insertionTable.removeIf(filter);
        return true;
    }

    @Override
    public E getAny(E element) {
        return inner.getAny(element);
    }

    @Override
    public E removeAny(E element) {
        E removed = inner.removeAny(element);
        if (removed != null) insertionTable.remove(removed);
        return removed;
    }

}
