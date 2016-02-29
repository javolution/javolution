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
 * The natural order implementation.
 */
public final class NaturalOrderImpl<T> implements Order<T>, Serializable {

	private static final long serialVersionUID = 0x700L; // Version.
	public static final NaturalOrderImpl<Object> INSTANCE = new NaturalOrderImpl<Object>();

	@Override
	public boolean areEqual(T left, T right) {
		return compare(left, right) == 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compare(T left, T right) {
		return (left == null) ? (right == null ? 0 : -1) : (right == null ? 1
				: ((Comparable<T>) left).compareTo(right));
	}

	@Override
	public int indexOf(T object) {
		return 0; // Cannot define a range in the general case.
	}

	@Override
	public Order<T> subOrder(T obj) {
		return null;
	}


}
