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
import java.util.Comparator;

import org.javolution.annotations.ReadOnly;
import org.javolution.lang.Immutable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * <p> A table for which immutability is guaranteed by construction.
 * 
 * <pre>{@code
 * // Creation from literal elements.
 * ConstTable<String> winners = ConstTable.of("John Deuff", "Otto Graf", "Sim Kamil");
 * 
 * // Creation from existing collections.
 * ConstTable<String> caseInsensitiveWinners = ConstTable.of(Equality.LEXICAL_CASE_INSENSITIVE, winners);
 * }</pre></p>
 * 
 * <p> This class ensures that calling a method which may modify the table will generate a deprecated warning
 *     at compile time and will most raise an exception at run-time.</p>
 * 
 * @param <E> the immutable set element
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
@ReadOnly
public final class ConstTable<E> extends FastTable<E> implements Immutable {

    
    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Constant tables cannot be modified.";
    private static final ConstTable<?> EMPTY  = new ConstTable<Object>(Equality.DEFAULT, new Object[0]);

    /**
     * Returns a constant empty table. 
     */
    @SuppressWarnings("unchecked")
    public static <E> ConstTable<E> empty() {
        return (ConstTable<E>) EMPTY;
    }

    /**
     * Returns a constant table holding the elements from the specified collection. 
     */
    @SuppressWarnings("unchecked")
    public static <E> ConstTable<E> of(Collection<? super E> elements) {    
        return new ConstTable<E>(Equality.DEFAULT, (E[]) elements.toArray(new Object[elements.size()]));
    }

    /**
     * Returns a constant table holding the specified elements. 
     */
    public static <E> ConstTable<E> of(@ReadOnly E... elements) {
        return new ConstTable<E>(Equality.DEFAULT, elements);
    }

    /**
     * Returns a constant table using the specified equality and holding the elements from the specified collection. 
     */
    @SuppressWarnings("unchecked")
    public static <E> ConstTable<E> of(Equality<? super E> equality, Collection<? extends E> elements) {
        return new ConstTable<E>(equality, (E[]) elements.toArray(new Object[elements.size()]));
    }

    /**
     * Returns a constant table using the specified equality and holding the specified elements. 
     */
    public static <E> ConstTable<E> of(Equality<? super E> equality, @ReadOnly E... elements) {
        return new ConstTable<E>(equality, elements);
    }

    /** Holds the equality comparator. */
    private final Equality<? super E> equality;

    /** Holds the elements. */
    private final E[] elements;

    /** Creates a new instance from the specified elements and equality. */
    ConstTable(Equality<? super E> equality, E[] elements) {
        this.equality = equality;
        this.elements = elements;
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean addAll(Collection<? extends E> that) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean addAll(E...elements) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> that) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean addAllSorted(Collection<? extends E> that, Comparator<? super E> cmp) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public void addFirst(E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public void addLast(E element) {
        add(size(), element);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /**  Returns {@code this}.*/
    @Override
    public ConstTable<E> clone() {
        return this;
    }

    @Override
    public Equality<? super E> equality() {
        return equality;
    }

    @Override
    public E get(int index) {
        return elements[index];
    }

    @Override
    public boolean isEmpty() {
        return elements.length == 0;
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public E pollLast() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean remove(Object searched) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean removeAll(Collection<?> that) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    public E removeLast() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }
    
    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean retainAll(Collection<?> that) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }
    
    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public int size() {
        return elements.length;
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public void sort(Comparator<? super E> cmp) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** Returns {@code this}.*/
    @Override
    public ConstTable<E> unmodifiable() {
        return this;
    }
    
    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean addSorted(E element,  Comparator<? super E> cmp) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public boolean removeSorted(E element,  Comparator<? super E> cmp) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

}