/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.comparator;


/**
 * The identity comparator implementation.
 */
public class IdentityComparatorImpl<E> extends StandardComparatorImpl<E> {

    private static final long serialVersionUID = -5186106439905062276L;

    @Override
    public int hashCodeOf(E obj) {
        return System.identityHashCode(obj);
    }

    @Override
    public boolean areEqual(E o1, E o2) {
        return o1 == o2;
    }

}
