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
import org.javolution.util.AbstractSet;
import org.javolution.util.FastTable;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A linked view over a set.
 */
public final class LinkedSetImpl<E> extends AbstractSet<E> {
    
    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractSet<E> inner;
    private final FastTable<E> insertionTable;

    public LinkedSetImpl(AbstractSet<E> inner) {
        this.inner = inner;
        this.insertionTable = new FastTable<E>(inner.order());
    }

    private LinkedSetImpl(AbstractSet<E> inner, FastTable<E> insertionTable) {
        this.inner = inner;
        this.insertionTable = insertionTable;
    }

    @Override
    public boolean add(E element) {
        return inner.add(element) ? insertionTable.add(element) : false;
    }

    @Override
    public boolean addMulti(E element) {
        return inner.addMulti(element) ? insertionTable.add(element) : false;
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
    public boolean contains(Object obj) {
        return inner.contains(obj);
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
    public boolean remove(Object obj) {
        if (!inner.remove(obj)) return false;
        insertionTable.remove(obj);
        return true;
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
    public FastIterator<E> iterator(E from) {
        return inner.iterator(from);
    }

    @Override
    public FastIterator<E> descendingIterator(E from) {
        return inner.descendingIterator(from);
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

}
