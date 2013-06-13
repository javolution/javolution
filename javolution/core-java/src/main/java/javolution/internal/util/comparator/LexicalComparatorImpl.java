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

import javolution.lang.MathLib;
import javolution.util.function.FullComparator;

/**
 * The lexical comparator implementation.
 */
public class LexicalComparatorImpl implements FullComparator<CharSequence>, Serializable {

    @Override
    public int hashCodeOf(CharSequence csq) {
        if (csq == null) return -1;
        if (csq instanceof String) return ((String)csq).hashCode(); // String hashcode is cached. 
        int h = 0;
        for (int i = 0, n = csq.length(); i < n;) {
            h = 31 * h + csq.charAt(i++);
        }
        return h;
    }
    
    @Override
    public boolean areEqual(CharSequence csq1, CharSequence csq2) {
        if (csq1 == csq2) return true;
        if ((csq1 == null) || (csq2 == null)) return false;
        int n = csq1.length();
        if (csq2.length() != n) return false;
        for (int i = 0; i < n;) {
            if (csq1.charAt(i) != csq2.charAt(i++)) return false;
        }
        return true;
    }

    @Override
    public int compare(CharSequence left, CharSequence right) {
        int i = 0;
        int n = MathLib.min(left.length(), right.length());
        while (n-- != 0) {
            char c1 = left.charAt(i);
            char c2 = right.charAt(i++);
            if (c1 != c2)
                return c1 - c2;
        }
        return left.length() - right.length();
    }

    private static final long serialVersionUID = 1217966333277654645L;
}
