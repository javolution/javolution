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
import java.util.Comparator;

import javolution.util.function.EqualityComparator;

/**
 * A comparator service wrapping a {@ link Comparator}, since 
 * consistency with hashcode cannot be maintained. The hashcode
 * calculation method throws UnsupportedOperationException. 
 */
public final class WrapperComparatorImpl<E> implements EqualityComparator<E>,
        Serializable {

    private static final long serialVersionUID = 8775282553794347279L;
    private final Comparator<? super E> comparator;

    public WrapperComparatorImpl(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int hashCodeOf(E obj) {
        throw new UnsupportedOperationException(
                "Standard comparator (java.util.Comparator) cannot be used for "
                        + "hashcode calculations; please use a coherent equality comparator "
                        + "instead (javolution.util.function.EqualityComparator).");
    }

    @Override
    public boolean areEqual(E e1, E e2) {
        return (e1 == e2) || (e1 != null && (comparator.compare(e1, e2) == 0));
    }

    @Override
    public int compare(E left, E right) {
        if (left == right)
            return 0;
        if (left == null)
            return -1;
        if (right == null)
            return 1;
        return comparator.compare(left, right);
    }
}
