/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.set.sorted;

import javolution.internal.util.set.UnmodifiableSetImpl;
import javolution.util.service.SortedSetService;

/**
 * An unmodifiable view over a set.
 */
public class UnmodifiableSortedSetImpl<E> extends UnmodifiableSetImpl<E> implements 
     SortedSetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
   
    public UnmodifiableSortedSetImpl(SortedSetService<E> target) {
        super(target);
    }

    @Override
    public E first() {
        return target().first();
    }

    @Override
    public E last() {
        return target().last();
    }

    @Override
    public SortedSetService<E> subSet(E fromElement, E toElement) {
        return target().subSet(fromElement, toElement);
    }
    
    @Override
    protected SortedSetService<E> target() {
        return (SortedSetService<E>)super.target();
    }
}
