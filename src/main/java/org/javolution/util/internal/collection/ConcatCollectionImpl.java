/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;


import org.javolution.util.AbstractCollection;
import org.javolution.util.FastIterator;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A view resulting of the concatenation of two collections.
 */
public final class ConcatCollectionImpl<E> extends AbstractCollection<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private final AbstractCollection<E> first;
    private final AbstractCollection<? extends E> second;

    public ConcatCollectionImpl(AbstractCollection<E> first, AbstractCollection<? extends E> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return first.removeIf(filter) | second.removeIf(filter);
    }

    @Override
    public FastIterator<E> iterator() {
        return new FastIterator<E>() {
            FastIterator<E> firstItr = first.iterator();
            FastIterator<? extends E> secondItr = second.iterator();

            @Override
            public boolean hasNext() {
                return firstItr.hasNext() || secondItr.hasNext();
            }

            @Override
            public boolean hasNext(Predicate<? super E> matching) {
                return firstItr.hasNext(matching) || secondItr.hasNext(matching);
            }

            @Override
            public E next() {
                return firstItr.hasNext() ? firstItr.next() : secondItr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }};
    }

    @Override
    public FastIterator<E> descendingIterator() {
        return new FastIterator<E>() {
            FastIterator<E> firstItr = first.descendingIterator();
            FastIterator<? extends E> secondItr = second.descendingIterator();

            @Override
            public boolean hasNext() {
                return secondItr.hasNext() || firstItr.hasNext();
            }

            @Override
            public boolean hasNext(Predicate<? super E> matching) {
                return secondItr.hasNext(matching) || firstItr.hasNext(matching);
            }

            @Override
            public E next() {
                return secondItr.hasNext() ? secondItr.next() : firstItr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }};
    }

    @Override
    public boolean add(E element) {
        return first.add(element); 
    }

    @Override
    public boolean isEmpty() {
        return first.isEmpty() && second.isEmpty();
    }

    @Override
    public int size() {
        return first.size() + second.size();
    }

    @Override
    public void clear() {
        first.clear();
        second.clear();
    }

    @Override
    public Equality<? super E> equality() {
        return first.equality();
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractCollection<E>[] trySplit(int n) {
        return new AbstractCollection[] { first, second };
    }
    
}
