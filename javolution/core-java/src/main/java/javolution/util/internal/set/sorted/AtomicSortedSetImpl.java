/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set.sorted;

import javolution.util.internal.set.AtomicSetImpl;
import javolution.util.service.SortedSetService;

/**
 * An atomic view over a sorted set allowing concurrent access and sequential updates.
 */
public class AtomicSortedSetImpl<E> extends AtomicSetImpl<E> implements
        SortedSetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public AtomicSortedSetImpl(SortedSetService<E> target) {
        super(target);
    }

    @Override
    public E first() {
        return targetView().first();
    }

    @Override
    public SortedSetService<E> headSet(E toElement) {
        return new SubSortedSetImpl<E>(this, null, toElement);
    }

    @Override
    public E last() {
        return targetView().last();
    }

    @Override
    public SortedSetService<E> subSet(E fromElement, E toElement) {
        return new SubSortedSetImpl<E>(this, fromElement, toElement);
    }

    @Override
    public SortedSetService<E> tailSet(E fromElement) {
        return new SubSortedSetImpl<E>(this, fromElement, null);
    }

    @Override
    public SortedSetService<E> threadSafe() {
        return this;
    }

    @Override
    protected SortedSetService<E> targetView() {
        return (SortedSetService<E>) super.targetView();
    }

}
