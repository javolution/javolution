/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import org.javolution.util.FastMap;
import org.javolution.util.SparseMap;
import org.javolution.util.function.Order;
import org.javolution.util.internal.SparseArrayImpl;

/**
 * Sparse map inner implementation.
 */
public final class InnerSparseMapImpl<K, V> extends SparseMap<K, V> implements SparseArrayImpl.Inner<K, FastMap.Entry<K,V>> {

    private static final long serialVersionUID = 0x700L; // Version.

    public InnerSparseMapImpl(Order<? super K> keyOrder) {
        super(keyOrder);    
    }
    
    public InnerSparseMapImpl<K,V> clone() {
        return (InnerSparseMapImpl<K,V>)super.clone();
    }
}
