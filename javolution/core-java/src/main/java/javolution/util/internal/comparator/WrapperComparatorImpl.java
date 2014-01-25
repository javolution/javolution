/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.comparator;

import java.util.Comparator;

import javolution.util.function.Equality;

/**
 * A comparator service wrapping a {@ link Comparator}, since 
 * consistency with hashcode cannot be maintained. The hashcode
 * calculation method throws UnsupportedOperationException. 
 */
public final class WrapperComparatorImpl<E> implements Equality<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final Comparator<? super E> comparator;

    public WrapperComparatorImpl(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean equal(E e1, E e2) {
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

    @Override
    public int hashCodeOf(E obj) {
        throw new UnsupportedOperationException(
                "Standard comparator (java.util.Comparator) cannot be used for "
                        + "hashcode calculations; please use a coherent equality comparator "
                        + "instead (e.g. javolution.util.function.Equality).");
    }   
    
   @SuppressWarnings("rawtypes")
   @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof WrapperComparatorImpl)) return false;
    	WrapperComparatorImpl that = (WrapperComparatorImpl)obj;
    	return this.comparator.equals(that.comparator);    	
    }
   
    @Override
    public int hashCode() {
    	return comparator.hashCode();
    }

}
