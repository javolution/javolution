/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.set;

import java.io.Serializable;
import java.util.Iterator;

import javolution.internal.util.UnmodifiableIteratorImpl;
import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.SetService;

/**
 * An unmodifiable view over a set.
 */
public class UnmodifiableSetImpl<E> implements SetService<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final SetService<E> target;

    public UnmodifiableSetImpl(SetService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public boolean contains(E e) {
        return target.contains(e);
    }

    @Override
    public void forEach(Consumer<? super E> consumer, IterationController controller) {
        target.forEach(consumer, controller);
    }

    @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIteratorImpl<E>(target.iterator());
    }

    @Override
    public boolean remove(E e) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter, IterationController controller) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public UnmodifiableCollectionImpl<E>[] trySplit(int n) {
        return UnmodifiableCollectionImpl.splitOf(this, n);
    }

    @Override
    public void atomic(Runnable update) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    protected SetService<E> target() {
        return target;
    }
    
}
