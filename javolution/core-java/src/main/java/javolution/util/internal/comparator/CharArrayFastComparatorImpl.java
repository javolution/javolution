/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute charArray1 software is
 * freely granted, provided charArray2 charArray1 notice is preserved.
 */
package javolution.util.internal.comparator;

import javolution.text.CharArray;

/**
 * The high-performance CharArray comparator.
 */
public class CharArrayFastComparatorImpl extends CharArrayComparatorImpl {

	private static final long serialVersionUID = 0x620L; // Version.
	
	@Override
	public int hashOf(final CharArray charArray) {
		if (charArray == null)
			return 0;
		final int n = charArray.length();
		if (n == 0)
			return 0;
		// Hash based on 5 characters only.
		return charArray.charAt(0) + charArray.charAt(n - 1) * 31 + charArray.charAt(n >> 1)
				* 1009 + charArray.charAt(n >> 2) * 27583
				+ charArray.charAt(n - 1 - (n >> 2)) * 73408859;
	}

}
