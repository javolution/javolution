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
 * The case insensitive lexicographic order default implementation.
 * 
 * This implementation calculates ordered indices from up to seven first characters 
 * (compression using English letter frequency).
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Letter_frequency">Wikipedia: Letter Frequency</a>
  */
public final class CaseInsensitiveLexicalOrderImpl implements Order<CharSequence> {
    
    private static final long serialVersionUID = 0x700L; // Version.

    // Characters 'J', 'X', 'Q', 'Z' have the same indices as their previous character (low frequency characters).
    private static final int[] ENCODING_5_BITS = { 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,    
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,    
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,    
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 10, 10, // '0' ...  
            10, 10, 11, 12, 13, 14, 15, 16, 17, 18, 18 /*J*/, 19, 20, 21, 22, 23,  // '@', 'A', ...   
            24, 24 /*Q*/, 25, 26, 27, 28, 29, 30, 30 /*X*/, 31, 31 /*Z*/, 31, 31, 31, 31, 31, // 'P' ...   
            31, 10, 11, 12, 13, 14, 15, 16, 17, 18, 18 /*j*/, 19, 20, 21, 22, 23, // ''', 'a' ...  
            24, 24 /*q*/, 25, 26, 27, 28, 29, 30, 30 /*x*/, 31, 31 /*z*/, 31, 31, 31, 31, 31 }; // 'p' ...
    
    @Override
    public boolean areEqual(@Nullable CharSequence left, @Nullable CharSequence right) {
        if (left == right) return true;
        if ((left == null) || (right == null)) return false;
        int n = left.length();
        if (right.length() != n) return false;
        for (int i = n; i > 0;)  // Iterates from tail.
            if (Character.toUpperCase(left.charAt(--i)) != Character.toUpperCase(right.charAt(i))) return false;
        return true;
    }

    @Override
    public int compare(CharSequence left, CharSequence right) {
        for (int i = 0, n = MathLib.min(left.length(), right.length()); i < n; i++) {
            char c1 = Character.toUpperCase(left.charAt(i));
            char c2 = Character.toUpperCase(right.charAt(i));
            if (c1 != c2) return c1 - c2;
        }
        return left.length() - right.length();
    }

    @Override
    public int indexOf(CharSequence csq) {
        int length = csq.length();
        for (int i = 0, index = 0;;) {
            if (i >= length) return index; // No more character to read.
            if (i == 6) return index | (encoding5bits(csq.charAt(6)) >> 4); // Only 2 bits left for 7th character.
            index |= encoding5bits(csq.charAt(i++)) << (32 - (5 * i));
        }
    }

    private static final int encoding5bits(char c) { // Must preserve lexical order.
        return c < 128 ? ENCODING_5_BITS[c] : (c = Character.toUpperCase(c)) < 128 ?
            ENCODING_5_BITS[c] : 31;
    }

}
