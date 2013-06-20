/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.comparator;

/**
 * The high-performance lexical comparator.
 */
public class LexicalFastComparatorImpl extends LexicalComparatorImpl {

    private static final long serialVersionUID = -1449702752185594025L;

    @Override
    public int hashCodeOf(CharSequence csq) {
        if (csq == null)
            return 0;
        int n = csq.length();
        if (n == 0)
            return 0;
        // Hash based on 5 characters only.
        return csq.charAt(0) + csq.charAt(n - 1) * 31 + csq.charAt(n >> 1)
                * 1009 + csq.charAt(n >> 2) * 27583
                + csq.charAt(n - 1 - (n >> 2)) * 73408859;
    }
}
