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
 * A view disallowing parallel processing
 */
public class SequentialCollectionImpl<E> extends FastCollection<E> {

	private static final long serialVersionUID = 0x700L; // Version.
	protected final FastCollection<E> inner;

	public SequentialCollectionImpl(FastCollection<E> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(E element) {
		return inner.add(element);
	}

	@Override
	public SequentialCollectionImpl<E> clone() {
		return new SequentialCollectionImpl<E>(inner.clone());
	}

	@Override
	public Equality<? super E> equality() {
		return inner.equality();
	}

	@Override
	public Iterator<E> iterator() {
		return inner.iterator();
	}
}
