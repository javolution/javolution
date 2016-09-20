/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.function;

import org.javolution.lang.MathLib;
import org.javolution.util.function.Order;
/**
 * The lexicographic order implementation. Enum-based singleton, ref. Effective
 * Java Reloaded (Joshua Bloch).
 */
public enum CaseInsensitiveLexicalOrderImpl implements Order<CharSequence> {
	INSTANCE(0), INSTANCE_2(2), INSTANCE_4(4), INSTANCE_6(6), 
	INSTANCE_8(8), INSTANCE_10(10), INSTANCE_12(12), INSTANCE_14(14), 
	INSTANCE_16(16), INSTANCE_18(18), INSTANCE_20(20), INSTANCE_22(22),
	INSTANCE_24(24), INSTANCE_26(26), INSTANCE_28(28), INSTANCE_30(30);

	private final int fromIndex;

	/**
	 * Creates a lexical order from the specified index. Anything before that
	 * index is ignored.
	 */
	private CaseInsensitiveLexicalOrderImpl(int fromIndex) {
		this.fromIndex = fromIndex;
	};

	@Override
	public boolean areEqual(CharSequence left, CharSequence right) {
		return areEqual(fromIndex, left, right);
	}

	@Override
	public int compare(CharSequence left, CharSequence right) {
		return compare(fromIndex, left, right);
	}

	@Override
	public int indexOf(CharSequence csq) {
		return indexOf(fromIndex, csq);
	}

    @Override
    public Order<CharSequence> subOrder(CharSequence csq) {
        int newIndex = fromIndex + 2;
        return newIndex <= 30 ? LexicalOrderImpl.values()[newIndex >> 1] : new DynamicImpl(newIndex);
    }
			
	/** Dynamic implementation for long character sequences (not an enum) **/
	private static class DynamicImpl implements Order<CharSequence> {
		private static final long serialVersionUID = 0x700L; // Version.
		private final int fromIndex;

		private DynamicImpl(int fromIndex) {
			this.fromIndex = fromIndex;
		}

	    @Override
	    public boolean areEqual(CharSequence left, CharSequence right) {
	        return CaseInsensitiveLexicalOrderImpl.areEqual(fromIndex, left, right);
	    }

	    @Override
	    public int compare(CharSequence left, CharSequence right) {
	        return CaseInsensitiveLexicalOrderImpl.compare(fromIndex, left, right);
	    }

	    @Override
	    public int indexOf(CharSequence csq) {
	        return CaseInsensitiveLexicalOrderImpl.indexOf(fromIndex, csq);
	    }

	    @Override
	    public Order<CharSequence> subOrder(CharSequence csq) {
	        return new DynamicImpl(fromIndex + 2);
	    }

	}

    /** Check for equality of the two characters sequences starting at the specified index */
    private static boolean areEqual(int fromIndex, CharSequence left, CharSequence right) {
        if (left == right)
            return true;
        if ((left == null) || (right == null))
            return false;
        int n = left.length();
        if (right.length() != n)
            return false;
        for (int i = n; i > fromIndex;) { // Search from the tail.
            if (Character.toUpperCase(left.charAt(--i)) != Character.toUpperCase(right.charAt(i)))
                return false;
        }
        return true;
    }

    /** Compares the two characters sequences starting at the specified index */
    private static int compare(int fromIndex, CharSequence left, CharSequence right) {
        if (left == null)
            return -1;
        if (right == null)
            return 1;
        for (int i = fromIndex, n = MathLib.min(left.length(),right.length()); i < n; i++) {
            char c1 = Character.toUpperCase(left.charAt(i));
            char c2 = Character.toUpperCase(right.charAt(i));
            if (c1 != c2)
                return c1 - c2;
        }
        return left.length() - right.length();
    }

    /** Returns the index starting at the specified index (two characters at a time).*/
    private static int indexOf(int fromIndex, CharSequence csq) {
        if (csq == null)
            return 0;
        int length = csq.length();
        int j = fromIndex;
        int i = (j < length) ? Character.toUpperCase(csq.charAt(j++)) : 0;
        i <<= 16;
        i |= (j < length) ? Character.toUpperCase(csq.charAt(j++)) : 0;
        return i;
    }
}
