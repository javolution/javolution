/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.function;

import org.javolution.util.function.Order;

/**
 * The identity hash order implementation.
 * Enum-based singleton, ref. Effective Java Reloaded (Joshua Bloch). 
 */
public enum IdentityHashOrderImpl implements Order<Object> { 
	INSTANCE; 

	@Override
	public boolean areEqual(Object left, Object right) {
		return (left == right);
	}

	@Override
	public int compare(Object left, Object right) {
		int hashLeft = System.identityHashCode(left);
		int hashRight = System.identityHashCode(right);
        return (hashLeft == hashRight) ? 0 : 
        	((hashLeft  & 0xffffffffL) < (hashRight & 0xffffffffL)) ? -1 : 1;
	}

	@Override
	public int indexOf(Object object) { // Unsigned 32-bits
		return System.identityHashCode(object);
	}

	@Override
	public Order<Object> subOrder(Object obj) {
		return null; // No sub-order.
	}

}
