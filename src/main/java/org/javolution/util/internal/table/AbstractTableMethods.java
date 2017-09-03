/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.table;

import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.RandomAccess;

import org.javolution.util.internal.collection.AbstractCollectionMethods;

/** 
 * Holds all AbstractTable methods which need to be overridden by Atomic / Shared views.
 * Should be updated whenever AbstractTable is modified and implementation of Atomic / Shared views verified. 
 */
public interface AbstractTableMethods<E> extends AbstractCollectionMethods<E>, List<E>, Deque<E>, RandomAccess {

    void sort(Comparator<? super E> cmp);


}