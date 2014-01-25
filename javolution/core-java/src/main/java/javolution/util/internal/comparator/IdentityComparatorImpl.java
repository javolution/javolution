/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.comparator;


/**
 * The identity comparator implementation.
 */
public class IdentityComparatorImpl<E> extends StandardComparatorImpl<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    @Override
    public boolean equal(E e1, E e2) {
        return e1 == e2;
    }

    @Override
    public int hashCodeOf(E obj) {
        return System.identityHashCode(obj);
    }
    
}
