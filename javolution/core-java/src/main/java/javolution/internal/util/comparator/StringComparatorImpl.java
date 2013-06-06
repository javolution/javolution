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
 * The string comparator high-performance implementation.
 */
public final class StringComparatorImpl implements ComparatorService<String>, Serializable {

    @Override
    public int hashCodeOf(String str) {
        if (str == null) return -1;
        int n = str.length();
        if (n == 0) return 0;
        // Hash based on 5 characters only.
        return str.charAt(0) + str.charAt(n - 1) * 31
                + str.charAt(n >> 1) * 1009 + str.charAt(n >> 2) * 27583 
                + str.charAt(n - 1 - (n >> 2)) * 73408859;
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

    private static final long serialVersionUID = -4956453216020126658L;
}
