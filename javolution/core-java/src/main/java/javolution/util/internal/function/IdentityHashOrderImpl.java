/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.function;

import java.io.Serializable;

import javolution.util.function.Order;

/**
 * The identity hash order implementation.
 */
public final class IdentityHashOrderImpl<T> implements Order<T>, Serializable {

	private static final long serialVersionUID = 0x700L; // Version.
	public static final IdentityHashOrderImpl<Object> INSTANCE = new IdentityHashOrderImpl<Object>();

	@Override
	public boolean areEqual(T left, T right) {
		return (left == right);
	}

	@Override
	public int compare(T left, T right) {
		int hashLeft = System.identityHashCode(left);
		int hashRight = System.identityHashCode(right);
        return (hashLeft == hashRight) ? 0 : 
        	((hashLeft  & 0xffffffffL) < (hashRight & 0xffffffffL)) ? -1 : 1;
	}

	@Override
	public int indexOf(T object) { // Unsigned 32-bits
		return System.identityHashCode(object);
	}

	@Override
	public Order<T> subOrder(T obj) {
		return null; // No sub-order.
	}

}
