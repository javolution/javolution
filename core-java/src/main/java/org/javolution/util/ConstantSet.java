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

import org.javolution.lang.Constant;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * <p> A set for which immutability is guaranteed by construction.
 * 
 * <pre>{@code
 * // From literal elements.
 * ConstantSet<String> winners = ConstantSet.of("John Deuff", "Otto Graf", "Sim Kamil");
 * 
 * // From existing collections.
 * ConstantSet<String> caseInsensitiveWinners = ConstantSet.of(LEXICAL_CASE_INSENSITIVE, winners);
 * }</pre></p>
 * 
 * <p> This class ensures that calling a method which may modify the set will most likely generate a warning
 *     (deprecated warning) at compile time and will most certainly raise an exception at run-time.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
@Constant
public final class ConstantSet<E> extends FastSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Constant sets cannot be modified.";

    /**
     * Returns a constant set (hash-ordered) holding the elements from the specified collection.
     */
    public static <E> ConstantSet<E> of(Collection<? extends E> elements) {
        SparseSet<E> sparse = new SparseSet<E>();
        for (E e : elements)
            sparse.add(e);
        return new ConstantSet<E>(sparse);
    }

    /**
     * Returns a constant set (hash-ordered) holding the specified elements. 
     */
    public static <E> ConstantSet<E> of(E... elements) {
        SparseSet<E> sparse = new SparseSet<E>();
        for (E e : elements)
            sparse.add(e);
        return new ConstantSet<E>(sparse);
    }

    /**
     * Returns a constant set sorted using the specified order and holding the elements from the specified collection.
     */
    public static <E> ConstantSet<E> of(Order<? super E> order, Collection<? extends E> elements) {
        SparseSet<E> sparse = new SparseSet<E>(order);
        for (E e : elements)
            sparse.add(e);
        return new ConstantSet<E>(sparse);
    }

    /**
     * Returns a constant set sorted using the specified order and holding the specified elements.
     */
    public static <E> ConstantSet<E> of(Order<? super E> order, E... elements) {
        SparseSet<E> sparse = new SparseSet<E>(order);
        for (E e : elements)
            sparse.add(e);
        return new ConstantSet<E>(sparse);
    }

    /** Holds the elements. */
    private final SparseSet<E> sparse;

    /** Private Constructor.*/
    private ConstantSet(SparseSet<E> sparse) {
        this.sparse = sparse;
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
    public boolean addAll(E...elements) {
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
    public ConstantSet<E> clone() {
        return this;
    }

    @Override
    public Order<? super E> comparator() {
        return sparse.comparator();
    }

    @Override
    public boolean contains(Object obj) {
        return sparse.contains(obj);
    }

    @Override
    public ReadOnlyIterator<E> descendingIterator() {
        return ReadOnlyIterator.of(sparse.descendingIterator());
    }

    @Override
    public ReadOnlyIterator<E> descendingIterator(E fromElement) {
        return ReadOnlyIterator.of(sparse.descendingIterator(fromElement));
    }

    @Override
    public ReadOnlyIterator<E> iterator() {
        return ReadOnlyIterator.of(sparse.iterator());
    }

    @Override
    public ReadOnlyIterator<E> iterator(E fromElement) {
        return ReadOnlyIterator.of(sparse.iterator(fromElement));
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
        return sparse.size();
    }

    /** Returns {@code this}.*/
    @Override
    public ConstantSet<E> unmodifiable() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return sparse.isEmpty();
    }

}