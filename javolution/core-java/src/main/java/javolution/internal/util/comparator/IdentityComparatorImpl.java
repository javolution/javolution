/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.comparator;

import javolution.util.FastComparator;

/**
 * The lexical comparator implementation.
 */
public final class IdentityComparatorImpl<E> extends FastComparator<E> {

    @Override
    public int hashCodeOf(Object obj) {
        return System.identityHashCode(obj);
    }

    @Override
    public boolean areEqual(Object o1, Object o2) {
        return o1 == o2;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2) {
        return ((Comparable<Object>) o1).compareTo(o2);
    }   

    private static final long serialVersionUID = -5453435627116001181L;
}
