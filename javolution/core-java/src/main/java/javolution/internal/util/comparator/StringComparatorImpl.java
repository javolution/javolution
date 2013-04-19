/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.comparator;

import javolution.util.FastComparator;

/**
 * The string comparator implementation.
 */
public final class StringComparatorImpl extends FastComparator<String> {


    @Override
    public int hashCodeOf(String str) {
        return (str != null) ? str.hashCode() : 0;
    }

    @Override
    public boolean areEqual(String str1, String str2) {
        return (str1 == null) ? (str2 == null) : (str1 == str2)
                || str1.equals(str2);
    }

    @Override
    public int compare(String left, String right) {
        return left.compareTo(right);
    }

    private static final long serialVersionUID = -9139132919876170673L;
}
