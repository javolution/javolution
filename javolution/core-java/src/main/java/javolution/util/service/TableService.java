/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import javolution.util.FastTable;

/**
 * The set of related functionalities which can be used/reused to 
 * implement tables collections.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 * @see FastTable
 */
public interface TableService<E> extends CollectionService<E> {

    //
    // List interface.
    //
    
    /** See {@link java.util.List#add(int, Object)} */
    void add(int index, E element);

    /** See {@link java.util.List#get(int)} */
    E get(int index);

    /** See {@link java.util.List#set(int, Object)} */
    E set(int index, E element);

    /** See {@link java.util.List#indexOf(Object)} */
    int indexOf(E element);

    /** See {@link java.util.List#lastIndexOf(Object)} */
    int lastIndexOf(E element);

    /** See {@link java.util.List#remove(int)} */
    E remove(int index);

    //
    // Deque interface.
    //
    
    /** See {@link java.util.Deque#getFirst() } */
    E getFirst();

    /** See {@link java.util.Deque#getLast() } */
    E getLast();

    /** See {@link java.util.Deque#addFirst(java.lang.Object) } */
    void addFirst(E element);

    /** See {@link java.util.Deque#getLast() } */
    void addLast(E element);

    /** See {@link java.util.Deque#removeFirst() } */
    E removeFirst();

    /** See {@link java.util.Deque#removeLast() } */
    E removeLast();

    /** See {@link java.util.Deque#pollFirst() } */
    E pollFirst();

    /** See {@link java.util.Deque#pollLast() } */
    E pollLast();

    /** See {@link java.util.Deque#peekFirst() } */
    E peekFirst();

    /** See {@link java.util.Deque#peekLast() } */
    E peekLast();
    
    //
    // Misc.
    //       

    /** Returns the comparator to be used for element ordering/comparisons } */
    ComparatorService<E> comparator();
    
    /** See {@link FastTable#sort() } */
    void sort();
                   
}
