/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;


import org.javolution.util.FastCollection;
import org.javolution.util.ReadOnlyIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * An unmodifiable view over a collection.
 */
public final class UnmodifiableCollectionImpl<E> extends FastCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Unmodifiable View.";
    private final FastCollection<E> inner;

    public UnmodifiableCollectionImpl(FastCollection<E> inner) {
        this.inner = inner;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public UnmodifiableCollectionImpl<E> clone() {
        return new UnmodifiableCollectionImpl<E>(inner.clone());
    }

    @Override
    public Equality<? super E> equality() {
        return inner.equality();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public ReadOnlyIterator<E> iterator() {
        return ReadOnlyIterator.of(inner.iterator());
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public FastCollection<E>[] trySplit(int n) {
        return inner.trySplit(n); // Read-only views (see trySplit contract)
    }

}
