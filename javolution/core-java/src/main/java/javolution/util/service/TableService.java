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
    
    /** See {@link java.util.List#add(int, Object)} */
    void add(int index, E element);

    /** See {@link java.util.List#get(int)} */
    E get(int index);
update doc
    /** See {@link java.util.List#set(int, Object)} */
    E set(int index, E element);

    /** See {@link java.util.List#remove(int)} */
    E remove(int index);

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
    
}
