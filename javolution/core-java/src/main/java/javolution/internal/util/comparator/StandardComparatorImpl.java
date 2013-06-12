/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.comparator;

import java.io.Serializable;

import javolution.util.service.ComparatorService;

/**
 * The standard comparator implementation.
 */
public class StandardComparatorImpl<E> implements ComparatorService<E>, Serializable {

    private static final long serialVersionUID = -615690677813206151L;

    @Override
    public int hashCodeOf(E e) {
        return (e == null) ? 0 : e.hashCode();
    }

    @Override
    public boolean areEqual(E e1, E e2) {
        return (e1 == e2) || (e1 != null && e1.equals(e2));
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(E o1, E o2) {
        // Comparator should always be consistent with equals.
        if (areEqual(o1, o2)) return 0;
        if (o1 instanceof Comparable) 
            return ((Comparable<E>) o1).compareTo(o2) < 0 ? -1 : 1;
        return (hashCodeOf(o1) < hashCodeOf(o2)) ? -1 :  1;
    }
}
