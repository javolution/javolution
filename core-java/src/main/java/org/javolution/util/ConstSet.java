/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2014 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.util.Collection;
import java.util.Iterator;

import org.javolution.annotations.ReadOnly;
import org.javolution.lang.Immutable;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.SparseArrayDescendingIteratorImpl;
import org.javolution.util.internal.SparseArrayIteratorImpl;

/**
 * <p> A set for which immutability is guaranteed by construction.
 * 
 * <pre>{@code
 * // Creation from literal elements.
 * ConstSet<String> winners = ConstSet.of("John Deuff", "Otto Graf", "Sim Kamil");
 * 
 * // From existing collections.
 * ConstSet<String> caseInsensitiveWinners = ConstSet.of(LEXICAL_CASE_INSENSITIVE, winners);
 * }</pre></p>
 * 
 * <p> This class ensures that calling a method which may modify the set will generate a deprecated warning
 *     at compile time and will raise an exception at run-time.</p>
 *     
 * @param <E> the immutable set element (cannot be {@code null})
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
@ReadOnly
public final class ConstSet<E> extends FastSet<E> implements Immutable {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Constant sets cannot be modified.";
    private static final ConstSet<?> EMPTY = new ConstSet<Object>(Order.DEFAULT);

    /**
     * Returns a constant empty set. 
     */
    @SuppressWarnings("unchecked")
    public static <E> ConstSet<E> empty() {
        return (ConstSet<E>) EMPTY;
    }

    /**
     * Returns a constant set (hash-ordered) holding the elements from the specified collection.
     */
    public static <E> ConstSet<E> of(Collection<? extends E> elements) {
        return new ConstSet<E>(Order.DEFAULT, elements);
    }
    /**
     * Returns a constant set (hash-ordered) holding the specified elements. 
     */
    public static <E> ConstSet<E> of(E... elements) {
        return new ConstSet<E>(Order.DEFAULT, elements);
    }
    /**
     * Returns a constant set sorted using the specified order and holding the elements from the specified collection.
     */
    public static <E> ConstSet<E> of(Order<? super E> order, Collection<? extends E> elements) {
        return new ConstSet<E>(order, elements);
    }

    /**
     * Returns a constant set sorted using the specified order and holding the specified elements.
     */
    public static <E> ConstSet<E> of(Order<? super E> order, E... elements) {
        return new ConstSet<E>(order, elements);
    }

    private final Order<? super E> order;

    private final SparseArray<Object> array;

    private final int size;

    /** Creates a constant set from the specified collection.*/
    private ConstSet(Order<? super E> order, Collection<? extends E> elements) {
        SparseSet<E> sparse = new SparseSet<E>(order);
        for (E e : elements)
            sparse.add(e);
        this.order = order;
        this.array = sparse.array;
        this.size = sparse.size();
    }

    /** Creates a constant set holding the specified elements.*/
    private ConstSet(Order<? super E> order, E... elements) {
        SparseSet<E> sparse = new SparseSet<E>(order);
        for (E e : elements)
            sparse.add(e);
        this.order = order;
        this.array = sparse.array;
        this.size = sparse.size();
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public boolean addAll(Collection<? extends E> that) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public boolean addAll(E... elements) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /**  Returns {@code this}.*/
    @Override
    public ConstSet<E> clone() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object element) {
        int index = order.indexOf((E) element);
        Object obj = array.get(index);
        if (obj == null)
            return false;
        if (SparseSet.isInner(obj))
            return ((FastSet<E>) obj).contains(element);
        return order.areEqual((E) element, (E) obj) ? true : false;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new SparseArrayDescendingIteratorImpl<E, E>(array) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException(ERROR_MSG);
            }
        };
    }

    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        return new SparseArrayDescendingIteratorImpl<E, E>(array, fromElement, order, false) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException(ERROR_MSG);
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new SparseArrayIteratorImpl<E, E>(array) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException(ERROR_MSG);
            }
        };
    }

    @Override
    public Iterator<E> iterator(E fromElement) {
        return new SparseArrayIteratorImpl<E, E>(array, fromElement, order, false) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException(ERROR_MSG);
            }
        };
    }

    @Override
    public Order<? super E> order() {
        return order;
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public E pollLast() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public boolean removeAll(Collection<?> that) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the set unmodified.
     * @deprecated Should never be used on immutable set.
     */
    @Override
    public boolean retainAll(Collection<?> that) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public int size() {
        return size;
    }

    /** Returns {@code this}.*/
    @Override
    public ConstSet<E> unmodifiable() {
        return this;
    }

}