/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set;

import javolution.util.function.Predicate;
import javolution.util.internal.collection.FilteredCollectionImpl;
import javolution.util.service.SetService;

/**
 * A filtered view over a set.
 */
public class FilteredSetImpl<E> extends FilteredCollectionImpl<E> implements
        SetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public FilteredSetImpl(SetService<E> target, Predicate<? super E> filter) {
        super(target, filter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SetService<E>[] split(int n, boolean updateable) {
        SetService<E>[] subTargets = target().split(n, updateable);
        SetService<E>[] result = new SetService[subTargets.length];
        for (int i = 0; i < subTargets.length; i++) {
            result[i] = new FilteredSetImpl<E>(subTargets[i], filter);
        }
        return result;
    } 

    /** Returns the actual target */
    @Override
    protected SetService<E> target() {
        return (SetService<E>) super.target();
    }
}
