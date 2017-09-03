/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.function;

import org.javolution.annotations.Nullable;
import org.javolution.lang.Index;
import org.javolution.lang.MathLib;
import org.javolution.util.function.Order;

/**
 * The index order default implementation.
 */
public final class IndexOrderImpl implements Order<Index> {
    private static final long serialVersionUID = 0x700L; // Version.
   
    @Override
    public boolean areEqual(@Nullable Index left, @Nullable Index right) {
        return (left == right) || (left != null && left.equals(right));
    }

    @Override
    public int compare(Index left, Index right) {
        int leftValue = left.intValue();
        int rightValue = right.intValue();
        return (leftValue == rightValue) ? 0 : MathLib.unsignedLessThan(leftValue, rightValue) ? -1 : 1;
    }

    @Override
    public int indexOf(Index index) { // Unsigned 32-bits
        return index.intValue();
    }

}
