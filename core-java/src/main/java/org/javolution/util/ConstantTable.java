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
import java.util.NoSuchElementException;

import org.javolution.lang.Constant;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * <p> A table for which immutability is guaranteed by construction.
 * <pre>{@code
 * // From literal elements.
 * ConstantTable<String> winners = ConstantTable.of("John Deuff", "Otto Graf", "Sim Kamil");
 * 
 * // From existing collections.
 * ConstantTable<String> caseInsensitiveWinners = ConstantTable.of(Equality.LEXICAL_CASE_INSENSITIVE, winners);
 * }</pre></p>
 * 
 * <p> This class ensures that calling a method which may modify the table will most likely generate a warning
 *     (deprecated warning) at compile time and will most certainly raise an exception at run-time.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
@Constant
public final class ConstantTable<E> extends FastTable<E> {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Constant tables cannot be modified.";

    /**
     * Returns a constant table holding the elements from the specified collection. 
     */

    @SuppressWarnings("unchecked")
    public static <E> ConstantTable<E> of(Collection<? super E> elements) {
        return new ConstantTable<E>(Equality.DEFAULT, (E[]) elements.toArray(new Object[elements.size()]));
    }

    /**
     * Returns a constant table holding the specified elements. 
     */
    public static <E> ConstantTable<E> of(@Constant E... elements) {
        return new ConstantTable<E>(Equality.DEFAULT, elements);
    }

    /**
     * Returns a constant table using the specified equality and holding the elements from the specified collection. 
     */
    @SuppressWarnings("unchecked")
    public static <E> ConstantTable<E> of(Equality<? super E> equality, Collection<? extends E> elements) {
        return new ConstantTable<E>(equality, (E[]) elements.toArray(new Object[elements.size()]));
    }

    /**
     * Returns a constant table using the specified equality and holding the specified elements. 
     */
    public static <E> ConstantTable<E> of(Equality<? super E> equality, @Constant E... elements) {
        return new ConstantTable<E>(equality, elements);
    }

    /** Holds the equality comparator. */
    private final Equality<? super E> equality;

    /** Holds the elements. */
    private final E[] elements;

    /** Creates a new instance from the specified elements and equality. */
    ConstantTable(Equality<? super E> equality, E[] elements) {
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
    public ConstantTable<E> clone() {
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
    public ConstantTable<E> unmodifiable() {
        return this;
    }
    
    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public int addSorted(E element,  Comparator<? super E> cmp) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the table unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public int removeSorted(E element,  Comparator<? super E> cmp) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public ReadOnlyIterator<E> iterator() {
        return new ReadOnlyIterator<E>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < elements.length;
            }

            @Override
            public E next() {
                if (index < elements.length) throw new NoSuchElementException();
                return elements[index++];
            }};
    }

}