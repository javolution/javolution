/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;


/**
 * The set of related functionalities used to implement tables collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public interface TableService<E> extends CollectionService<E> {

    /**
     * Returns the number of elements in this table.
     */
    int size();

    /**
     * Removes all of the elements from this table.
     */
    void clear();
    
    /** 
     * Inserts the specified element at the specified position in this list
     * Shifts the element currently at that position to the right.
     */
    void add(int index, E element);

    /** 
     * Returns the element at the specified position.
     */
    E get(int index);

    /** 
     * Replaces the element at the specified position and returns the
     * previous element.
     */
    E set(int index, E element);

    /** 
     * Removes the element as the specified position.
     */
    E remove(int index);

    /**
     * Dequeue methods.
     */
    
    /** 
     * Returns the first element. 
     */
    E getFirst();

    /**
     *  Returns the last element.
     */
    E getLast();

    /** 
     * Inserts the specified element at the front of this table.
     */
    void addFirst(E element);

    /** 
     * Inserts the specified element at the end of this table.
     */
    void addLast(E element);

    /** 
     *  Retrieves and removes the first element of this table.
     */
    E removeFirst();
    
    /** 
     *  Retrieves and removes the last element of this table.
     */
    E removeLast();
    
    /**
     * Retrieves, but does not remove, the first element of this table,
     * or returns {@link null} if this table is empty (atomic operation).
     */
    E peekFirst();

    /**
     * Retrieves, but does not remove, the last element of this table,
     * or returns {@link null} if this table is empty (atomic operation).
     */
    E peekLast();

    /**
     * Retrieves and removes the first element of this table,
     * or returns {@link null} if this table is empty (atomic operation).
     */
    E pollFirst();

    /**
     * Retrieves and removes the last element of this table,
     * or returns {@link null} if this table is empty (atomic operation).
     */
    E pollLast();    
}
