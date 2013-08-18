/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set;

import javolution.util.internal.collection.CollectionView;
import javolution.util.service.SetService;

/**
 * Set view implementation; can be used as root class for implementations 
 * if target is {@code null}.
 */
public abstract class SetView<E> extends CollectionView<E> implements SetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * The view constructor or root class constructor if target is {@code null}.
     */
    public SetView(SetService<E> target) {
        super(target);
    }

    @Override
    public abstract int size();

    @Override
    public abstract boolean contains(Object o);

    @Override
    public abstract boolean remove(Object o);

    @Override
    public SetService<E> threadSafe() {
        return new SharedSetImpl<E>(this);
    }

    /** Returns the actual target */
    @Override
    protected SetService<E> target() {
        return (SetService<E>) super.target();
    }

    
}
