/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.function;

import org.javolution.lang.Index;
import org.javolution.lang.MathLib;
import org.javolution.util.function.Order;

/**
 * The index order implementation.
 * Enum-based singleton, ref. Effective Java Reloaded (Joshua Bloch). 
 */
public enum IndexOrderImpl implements Order<Index> {
    INSTANCE;

    @Override
    public boolean areEqual(Index left, Index right) {
        return left.intValue() == right.intValue();
    }

    @Override
    public int compare(Index left, Index right) {
        int leftValue = left.intValue();
        int rightValue = right.intValue();
        return (leftValue == rightValue) ? 0 : MathLib.unsigned(leftValue) < MathLib.unsigned(rightValue) ? -1 : 1;
    }

    @Override
    public int indexOf(Index index) { // Unsigned 32-bits
        return index.intValue();
    }

    @Override
    public Order<Index> subOrder(Index index) {
        return null; // No sub-order.
    }

}
