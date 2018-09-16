/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.collection;

import java.io.Serializable;
import java.util.Collection;

import org.javolution.util.AbstractCollection;
import org.javolution.util.FastIterator;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/** 
 * Holds all AbstractCollection methods which need to be overridden by Atomic / Shared views.
 * Should be updated whenever AbstractCollection is modified and implementation of Atomic / Shared views verified. 
 */
public interface AbstractCollectionMethods<E> extends Collection<E>, Serializable, Cloneable {
        
    void forEach(final Consumer<? super E> consumer);
    
    E reduce(BinaryOperator<E> operator);

    boolean removeIf(Predicate<? super E> filter);

    E findAny();
    
    boolean anyMatch(Predicate<? super E> predicate);
    
    AbstractCollection<E> collect();
    
    FastIterator<E> iterator();

    FastIterator<E> descendingIterator();

    boolean add(E element);

    boolean isEmpty();

    int size();

    void clear();

    boolean contains(final Object searched);
    
    boolean remove(final Object searched);
    
    boolean addAll(Collection<? extends E> that);

    boolean addAll(@SuppressWarnings("unchecked") E... elements);

    boolean containsAll(Collection<?> that);

    boolean removeAll(final Collection<?> that);
    
    boolean retainAll(final Collection<?> that);
    
    Object[] toArray();

    <T> T[] toArray(final T[] array);

    boolean equals(Object obj);

    int hashCode();

    String toString();
    
    Equality<? super E> equality();

    AbstractCollection<E>[] trySplit(int n);
    
    AbstractCollectionMethods<E> clone();
    
}