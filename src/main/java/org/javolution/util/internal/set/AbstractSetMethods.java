/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.set;

import java.util.SortedSet;

import org.javolution.annotations.Nullable;
import org.javolution.util.FastIterator;
import org.javolution.util.function.Order;
import org.javolution.util.internal.collection.AbstractCollectionMethods;

/** 
 * Holds all AbstractSet methods which need to be overridden by Atomic / Shared views.
 * Should be updated whenever AbstractSet is modified and implementation of Atomic / Shared views verified. 
 */
public interface AbstractSetMethods<E> extends AbstractCollectionMethods<E>,  SortedSet<E> {
    
    SortedSet<E> subSet(E element);
        
    boolean add(E element, boolean allowDuplicate);

    E getAny(E element);
    
    E removeAny(E element);
    
    Order<? super E> order();
 
    FastIterator<E> iterator(@Nullable E from);
 
    FastIterator<E> descendingIterator(@Nullable E from);
   
}