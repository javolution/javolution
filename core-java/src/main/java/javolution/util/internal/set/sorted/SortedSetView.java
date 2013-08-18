/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set.sorted;

import javolution.util.internal.set.SetView;
import javolution.util.service.SortedSetService;

/**
 * Sorted Set view implementation; can be used as root class for implementations 
 * if target is {@code null}.
 */
public abstract class SortedSetView<E> extends SetView<E> implements SortedSetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * The view constructor or root class constructor if target is {@code null}.
     */
    public SortedSetView(SortedSetService<E> target) {
        super(target);
    }

    @Override
    public abstract E first();

    @Override
    public SortedSetService<E> headSet(E toElement) {
        return new SubSortedSetImpl<E>(this, null, toElement);
    }

    @Override
    public abstract E last();

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
        return new SharedSortedSetImpl<E>(this);
    }

    @Override
    protected SortedSetService<E> target() {
        return (SortedSetService<E>) super.target();
    }

}
