/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.comparator;

import java.util.Comparator;

/**
 * A comparator service wrapping a standard comparator.
 */
public final class WrapperComparatorImpl<E> extends StandardComparatorImpl<E> {

    private static final long serialVersionUID = 8775282553794347279L;
    private final Comparator<? super E> comparator;
    
    public WrapperComparatorImpl(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(E o1, E o2) {
        // Comparator should always be consistent with equals.
        if (areEqual(o1, o2)) return 0;
        return comparator.compare(o1, o2) < 0 ? -1 : 1;
    }   
}
