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
 * The standard equality implementation (it also defines an order based on hash
 * value).
 */
public final class StandardOrderImpl extends Order<Object> {
	private static final long serialVersionUID = 0x700L; // Version.

	@Override
	public boolean areEqual(@Nullable Object left, @Nullable Object right) {
		return (left == right) || (left != null && left.equals(right));
	}

	@Override
	public int compare(@Nullable Object left, @Nullable Object right) {
		long leftIndex = indexOf(left);
		long rightIndex = indexOf(right);
		if (leftIndex == rightIndex)
			return 0;
		return MathLib.unsignedLessThan(leftIndex, rightIndex) ? -1 : 1;
	}

	@Override
	public long indexOf(@Nullable Object object) {
		return (object != null) ? object.hashCode() : 0;
	}

}
