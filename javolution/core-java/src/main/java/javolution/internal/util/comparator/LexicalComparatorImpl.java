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
 * The lexical comparator implementation.
 */
public final class LexicalComparatorImpl extends FastComparator<CharSequence> {

    @Override
    public int hashCodeOf(CharSequence csq) {
        if (csq == null)
            return 0;
        final int length = csq.length();
        if (length == 0)
            return 0;
        return csq.charAt(0) + csq.charAt(length - 1) * 31
                + csq.charAt(length >> 1) * 1009 + csq.charAt(length >> 2)
                * 27583 + csq.charAt(length - 1 - (length >> 2)) * 73408859;
    }

    @Override
    public boolean areEqual(CharSequence csq1, CharSequence csq2) {
        if ((csq1 == null) || (csq2 == null))
            return csq1 == csq2;
        final int length = csq1.length();
        if (csq2.length() != length)
            return false;
        for (int i = 0; i < length;) {
            if (csq1.charAt(i) != csq2.charAt(i++))
                return false;
        }
        return true;
    }

    @Override
    public int compare(CharSequence left, CharSequence right) {
        int i = 0;
        int n = Math.min(left.length(), right.length());
        while (n-- != 0) {
            char c1 = left.charAt(i);
            char c2 = right.charAt(i++);
            if (c1 != c2)
                return c1 - c2;
        }
        return left.length() - right.length();
    }

    private static final long serialVersionUID = -2258910672193027753L;
}
