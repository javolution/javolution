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
import org.javolution.lang.MathLib;
import org.javolution.util.function.Order;

/**
 * The lexicographic order default implementation.
 * 
 * This implementation calculates ordered indices from up to six first characters 
 * (compression using English letter frequency).
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Letter_frequency">Wikipedia: Letter Frequency</a>
 */
public final class LexicalOrderImpl implements Order<CharSequence> {
    private static final long serialVersionUID = 0x700L; // Version.
    
    private static final int[] ENCODING_6_BITS = {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,    
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,    
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,    
         0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 10, 10,            // '0' ...  
         10, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,   // '@', 'A', ...   
         25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 36, 36, 36, 36,   // 'P' ...   
         36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,   // ''', 'a' ...  
         52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 63, 63, 63, 63 }; // 'p' ...

    @Override
    public boolean areEqual(@Nullable CharSequence left, @Nullable CharSequence right) {
        if (left == right) return true;
        if ((left == null) || (right == null)) return false;
        int n = left.length();
        if (right.length() != n) return false;
        for (int i = n; i > 0;) // Iterates from tail.
            if (left.charAt(--i) != right.charAt(i)) return false;
        return true;
    }

    @Override
    public int compare(CharSequence left, CharSequence right) {
        for (int i = 0, n = MathLib.min(left.length(), right.length()); i < n; i++) {
            char c1 = left.charAt(i);
            char c2 = right.charAt(i);
            if (c1 != c2) return c1 - c2;
        }
        return left.length() - right.length();
    }

    @Override
    public int indexOf(CharSequence csq) {
        int length = csq.length();
        for (int i = 0, index = 0;;) {
            if (i >= length) return index; // No more character to read.
            if (i == 5) return index | (encoding6bits(csq.charAt(5)) >> 4); // Only 2 bits left for 6th character.
            index |= encoding6bits(csq.charAt(i++)) << (32 - (6 * i));
        }
    }

    private static final int encoding6bits(int c) { // Must preserve lexical order.
        return c < 128 ? ENCODING_6_BITS[c] : 63;
    }
}
