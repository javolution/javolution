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
 * The lexicographic order default implementation (four UTF-16 characters at a time).
 * 
 */
public final class LexicalOrderImpl extends Order<CharSequence> {
    private static final long serialVersionUID = 0x700L; // Version.
    private int startIndex;
    
    public LexicalOrderImpl(int startIndex) {
    	this.startIndex = startIndex;
    }    
    
    @Override
    public boolean areEqual(@Nullable CharSequence left, @Nullable CharSequence right) {
        if (left == right) return true;
        if ((left == null) || (right == null)) return false;
        int n = left.length();
        if (right.length() != n) return false;
        for (int i = n; i > startIndex;) // Iterates from tail.
            if (left.charAt(--i) != right.charAt(i)) return false;
        return true;
    }

    @Override
    public int compare(@Nullable CharSequence left, @Nullable CharSequence right) {
    	if (left == null) return -1;
    	if (right == null) return 1;
        for (int i = startIndex, n = MathLib.min(left.length(), right.length()); i < n; i++) {
            char c1 = left.charAt(i);
            char c2 = right.charAt(i);
            if (c1 != c2) return c1 - c2;
        }
        return left.length() - right.length();
    }

    @Override
    public long indexOf(@Nullable CharSequence csq) {
    	if (csq == null) return 0;
        int length = csq.length();
        int index = 0;
        for (int i = startIndex, n = MathLib.min(startIndex + 4, length); i < n;) 
        	index = (index << 16) | csq.charAt(i++);
        return index;	
    }

    @Override
    public LexicalOrderImpl subOrder(@Nullable CharSequence csq) {
        return (csq != null) && csq.length() > startIndex + 4 ? 
        		new LexicalOrderImpl(startIndex + 4) : null;
    }
}
