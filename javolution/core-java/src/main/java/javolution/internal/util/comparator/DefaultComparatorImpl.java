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
 * The default comparator implementation.
 */
public final class DefaultComparatorImpl<E> implements ComparatorService<E>, Serializable {

     @Override
    public int hashCodeOf(E obj) {
        return (obj == null) ? 0 : obj.hashCode();
    }

    @Override
    public boolean areEqual(E o1, E o2) {
        return (o1 == null) ? (o2 == null) : (o1 == o2) || o1.equals(o2);        
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(E o1, E o2) {
        return ((Comparable<E>) o1).compareTo(o2);
    }

    private static final long serialVersionUID = -8001529317496419213L;
}
