/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Iterator;

import javolution.util.FastCollection;
import javolution.util.function.Equality;

/**
 * A view using a custom equality.
 */
public final class CustomEqualityCollectionImpl<E> extends FastCollection<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	private final FastCollection<E> inner;
	private final Equality<? super E> equality;

	public CustomEqualityCollectionImpl(FastCollection<E> inner,
			Equality<? super E> equality) {
		this.inner = inner;
		this.equality = equality;
	}

	@Override
	public boolean add(E element) {
		return inner.add(element);
	}

	@Override
	public void clear() { // Optimization.
		inner.clear();
	}

	@Override
	public CustomEqualityCollectionImpl<E> clone() {
		return new CustomEqualityCollectionImpl<E>(inner.clone(), equality);
	}

	@Override
	public Equality<? super E> equality() {
		return equality;
	}

	@Override
	public boolean isEmpty() { // Optimization.
		return inner.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return inner.iterator();
	}

	@Override
	public int size() { // Optimization.
		return inner.size();
	}

	@Override
	public CustomEqualityCollectionImpl<E> reversed() { // Optimization.
	    return new CustomEqualityCollectionImpl<E>(inner.reversed(), equality);
	}
	
	@Override
	public FastCollection<E>[] subViews(FastCollection<E>[] subViews) {
		inner.subViews(subViews);
		for (int i = 0; i < subViews.length; i++) {
			FastCollection<E> subView = subViews[i];
			if (subView == null) continue;
			subViews[i] = new CustomEqualityCollectionImpl<E>(subView, equality);
		}
		return subViews;
	}

}
