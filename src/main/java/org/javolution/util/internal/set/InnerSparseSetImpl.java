/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import org.javolution.util.SparseSet;
import org.javolution.util.function.Order;
import org.javolution.util.internal.SparseArrayImpl;

/**
 * Sparse map inner implementation.
 */
public final class InnerSparseSetImpl<E> extends SparseSet<E> implements SparseArrayImpl.Inner<E,E> {

    private static final long serialVersionUID = 0x700L; // Version.

    public InnerSparseSetImpl(Order<? super E> order) {
        super(order);    
    }
    
    public InnerSparseSetImpl<E> clone() {
        return (InnerSparseSetImpl<E>)super.clone();
    }
}
