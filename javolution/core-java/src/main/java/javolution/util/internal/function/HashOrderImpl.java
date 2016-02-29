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
 * The hash order implementation.
 */
public final class HashOrderImpl<T> implements Order<T>, Serializable {

	private static final long serialVersionUID = 0x700L; // Version.
	public static final HashOrderImpl<Object> INSTANCE = new HashOrderImpl<Object>();

	@Override
	public boolean areEqual(T left, T right) {
		return (left == right) || (left != null && left.equals(right));
	}

	@Override
	public int compare(T left, T right) {
		int hashLeft = (left != null) ? left.hashCode() : 0;
		int hashRight = (right != null) ? right.hashCode() : 0;
		return (hashLeft == hashRight) ? 0
				: ((hashLeft & 0xffffffffL) < (hashRight & 0xffffffffL)) ? -1
						: 1;
	}

	@Override
	public int indexOf(T object) { // Unsigned 32-bits
		return (object != null) ? object.hashCode() : 0;
	}

	@Override
	public Order<T> subOrder(T obj) {
		return null; // No sub-order.
	}

}
