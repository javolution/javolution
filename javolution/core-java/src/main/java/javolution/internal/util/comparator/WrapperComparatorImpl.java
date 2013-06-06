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

import javolution.util.service.ComparatorService;

/**
 * A comparator service wrapping a standard comparator.
 */
public final class WrapperComparatorImpl<E> implements ComparatorService<E>, Serializable {

    private final Comparator<? super E> comparator;
    
    public WrapperComparatorImpl(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }
    
    public Comparator<? super E> getComparator() {
        return comparator;
    }    
    
    @Override
    public int hashCodeOf(Object obj) {
        return obj.hashCode();
    }

    @Override
    public boolean areEqual(E o1, E o2) {
        return (o1 == null) ? (o2 == null) : (o1 == o2) || o1.equals(o2);
    }

    @Override
    public int compare(E o1, E o2) {
        return comparator.compare(o1, o2);
    }   

    private static final long serialVersionUID = -2181380624939754736L;
}
