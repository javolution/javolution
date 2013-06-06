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
 * The case insensitive lexical comparator implementation.
 */
public final class LexicalCaseInsensitiveComparatorImpl implements ComparatorService<CharSequence>, Serializable {

    @Override
    public int hashCodeOf(CharSequence csq) {
        if (csq == null) return -1;
        int n = csq.length();
        if (n == 0) return 0;
        return up(csq.charAt(0)) + up(csq.charAt(n - 1)) * 31
                + up(csq.charAt(n >> 1)) * 1009 + up(csq.charAt(n >> 2))
                * 27583 + up(csq.charAt(n - 1 - (n >> 2))) * 73408859;
    }
    
    @Override
    public boolean areEqual(CharSequence csq1, CharSequence csq2) {
        if ((csq1 == null) || (csq2 == null))
            return csq1 == csq2;
        int n = csq1.length();
        if (csq2.length() != n)
            return false;
        for (int i = 0; i < n;) {
            if (up(csq1.charAt(i)) != up(csq2.charAt(i++)))
                return false;
        }
        return true;
    }

    @Override
    public int compare(CharSequence left, CharSequence right) {
        int i = 0;
        int n = Math.min(left.length(), right.length());
        while (n-- != 0) {
            char c1 = up(left.charAt(i));
            char c2 = up(right.charAt(i++));
            if (c1 != c2)
                return c1 - c2;
        }
        return left.length() - right.length();
    }

    // Converts to upper case.
    private static char up(char c) {
        return Character.toUpperCase(c);
    }
    
    private static final long serialVersionUID = 7232021322902740242L;  
}
