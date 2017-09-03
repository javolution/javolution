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
import org.javolution.util.function.Order;

/**
 * The identity hash order implementation (it also defines an order based on system hash value).
 */
public final class IdentityOrderImpl implements Order<Object> {
    private static final long serialVersionUID = 0x700L; // Version.

    @Override
    public boolean areEqual(@Nullable Object left, @Nullable Object right) {
        return (left == right);
    }

    @Override
    public int compare(Object left, Object right) {
        return 0;
    }

    @Override
    public int indexOf(Object object) { // Unsigned 32-bits
        return System.identityHashCode(object);
    }


}
