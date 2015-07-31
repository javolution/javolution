/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.comparator;

import javolution.lang.MathLib;
import javolution.text.CharArray;

/**
 * The CharArray comparator implementation
 */
public class CharArrayComparatorImpl extends StandardComparatorImpl<CharArray> {

	private static final long serialVersionUID = 0x620L; // Version.

	@Override
	public boolean equal(final CharArray charArray1, final CharArray charArray2) {
		if(charArray1 == null) return false;
		return charArray1.equals(charArray2);
	}

	@Override
	public int compare(final CharArray left, final CharArray right) {
		if (left == null)
			return -1;
		if (right == null)
			return 1;

		int i = 0;
		int n = MathLib.min(left.length(), right.length());

		while (n-- != 0) {
			final char c1 = left.charAt(i);
			final char c2 = right.charAt(i++);
			if (c1 != c2)
				return c1 - c2;
		}
		return left.length() - right.length();
	}

	@Override
	public int hashOf(final CharArray charArray) {
		return charArray.hashCode();
	}

}
