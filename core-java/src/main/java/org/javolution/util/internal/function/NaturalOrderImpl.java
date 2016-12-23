/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.function;

import org.javolution.util.function.Order;

/**
 * The natural order implementation.
 * Enum-based singleton, ref. Effective Java Reloaded (Joshua Bloch). 
 */
public enum NaturalOrderImpl implements Order<Comparable<?>> {
    INSTANCE;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean areEqual(Comparable left, Comparable right) {
        return left.compareTo(right) == 0;
    }

    @Override
    public int indexOf(Comparable<?> obj) {
        return 0;
    }

    @Override
    public Order<Comparable<?>> subOrder(Comparable<?> obj) {
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public int compare(Comparable left, Comparable right) {
        return left.compareTo(right);
    }


}
