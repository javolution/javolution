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

import javolution.util.function.EqualityComparator;

/**
 * The standard comparator implementation.
 */
public class StandardComparatorImpl<E> implements EqualityComparator<E>,
        Serializable {

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
    public int compare(E left, E right) {
        if (left == right) return 0;
        if (left == null)
            return -1;
        if (right == null)
            return 1;
        if (left instanceof Comparable) 
            return ((Comparable<E>)left).compareTo(right);
        
        // Empirical method (consistent with equals).
        if (left.equals(right)) return 0;
        return left.hashCode() < right.hashCode() ? -1 : 1;
    }

}
